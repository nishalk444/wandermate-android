package com.wandermate.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Destination(
    val id: String, val name: String, val region: String, val tagline: String,
    val description: String, val season: String, val transport: String,
    val tips: List<String>, val latitude: Double, val longitude: Double,
    val source: String, val photo: String,
)
@Serializable
data class Place(
    val id: String, val destinationId: String, val name: String, val category: String,
    val description: String, val latitude: Double, val longitude: Double,
    val minutes: Int, val estimatedCost: Int, val family: Boolean, val indoor: Boolean,
    val source: String, val access: String,
)
@Serializable
data class Stop(val placeId: String, val day: Int)
@Serializable
data class Expense(val id: String = UUID.randomUUID().toString(), val label: String, val category: String, val cents: Long)
@Serializable
data class PackingItem(val id: String = UUID.randomUUID().toString(), val label: String, val done: Boolean = false)
@Serializable
data class Booking(val id: String = UUID.randomUUID().toString(), val label: String, val reference: String, val url: String = "", val ticketUri: String = "")
@Serializable
data class Trip(
    val id: String = UUID.randomUUID().toString(), val name: String, val destinationId: String,
    val startDate: String, val days: Int, val budgetCents: Long, val travelers: Int = 1,
    val family: Boolean = false, val stops: List<Stop> = emptyList(),
    val expenses: List<Expense> = emptyList(), val bookings: List<Booking> = emptyList(),
    val packing: List<PackingItem> = listOf("ID and travel documents", "Tickets and reservations", "Phone and charger", "Weather-appropriate clothes", "Water bottle", "Personal essentials").map { PackingItem(label = it) },
    val offlineReady: Boolean = false,
)
@Serializable
data class SharedTrip(val version: Int = 1, val trip: Trip)
@Entity(tableName = "trips")
data class TripEntity(@PrimaryKey val id: String, val json: String)
@Entity(tableName = "favorites")
data class FavoriteEntity(@PrimaryKey val placeId: String)
@Entity(tableName = "weather")
data class WeatherEntity(@PrimaryKey val destinationId: String, val json: String, val fetchedAt: Long)
@Serializable
data class ForecastDay(val date: String, val high: Double, val low: Double, val code: Int) {
    val wet: Boolean get() = code in 51..99
    val summary: String get() = when (code) {
        0 -> "Clear sky"; 1, 2, 3 -> "Partly cloudy"; 45, 48 -> "Fog"
        in 51..67, in 80..82 -> "Rain"; in 71..77, 85, 86 -> "Snow"
        in 95..99 -> "Thunderstorms"; else -> "Mixed conditions"
    }
}
data class WeatherState(val days: List<ForecastDay> = emptyList(), val loading: Boolean = false, val message: String? = null, val fetchedAt: Long? = null)
