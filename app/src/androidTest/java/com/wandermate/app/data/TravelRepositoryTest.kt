package com.wandermate.app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TravelRepositoryTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: TravelDatabase
    private lateinit var repository: TravelRepository
    private val databaseName = "repository-test.db"

    private fun openDatabase() {
        database = Room.databaseBuilder(context, TravelDatabase::class.java, databaseName).build()
        repository = TravelRepository(database.dao(), OkHttpClient(), context)
    }

    @Before fun setup() { context.deleteDatabase(databaseName); openDatabase() }
    @After fun cleanup() { database.close(); context.deleteDatabase(databaseName) }

    private fun trip() = Trip(
        name = "Persistence test", destinationId = "nyc", startDate = "2026-10-16", days = 3,
        budgetCents = 50000, stops = listOf(Stop("central", 1)),
        expenses = listOf(Expense(label = "Lunch", category = "Food", cents = 1234)),
        bookings = listOf(Booking(label = "Museum", reference = "TEST-ONLY", ticketUri = "content://test/ticket")),
    )

    @Test fun tripAndBookmarksSurviveDatabaseReopen() = runBlocking {
        val original = trip()
        repository.save(original)
        repository.favorite("central", true)
        database.close()
        openDatabase()
        assertEquals(original, repository.trips.first().single())
        assertEquals(setOf("central"), repository.favorites.first())
        repository.delete(original.id)
        assertTrue(repository.trips.first().isEmpty())
        assertEquals(setOf("central"), repository.favorites.first())
    }

    @Test fun sharedTripRoundTripKeepsDataButStripsDeviceTicketPermissions() = runBlocking {
        val original = trip()
        repository.save(original)
        val exported = repository.export(original)
        assertFalse(exported.contains("content://test/ticket"))
        val imported = repository.import(exported)
        assertNotEquals(original.id, imported.id)
        assertEquals(original.expenses, imported.expenses)
        assertEquals(original.stops, imported.stops)
        assertEquals("", imported.bookings.single().ticketUri)
        assertEquals(2, repository.trips.first().size)
        // Incoming files must not inject local document-provider references either.
        val injected = repository.import(Json.encodeToString(SharedTrip(trip = original)))
        assertEquals("", injected.bookings.single().ticketUri)
    }

    @Test fun unsupportedOrInvalidImportDoesNotWriteToDatabase() = runBlocking {
        val invalidFiles = listOf(
            Json.encodeToString(SharedTrip(version = 2, trip = trip())),
            Json.encodeToString(SharedTrip(trip = trip().copy(stops = listOf(Stop("falls", 1))))),
            "x".repeat(1_000_001),
        )
        invalidFiles.forEach { file ->
            try { repository.import(file); fail("Invalid file was accepted") }
            catch (_: IllegalArgumentException) { /* expected */ }
        }
        assertTrue(repository.trips.first().isEmpty())
    }
}
