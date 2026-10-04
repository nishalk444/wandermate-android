package com.wandermate.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wandermate.app.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class TravelViewModel @Inject constructor(private val repository: TravelRepository) : ViewModel() {
    val trips = repository.trips.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val favorites = repository.favorites.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())
    val theme = MutableStateFlow(repository.theme())
    val saving = MutableStateFlow(false)
    val weather = MutableStateFlow<Map<String, WeatherState>>(emptyMap())
    private val events = Channel<String>(Channel.BUFFERED)
    val messages = events.receiveAsFlow()
    private val mutex = Mutex()
    private fun work(block: suspend () -> Unit) { viewModelScope.launch {
        try { block() } catch (e: CancellationException) { throw e } catch (e: Exception) { events.send(e.message ?: "Something went wrong. Please try again.") }
    } }
    fun message(value: String) { work { events.send(value) } }
    fun save(trip: Trip, then: (String) -> Unit = {}) {
        if (saving.value) return
        saving.value = true
        work { try { repository.save(trip); then(trip.id) } finally { saving.value = false } }
    }
    fun edit(id: String, transform: (Trip) -> Trip) { work { mutex.withLock {
        val latest = repository.trips.first().firstOrNull { it.id == id } ?: return@withLock
        repository.save(transform(latest))
    } } }
    fun delete(id: String, then: () -> Unit) { work { repository.delete(id); then() } }
    fun favorite(id: String) { work { mutex.withLock { repository.favorite(id, id !in repository.favorites.first()) } } }
    fun theme(value: String) { repository.setTheme(value); theme.value = value }
    fun export(trip: Trip) = repository.export(trip)
    fun import(text: String, then: (String) -> Unit) { work { then(repository.import(text).id); events.send("Trip imported as a new copy.") } }
    fun forecast(id: String) { if (weather.value[id]?.loading == true) return
        work {
            val cached = repository.cachedWeather(id)
            weather.update { it + (id to (cached ?: WeatherState()).copy(loading = true)) }
            try { val result = repository.fetchWeather(Catalog.destination(id)); weather.update { it + (id to result) } }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { weather.update { it + (id to (cached ?: WeatherState()).copy(message = if (cached == null) "Forecast unavailable. Connect to the internet and retry." else "Showing saved forecast. Refresh when connected.")) } }
        }
    }
}
