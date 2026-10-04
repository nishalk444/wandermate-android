package com.wandermate.app.domain

import com.wandermate.app.data.*
import org.junit.Assert.*
import org.junit.Test

class PlannerTest {
    @Test fun `money parsing preserves exact cents and rejects extra decimals`() {
        assertEquals(1029L, Planner.cents("10.29"))
        assertEquals(0L, Planner.cents("0"))
        assertNull(Planner.cents("10.299"))
        assertNull(Planner.cents("-1"))
        assertNull(Planner.cents("NaN"))
        assertNull(Planner.cents("999999999999"))
    }
    @Test fun `planner respects family interest and group budget constraints`() {
        val result = Planner.suggest(Catalog.placesFor("nyc"), 3, true, setOf("Culture"), 6000L, 2)
        val places = result.map { Catalog.place(it.placeId)!! }
        assertEquals(1, places.size)
        assertTrue(places.all { it.family && it.category == "Culture" })
        assertTrue(places.sumOf { it.estimatedCost * 200L } <= 6000)
    }
    @Test fun `free plan has no paid attractions and no duplicated stops`() {
        val stops = Planner.suggest(Catalog.placesFor("nyc"), 14, false, emptySet(), 0, 2)
        assertTrue(stops.isNotEmpty())
        assertTrue(stops.all { Catalog.place(it.placeId)!!.estimatedCost == 0 })
        assertEquals(stops.size, stops.map { it.placeId }.distinct().size)
        assertTrue(stops.groupBy { it.day }.values.all { it.size <= 3 })
    }
    @Test fun `planner daily time budget includes buffers`() {
        val template = Catalog.places.first()
        val places = (1..20).map { template.copy(id = "$it", minutes = 180) }
        val stops = Planner.suggest(places, 2, false, emptySet(), 0, 1)
        assertEquals(4, stops.size)
        assertTrue(stops.groupBy { it.day }.values.all { it.size * 210 <= 420 })
    }
    @Test fun `distance handles identical and nearby points`() {
        assertEquals(0.0, Planner.distanceKm(40.0,-74.0,40.0,-74.0), .0001)
        assertTrue(Planner.distanceKm(40.0,-74.0,41.0,-74.0) in 110.0..112.0)
    }
    @Test fun `dates reject impossible calendar dates`() {
        assertTrue(Planner.validDate("2028-02-29"))
        assertFalse(Planner.validDate("2026-02-29"))
        assertFalse(Planner.validDate("tomorrow"))
    }
    @Test fun `import validation rejects places from another destination`() {
        val trip = Trip(name = "Test", destinationId = "nyc", startDate = "2026-10-16", days = 3, budgetCents = 10000, stops = listOf(Stop("falls",1)))
        assertThrows(IllegalArgumentException::class.java) { Planner.validate(trip) }
    }
    @Test fun `import validation rejects duplicate stops and invalid days`() {
        val trip = Trip(name = "Test", destinationId = "nyc", startDate = "2026-10-16", days = 3, budgetCents = 10000)
        assertThrows(IllegalArgumentException::class.java) { Planner.validate(trip.copy(stops = listOf(Stop("central",1),Stop("central",2)))) }
        assertThrows(IllegalArgumentException::class.java) { Planner.validate(trip.copy(stops = listOf(Stop("central",4)))) }
    }
    @Test fun `import validation rejects unsafe booking links and negative spending`() {
        val trip = Trip(name = "Test", destinationId = "nyc", startDate = "2026-10-16", days = 3, budgetCents = 10000)
        assertThrows(IllegalArgumentException::class.java) { Planner.validate(trip.copy(bookings = listOf(Booking(label = "Booking", reference = "x", url = "javascript:alert(1)")))) }
        assertThrows(IllegalArgumentException::class.java) { Planner.validate(trip.copy(expenses = listOf(Expense(label = "Food", category = "Food", cents = -1)))) }
    }
    @Test fun `supported trip validates and catalog identities are unique`() {
        Planner.validate(Trip(name = "Test", destinationId = "nyc", startDate = "2026-10-16", days = 3, budgetCents = 10000, stops = listOf(Stop("central",1))))
        assertEquals(Catalog.places.size, Catalog.places.map { it.id }.distinct().size)
        assertTrue(Catalog.places.all { p -> Catalog.destinations.any { it.id == p.destinationId } })
    }
}
