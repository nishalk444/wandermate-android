package com.wandermate.app.data

import android.content.Context
import com.wandermate.app.domain.Planner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TravelRepository @Inject constructor(private val dao: TravelDao, private val client: OkHttpClient, @ApplicationContext context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val preferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    val trips = dao.trips().map { rows -> rows.map { json.decodeFromString<Trip>(it.json) } }
    val favorites = dao.favorites().map { rows -> rows.map { it.placeId }.toSet() }
    fun theme() = preferences.getString("theme", "System") ?: "System"
    fun setTheme(value: String) { preferences.edit().putString("theme", value).apply() }
    suspend fun save(trip: Trip) { Planner.validate(trip); dao.save(TripEntity(trip.id, json.encodeToString(trip))) }
    suspend fun delete(id: String) = dao.deleteTrip(id)
    suspend fun favorite(id: String, selected: Boolean) { if (selected) dao.favorite(FavoriteEntity(id)) else dao.unfavorite(id) }
    fun export(trip: Trip): String = json.encodeToString(SharedTrip(trip = trip.copy(bookings = trip.bookings.map { it.copy(ticketUri = "") })))
    suspend fun import(text: String): Trip {
        require(text.length <= 1_000_000) { "Trip file is too large." }
        val shared = json.decodeFromString<SharedTrip>(text)
        require(shared.version == 1) { "Unsupported trip format." }
        val trip = shared.trip.copy(id = UUID.randomUUID().toString(), offlineReady = false, bookings = shared.trip.bookings.map { it.copy(ticketUri = "") })
        save(trip)
        return trip
    }
    suspend fun cachedWeather(id: String): WeatherState? = dao.weather(id)?.let {
        WeatherState(json.decodeFromString(it.json), fetchedAt = it.fetchedAt)
    }
    suspend fun fetchWeather(destination: Destination): WeatherState = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=${destination.latitude}&longitude=${destination.longitude}&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=7"
        val days = client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            check(response.isSuccessful) { "Weather service unavailable." }
            val daily = json.parseToJsonElement(response.body?.string() ?: error("Empty forecast")).jsonObject.getValue("daily").jsonObject
            val dates = daily.getValue("time").jsonArray
            dates.indices.map { i -> ForecastDay(dates[i].jsonPrimitive.content,
                daily.getValue("temperature_2m_max").jsonArray[i].jsonPrimitive.double,
                daily.getValue("temperature_2m_min").jsonArray[i].jsonPrimitive.double,
                daily.getValue("weather_code").jsonArray[i].jsonPrimitive.int) }
        }
        val now = System.currentTimeMillis()
        dao.saveWeather(WeatherEntity(destination.id, json.encodeToString(days), now))
        WeatherState(days, fetchedAt = now)
    }
}
