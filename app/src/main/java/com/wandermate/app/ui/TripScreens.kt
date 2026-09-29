package com.wandermate.app.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wandermate.app.data.*
import com.wandermate.app.domain.Planner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable fun PlanScreen(initial: String, vm: TravelViewModel, created: (String) -> Unit) {
    val saving by vm.saving.collectAsStateWithLifecycle()
    var destinationId by rememberSaveable { mutableStateOf(initial) }
    var name by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(7).toString()) }
    var days by rememberSaveable { mutableStateOf("3") }
    var travelers by rememberSaveable { mutableStateOf("2") }
    var budget by rememberSaveable { mutableStateOf("500") }
    var family by rememberSaveable { mutableStateOf(false) }
    var interest by rememberSaveable { mutableStateOf("All") }
    var suggest by rememberSaveable { mutableStateOf(true) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("A trip that feels like you", "Start simple. You can change your stops anytime.")
        Text("Choose a destination", style = MaterialTheme.typography.titleMedium)
        Catalog.destinations.forEach { d ->
            FilterChip(destinationId == d.id, { destinationId = d.id }, { Text("${d.name} · ${d.region}") })
        }
        LabeledField(name, { name = it.take(100) }, "Trip name (optional)")
        LabeledField(date, { date = it }, "Start date", supporting = "YYYY-MM-DD")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LabeledField(days, { days = it }, "Days (1–14)", Modifier.weight(1f))
            LabeledField(travelers, { travelers = it }, "Travelers (1–20)", Modifier.weight(1f))
        }
        LabeledField(budget, { budget = it }, "Total trip budget (USD)")
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(family, { family = it }, Modifier.semantics { contentDescription = "Traveling with children" }); Text("Traveling with children") }
        Text("What do you enjoy?")
        Chips(listOf("All", "Nature", "Culture", "Beach", "History"), interest) { interest = it }
        Row(verticalAlignment = Alignment.CenterVertically) { Switch(suggest, { suggest = it }, Modifier.semantics { contentDescription = "Suggest a starting itinerary" }); Spacer(Modifier.width(12.dp)); Text("Suggest a starting itinerary") }
        Note("Suggestions use up to 30% of your total budget for attraction estimates, up to 3 stops per day, and a 30-minute buffer per stop. Travel times and opening hours need checking before you go. Empty days are yours to explore.")
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button({
            val count = days.toIntOrNull()
            val people = travelers.toIntOrNull()
            val amount = Planner.cents(budget)
            if (!Planner.validDate(date) || count == null || count !in 1..14 || people == null || people !in 1..20 || amount == null) {
                error = "Enter a valid date, 1–14 days, 1–20 travelers, and a nonnegative budget with up to two decimals."
            } else {
                error = null
                val stops = if (suggest) Planner.suggest(Catalog.placesFor(destinationId), count, family, if (interest == "All") emptySet() else setOf(interest), amount * 30 / 100, people) else emptyList()
                val trip = Trip(name = name.trim().ifBlank { "${Catalog.destination(destinationId).name} getaway" }, destinationId = destinationId, startDate = date, days = count, budgetCents = amount, travelers = people, family = family, stops = stops)
                vm.save(trip, created)
            }
        }, Modifier.fillMaxWidth(), enabled = !saving) { Text(if (saving) "Saving…" else "Create my trip") }
    }
}

@Composable fun TripsScreen(vm: TravelViewModel, open: (String) -> Unit, plan: () -> Unit) {
    val trips by vm.trips.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bytes = stream.readBytesLimited(1_000_000)
                        String(bytes, Charsets.UTF_8)
                    } ?: error("Unable to read trip file.")
                }
                vm.import(text, open)
            } catch (e: Exception) { vm.message(e.message ?: "Unable to import trip.") }
        }
    }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle("Good things ahead", "Your plans, all in one place") }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(plan, Modifier.weight(1f)) { Text("New trip") }
            OutlinedButton({ import.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }, Modifier.weight(1f)) { Text("Import trip") }
        } }
        if (trips.isEmpty()) item { EmptyState("Your next adventure starts here", "Create a trip or import a plan shared by a friend.") }
        items(trips, key = { it.id }) { trip ->
            OutlinedCard(onClick = { open(trip.id) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(Catalog.destination(trip.destinationId).name.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(trip.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${trip.startDate} · ${trip.days} days · ${trip.travelers} travelers")
                    Text("${trip.stops.size} stops · ${money(trip.budgetCents)} budget", style = MaterialTheme.typography.bodyMedium)
                    Text("Available offline", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
private fun java.io.InputStream.readBytesLimited(limit: Int): ByteArray {
    val output = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (true) {
        val read = read(buffer)
        if (read == -1) break
        require(output.size() + read <= limit) { "Trip file must be smaller than 1 MB." }
        output.write(buffer, 0, read)
    }
    return output.toByteArray()
}

@Composable fun TripScreen(id: String, vm: TravelViewModel, openPlace: (String) -> Unit, back: () -> Unit) {
    val trips by vm.trips.collectAsStateWithLifecycle()
    val trip = trips.firstOrNull { it.id == id }
    if (trip == null) { Box(Modifier.padding(24.dp)) { Text("Loading trip…") }; return }
    var tab by rememberSaveable { mutableStateOf("Itinerary") }
    var delete by rememberSaveable { mutableStateOf(false) }
    var share by rememberSaveable { mutableStateOf(false) }
    var edit by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            try {
                val text = vm.export(trip)
                withContext(Dispatchers.IO) { context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) } ?: error("Unable to create file.") }
                vm.message("Trip exported. Share the file with your travel group.")
            } catch (_: Exception) { vm.message("Unable to export trip. Please try another location.") }
        }
    }
    Column {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(trip.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${trip.startDate} · ${trip.days} days · ${trip.travelers} travelers")
            Chips(listOf("Itinerary", "Budget", "Checklist", "Bookings", "Offline & share"), tab) { tab = it }
        }
        when (tab) {
            "Itinerary" -> ItineraryPanel(trip, vm, openPlace)
            "Budget" -> BudgetPanel(trip, vm)
            "Checklist" -> ChecklistPanel(trip, vm)
            "Bookings" -> BookingsPanel(trip, vm)
            else -> Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("Ready when the signal isn't")
                Note("Your itinerary, attraction details, addresses as map coordinates, checklist, budget, and booking references are already saved locally. Maps and photos are not downloaded. Forecasts are cached after a successful refresh.")
                Button({ vm.edit(id) { it.copy(offlineReady = true) }; vm.message("Offline guide ready. Open this trip anytime without internet.") }, Modifier.fillMaxWidth()) { Text(if (trip.offlineReady) "Offline guide ready ✓" else "Confirm offline guide") }
                SectionTitle("Bring your group along")
                Text("Export a trip file, send it with your preferred sharing app, and ask others to import it in My trips. Each person gets an independent editable copy.")
                Button({ share = true }, Modifier.fillMaxWidth()) { Text("Export trip file") }
                OutlinedButton({
                    val summary = buildString {
                        append("${trip.name}\n${trip.startDate} · ${trip.days} days\n")
                        for (day in 1..trip.days) { append("\nDay $day\n"); trip.stops.filter { it.day == day }.forEach { append("• ${Catalog.place(it.placeId)?.name}\n") } }
                    }
                    try { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, summary) }, "Share itinerary")) } catch (_: Exception) { vm.message("No sharing app available.") }
                }, Modifier.fillMaxWidth()) { Text("Share itinerary summary") }
                OutlinedButton({ edit = true }, Modifier.fillMaxWidth()) { Text("Edit trip name, dates, and budget") }
                TextButton({ delete = true }) { Text("Delete trip", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
    if (share) AlertDialog(onDismissRequest = { share = false }, title = { Text("Export this trip?") }, text = { Text("The file includes expenses, budget, and booking references. Share only with people you trust. Ticket attachments are not included.") }, confirmButton = { TextButton({ share = false; export.launch("wandermate-trip.json") }) { Text("Export") } }, dismissButton = { TextButton({ share = false }) { Text("Cancel") } })
    if (delete) AlertDialog(onDismissRequest = { delete = false }, title = { Text("Delete ${trip.name}?") }, text = { Text("This removes the trip and its saved details from this device.") }, confirmButton = { TextButton({ delete = false; vm.delete(id, back) }) { Text("Delete") } }, dismissButton = { TextButton({ delete = false }) { Text("Keep trip") } })
    if (edit) EditTripDialog(trip, vm) { edit = false }
}

@Composable private fun EditTripDialog(trip: Trip, vm: TravelViewModel, close: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(trip.name) }
    var start by rememberSaveable { mutableStateOf(trip.startDate) }
    var budget by rememberSaveable { mutableStateOf(java.math.BigDecimal(trip.budgetCents).movePointLeft(2).toPlainString()) }
    var error by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = close, title = { Text("Edit trip") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LabeledField(name, { name = it.take(100) }, "Name")
            LabeledField(start, { start = it }, "Start date (YYYY-MM-DD)")
            LabeledField(budget, { budget = it }, "Budget (USD)")
            if (error) Text("Check name, date, and budget.", color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { TextButton({ val amount = Planner.cents(budget); if (name.isBlank() || !Planner.validDate(start) || amount == null) error = true else { vm.edit(trip.id) { it.copy(name = name.trim(), startDate = start, budgetCents = amount) }; close() } }) { Text("Save") } }, dismissButton = { TextButton(close) { Text("Cancel") } })
}

@Composable private fun ItineraryPanel(trip: Trip, vm: TravelViewModel, open: (String) -> Unit) {
    var day by rememberSaveable(trip.id) { mutableIntStateOf(1) }
    var adding by rememberSaveable { mutableStateOf(false) }
    val weather by vm.weather.collectAsStateWithLifecycle()
    LaunchedEffect(trip.destinationId) { vm.forecast(trip.destinationId) }
    val date = LocalDate.parse(trip.startDate).plusDays(day - 1L).toString()
    val forecast = weather[trip.destinationId]?.days?.firstOrNull { it.date == date }
    val stops = trip.stops.filter { it.day == day }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Chips((1..trip.days).map { "Day $it" }, "Day $day") { day = it.removePrefix("Day ").toInt() } }
        item { SectionTitle(date, "${stops.sumOf { Catalog.place(it.placeId)?.minutes ?: 0 }} minutes of visits · allow extra time for travel and breaks") }
        item {
            if (forecast != null) Note("${forecast.summary} · ${forecast.low.toInt()}°–${forecast.high.toInt()}°C. ${if (forecast.wet) "Consider indoor options from Add a stop. Check forecast freshness in the destination guide." else "Allow flexibility if conditions change."}")
            else Text("No forecast available for this trip date.", style = MaterialTheme.typography.bodySmall)
        }
        if (stops.isEmpty()) item { EmptyState("A day with possibilities", "Add a stop below, or keep this day free.") }
        items(stops, key = { it.placeId }) { stop ->
            val p = Catalog.place(stop.placeId)!!
            val index = stops.indexOf(stop)
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("STOP ${index + 1} · ${p.minutes} MIN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    TextButton({ open(p.id) }, contentPadding = PaddingValues(0.dp)) { Text(p.name, style = MaterialTheme.typography.titleMedium) }
                    if (index > 0) {
                        val previous = Catalog.place(stops[index - 1].placeId)!!
                        val km = Planner.distanceKm(previous.latitude, previous.longitude, p.latitude, p.longitude)
                        Text("~${(km * 10).roundToInt() / 10.0} km from prior stop (straight line). Check map for route time.", style = MaterialTheme.typography.bodySmall)
                    }
                    Row {
                        IconButton({ vm.edit(trip.id) { t ->
                            val updated = t.stops.toMutableList(); val current = updated.indexOfFirst { it.placeId == p.id }; val prior = updated.indexOfLast { it.day == day && updated.indexOf(it) < current }
                            if (current >= 0 && prior >= 0) java.util.Collections.swap(updated, current, prior)
                            t.copy(stops = updated)
                        } }, enabled = index > 0) { Icon(Icons.Outlined.ArrowUpward, "Move ${p.name} earlier") }
                        TextButton({ vm.edit(trip.id) { t -> t.copy(stops = t.stops.map { if (it.placeId == p.id) it.copy(day = if (it.day == trip.days) 1 else it.day + 1) else it }) } }, enabled = trip.days > 1) { Text("Next day") }
                        IconButton({ vm.edit(trip.id) { t -> t.copy(stops = t.stops.filterNot { it.placeId == p.id }) } }) { Icon(Icons.Outlined.Close, "Remove ${p.name}") }
                    }
                }
            }
        }
        item { Button({ adding = true }, Modifier.fillMaxWidth()) { Text("Add a stop") } }
        item { Note("Suggested order is a starting point. Opening hours, reservations, road routes, and real travel times are not automatically checked.") }
    }
    if (adding) AlertDialog(onDismissRequest = { adding = false }, title = { Text("Add to day $day") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            val available = Catalog.placesFor(trip.destinationId).filter { p -> trip.stops.none { it.placeId == p.id } }
            if (available.isEmpty()) Text("All guide attractions are already in this trip.")
            available.forEach { p -> TextButton({ vm.edit(trip.id) { it.copy(stops = it.stops + Stop(p.id, day)) }; adding = false }) { Text("${p.name}${if (p.indoor) " · Indoors" else ""}") } }
        }
    }, confirmButton = { TextButton({ adding = false }) { Text("Done") } })
}

@Composable private fun BudgetPanel(trip: Trip, vm: TravelViewModel) {
    var label by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Food") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val spent = trip.expenses.sumOf { it.cents }
    val remaining = trip.budgetCents - spent
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle(if (remaining >= 0) "${money(remaining)} remaining" else "${money(-remaining)} over budget", "Spent ${money(spent)} of ${money(trip.budgetCents)}") }
        item { LinearProgressIndicator(progress = { if (trip.budgetCents == 0L) if (spent > 0) 1f else 0f else (spent.toFloat() / trip.budgetCents).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth()) }
        item { Note("Attraction planning estimate: ${money(trip.stops.sumOf { (Catalog.place(it.placeId)?.estimatedCost ?: 0) * 100L } * trip.travelers)} for your group. Not added to actual spending. Fees, discounts, and per-vehicle rates vary.") }
        item { SectionTitle("Add an expense") }
        item { LabeledField(label, { label = it.take(100) }, "What was it for?") }
        item { LabeledField(amount, { amount = it }, "Amount (USD)") }
        item { Chips(listOf("Food", "Transport", "Stay", "Activities", "Other"), category) { category = it } }
        item {
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button({ val cents = Planner.cents(amount); if (label.isBlank() || cents == null || cents <= 0) error = "Enter a description and a positive amount with up to two decimals." else {
                val expense = Expense(label = label.trim(), category = category, cents = cents)
                vm.edit(trip.id) { it.copy(expenses = it.expenses + expense) }; label = ""; amount = ""; error = null
            } }) { Text("Add expense") }
        }
        if (trip.expenses.isEmpty()) item { Note("No expenses yet. Your budget tracks actual spending entered here.") }
        items(trip.expenses, key = { it.id }) { expense ->
            ListItem(headlineContent = { Text(expense.label) }, supportingContent = { Text(expense.category) }, trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) { Text(money(expense.cents)); IconButton({ vm.edit(trip.id) { it.copy(expenses = it.expenses.filterNot { e -> e.id == expense.id }) } }) { Icon(Icons.Outlined.DeleteOutline, "Delete expense ${expense.label}") } }
            })
        }
    }
}
@Composable private fun ChecklistPanel(trip: Trip, vm: TravelViewModel) {
    var label by rememberSaveable { mutableStateOf("") }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("A little preparation", "${trip.packing.count { it.done }} of ${trip.packing.size} packed") }
        items(trip.packing, key = { it.id }) { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(item.done, { value -> vm.edit(trip.id) { it.copy(packing = it.packing.map { p -> if (p.id == item.id) p.copy(done = value) else p }) } }, Modifier.semantics { contentDescription = item.label })
                Text(item.label, Modifier.weight(1f))
                IconButton({ vm.edit(trip.id) { it.copy(packing = it.packing.filterNot { p -> p.id == item.id }) } }) { Icon(Icons.Outlined.Close, "Remove ${item.label}") }
            }
        }
        item { LabeledField(label, { label = it.take(200) }, "Add a packing item") }
        item { Button({ val item = PackingItem(label = label.trim()); vm.edit(trip.id) { it.copy(packing = it.packing + item) }; label = "" }, enabled = label.isNotBlank() && trip.packing.size < 100) { Text("Add item") } }
    }
}
@Composable private fun BookingsPanel(trip: Trip, vm: TravelViewModel) {
    var label by rememberSaveable { mutableStateOf("") }
    var reference by rememberSaveable { mutableStateOf("") }
    var url by rememberSaveable { mutableStateOf("") }
    var ticket by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); ticket = uri.toString() } catch (_: Exception) { vm.message("Unable to keep access to that attachment. Try another file.") }
    }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("Everything in one place", "Store confirmations you have already booked") }
        item { Note("Do not store payment card details or passport numbers. Ticket files remain with their document provider; ensure they are downloaded there for offline use.") }
        items(trip.bookings, key = { it.id }) { booking ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(booking.label, style = MaterialTheme.typography.titleMedium)
                    Text("Reference: ${booking.reference.ifBlank { "Not entered" }}")
                    if (booking.url.isNotBlank()) TextButton({ openLink(context, booking.url, { vm.message("Unable to open booking link.") }) }) { Text("Open booking") }
                    if (booking.ticketUri.isNotBlank()) TextButton({ openLink(context, booking.ticketUri, { vm.message("Ticket unavailable. The file may have moved or access expired.") }) }) { Text("View ticket") }
                    TextButton({ vm.edit(trip.id) { it.copy(bookings = it.bookings.filterNot { b -> b.id == booking.id }) } }) { Text("Remove booking") }
                }
            }
        }
        item { LabeledField(label, { label = it.take(100) }, "Hotel, flight, or activity") }
        item { LabeledField(reference, { reference = it.take(200) }, "Confirmation reference") }
        item { LabeledField(url, { url = it }, "Booking link (optional, https://)") }
        item { OutlinedButton({ picker.launch(arrayOf("application/pdf", "image/*")) }) { Text(if (ticket.isBlank()) "Attach ticket" else "Ticket attached · Change") } }
        item { Button({
            if (url.isNotBlank() && (!url.startsWith("https://") || android.net.Uri.parse(url).host.isNullOrBlank())) vm.message("Use a valid https:// booking link.")
            else { val b = Booking(label = label.trim(), reference = reference.trim(), url = url.trim(), ticketUri = ticket); vm.edit(trip.id) { it.copy(bookings = it.bookings + b) }; label = ""; reference = ""; url = ""; ticket = "" }
        }, enabled = label.isNotBlank() && trip.bookings.size < 100) { Text("Save booking") } }
    }
}
