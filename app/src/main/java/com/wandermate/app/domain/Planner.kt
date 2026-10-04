package com.wandermate.app.domain

import com.wandermate.app.data.*
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.math.*

object Planner {
    fun distanceKm(aLat: Double, aLon: Double, bLat: Double, bLon: Double): Double {
        val dLat = Math.toRadians(bLat - aLat)
        val dLon = Math.toRadians(bLon - aLon)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(aLat)) * cos(Math.toRadians(bLat)) * sin(dLon / 2).pow(2)
        return 6371 * 2 * atan2(sqrt(a.coerceIn(0.0, 1.0)), sqrt((1-a).coerceIn(0.0, 1.0)))
    }
    fun suggest(places: List<Place>, days: Int, family: Boolean, interests: Set<String>, maxAttractionCents: Long, travelers: Int): List<Stop> {
        require(days in 1..14 && travelers in 1..20 && maxAttractionCents >= 0)
        val remaining = places.filter { (!family || it.family) && (interests.isEmpty() || it.category in interests) }.toMutableList()
        val result = mutableListOf<Stop>()
        var balance = maxAttractionCents
        for (day in 1..days) {
            var minutes = 0
            var previous: Place? = null
            while (result.count { it.day == day } < 3) {
                val options = remaining.filter { it.estimatedCost * 100L * travelers <= balance && minutes + it.minutes + 30 <= 420 }
                val next = options.minByOrNull { p -> previous?.let { distanceKm(it.latitude,it.longitude,p.latitude,p.longitude) } ?: p.estimatedCost.toDouble() } ?: break
                result += Stop(next.id, day)
                balance -= next.estimatedCost * 100L * travelers
                minutes += next.minutes + 30
                previous = next
                remaining.remove(next)
            }
        }
        return result
    }
    fun cents(text: String): Long? = try {
        val amount = BigDecimal(text.trim())
        if (amount < BigDecimal.ZERO || amount > BigDecimal("10000000")) null
        else amount.movePointRight(2).longValueExact()
    } catch (_: Exception) { null }
    fun validDate(value: String): Boolean = try { LocalDate.parse(value); true } catch (_: Exception) { false }
    fun validate(trip: Trip) {
        require(trip.name.isNotBlank() && trip.name.length <= 100) { "Trip name must contain 1–100 characters." }
        require(Catalog.destinations.any { it.id == trip.destinationId }) { "This destination is not in the guide catalog." }
        require(validDate(trip.startDate) && trip.days in 1..14 && trip.travelers in 1..20) { "Check dates, trip length, and travelers." }
        require(trip.budgetCents in 0..1_000_000_000L) { "Invalid budget." }
        require(trip.stops.size <= 100 && trip.stops.distinctBy { it.placeId }.size == trip.stops.size) { "Duplicate or excessive stops." }
        require(trip.stops.all { it.day in 1..trip.days && Catalog.place(it.placeId)?.destinationId == trip.destinationId }) { "Invalid itinerary stop." }
        require(trip.expenses.size <= 500 && trip.expenses.all { it.cents in 0..1_000_000_000L && it.label.length in 1..100 }) { "Invalid expenses." }
        require(trip.packing.size <= 100 && trip.packing.all { it.label.length in 1..200 }) { "Invalid checklist." }
        require(trip.bookings.size <= 100 && trip.bookings.all { it.label.length in 1..100 && it.reference.length <= 200 && (it.url.isBlank() || it.url.startsWith("https://")) }) { "Invalid booking details." }
        require(trip.expenses.map { it.id }.distinct().size == trip.expenses.size && trip.packing.map { it.id }.distinct().size == trip.packing.size && trip.bookings.map { it.id }.distinct().size == trip.bookings.size) { "Duplicate item IDs." }
    }
}
