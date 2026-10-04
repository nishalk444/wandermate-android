package com.wandermate.app.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wandermate.app.data.*
import com.wandermate.app.domain.Planner
import java.text.DateFormat
import java.util.Date

@Composable fun ExploreScreen(vm: TravelViewModel, destination: (String) -> Unit, place: (String) -> Unit, plan: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("All") }
    var family by rememberSaveable { mutableStateOf(false) }
    var free by rememberSaveable { mutableStateOf(false) }
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val searching = query.isNotBlank() || category != "All" || family || free
    val places = Catalog.places.filter { p ->
        (p.name.contains(query, true) || Catalog.destination(p.destinationId).name.contains(query, true) || p.description.contains(query, true)) &&
            (category == "All" || p.category == category) && (!family || p.family) && (!free || p.estimatedCost == 0)
    }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("LESS PLANNING. MORE EXPLORING.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text("Where will you\ngo next?", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        }
        item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text("Search cities or attractions") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, label = { Text("Search destinations and places") }, singleLine = true) }
        item { Chips(listOf("All", "Nature", "Beach", "Culture", "History"), category) { category = it } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(family, { family = !family }, { Text("Family friendly") })
            FilterChip(free, { free = !free }, { Text("Free estimate") })
        } }
        item { FilledTonalButton(onClick = plan, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text("Help me plan a trip") } }
        if (searching) {
            item { SectionTitle("Find your next stop", "${places.size} places in our starter guides") }
            if (places.isEmpty()) item { EmptyState("No places found", "Try a different search or remove a filter.", "Clear filters") { query = ""; category = "All"; family = false; free = false } }
            items(places, key = { it.id }) { p -> PlaceCard(p, p.id in favorites, { vm.favorite(p.id) }, { place(p.id) }) }
        } else {
            item { SectionTitle("A little inspiration", "Four starter guides, ready to explore offline") }
            items(Catalog.destinations, key = { it.id }) { d -> DestinationCard(d) { destination(d.id) } }
            item { Note("Guides and saved trips work offline. Photos, live weather, and map navigation need an internet connection. Scenic photos are illustrative.") }
        }
    }
}
@Composable fun DestinationScreen(id: String, vm: TravelViewModel, place: (String) -> Unit, plan: (String) -> Unit) {
    val d = Catalog.destination(id)
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val forecasts by vm.weather.collectAsStateWithLifecycle()
    val weather = forecasts[id]
    val context = LocalContext.current
    var radius by rememberSaveable { mutableStateOf("Any distance") }
    LaunchedEffect(id) { vm.forecast(id) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { DestinationCard(d) { } }
        item { Text(d.description, style = MaterialTheme.typography.bodyLarge) }
        item { Button({ plan(id) }, Modifier.fillMaxWidth()) { Text("Plan a trip here") } }
        item { SectionTitle("Know before you go") }
        item { Note("Best time: ${d.season}\n\nGetting around: ${d.transport}".replace("\n", "\n")) }
        items(d.tips) { Text("• $it") }
        item { SectionTitle("Next 7 days", "Forecast is for upcoming dates, not necessarily your trip dates. Temperatures in °C.") }
        item {
            if (weather?.loading == true) LinearProgressIndicator(Modifier.fillMaxWidth())
            weather?.message?.let { Note(it) }
            weather?.days?.forEach { day -> Text("${day.date} · ${day.summary} · ${day.low.toInt()}°–${day.high.toInt()}°", Modifier.padding(vertical = 4.dp)) }
            weather?.fetchedAt?.let { Text("Updated ${DateFormat.getDateTimeInstance().format(Date(it))}", style = MaterialTheme.typography.labelSmall) }
            TextButton({ vm.forecast(id) }, enabled = weather?.loading != true) { Text("Refresh forecast") }
            TextButton({ openLink(context, "https://open-meteo.com/", { vm.message("Unable to open browser.") }) }) { Text("Weather by Open-Meteo · CC BY 4.0") }
        }
        item { SectionTitle("Places to discover", "Distance is straight-line distance from the destination center.") }
        item { Chips(listOf("Any distance", "Within 5 km", "Within 20 km"), radius) { radius = it } }
        val filtered = Catalog.placesFor(id).filter { radius == "Any distance" || Planner.distanceKm(d.latitude,d.longitude,it.latitude,it.longitude) <= if (radius == "Within 5 km") 5 else 20 }
        if (filtered.isEmpty()) item { Note("No places within this distance. Try a wider radius.") }
        items(filtered, key = { it.id }) { p -> PlaceCard(p, p.id in favorites, { vm.favorite(p.id) }, { place(p.id) }) }
        item { SectionTitle("Nearby essentials", "Open a map search around this destination.") }
        items(listOf("Restrooms", "Pharmacies", "Hospitals", "Restaurants", "Public transport")) { term ->
            OutlinedButton({ openLink(context, "https://www.google.com/maps/search/?api=1&query=${android.net.Uri.encode("$term near ${d.name}")}", { vm.message("No map or browser app available.") }) }, Modifier.fillMaxWidth()) { Text(term) }
        }
        item { TextButton({ openLink(context, d.source, { vm.message("Unable to open browser.") }) }) { Text("Official destination information") } }
        item { Note("Starter guide compiled September 2026. Hours, closures, accessibility, and prices are not live-verified. Check official sources before traveling.") }
    }
}
@Composable fun PlaceScreen(id: String, vm: TravelViewModel) {
    val p = Catalog.place(id) ?: return
    val trips by vm.trips.collectAsStateWithLifecycle()
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var choosing by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(p.category.uppercase(), color = MaterialTheme.colorScheme.primary)
        Text(p.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(p.description, style = MaterialTheme.typography.bodyLarge)
        Note("Suggested visit: ${p.minutes} minutes\nPlanning cost: ~${money(p.estimatedCost * 100L)} per person (fees may use a different basis).\n${if (p.indoor) "Indoor option" else "Outdoor activity"}".replace("\n", "\n"))
        SectionTitle("Access and family planning")
        Text(p.access)
        Text(if (p.family) "Family option: adapt the route and visit length to your group." else "Review terrain and suitability carefully before bringing children.")
        Button({ vm.favorite(id) }, Modifier.fillMaxWidth()) { Text(if (id in favorites) "Remove from saved" else "Save this place") }
        OutlinedButton({ choosing = true }, Modifier.fillMaxWidth()) { Text("Add to an itinerary") }
        OutlinedButton({ openLink(context, mapUrl(p), { vm.message("No map or browser app available.") }) }, Modifier.fillMaxWidth()) { Text("Map and directions") }
        TextButton({ openLink(context, p.source, { vm.message("Unable to open browser.") }) }) { Text("Check official hours, tickets, and access") }
        Note("Opening hours and prices can change. Planning estimates are not ticket quotes. This starter guide was compiled September 2026; use the official link for current information.")
    }
    if (choosing) AlertDialog(onDismissRequest = { choosing = false }, title = { Text("Choose a trip") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            val compatible = trips.filter { it.destinationId == p.destinationId }
            if (compatible.isEmpty()) Text("Create a trip for ${Catalog.destination(p.destinationId).name} first.")
            compatible.forEach { trip -> TextButton({
                vm.edit(trip.id) { t -> if (t.stops.any { it.placeId == id }) t else t.copy(stops = t.stops + Stop(id, 1)) }
                choosing = false; vm.message("Place added to day 1, unless already included.")
            }) { Text(trip.name) } }
        }
    }, confirmButton = { TextButton({ choosing = false }) { Text("Done") } })
}
@Composable fun SavedScreen(vm: TravelViewModel, open: (String) -> Unit) {
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val places = Catalog.places.filter { it.id in favorites }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle("Your little collection", "Ideas for this trip and the next") }
        if (places.isEmpty()) item { EmptyState("No saved places yet", "Tap a bookmark while exploring to save a place here.") }
        items(places, key = { it.id }) { p -> PlaceCard(p, true, { vm.favorite(p.id) }, { open(p.id) }) }
    }
}
@Composable fun SettingsScreen(vm: TravelViewModel) {
    val theme by vm.theme.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionTitle("Make yourself at home", "No account needed")
        SectionTitle("Appearance")
        Chips(listOf("System", "Light", "Dark"), theme) { vm.theme(it) }
        SectionTitle("Your data")
        Text("Trips, budgets, booking references, and checklists are stored on this device. Uninstalling the app removes them. Export trips to keep a copy. Automatic device backup is disabled.")
        SectionTitle("Connections")
        Text("Weather requests send destination coordinates to Open-Meteo. Photos load from Unsplash. Map and official-source buttons open external apps or websites. Your device location is not requested.")
        SectionTitle("Offline access")
        Text("All four text guides and your saved trips are available offline. Photos are not guaranteed offline; maps, directions, source websites, and fresh forecasts require connectivity.")
        SectionTitle("Sharing")
        Text("Export a trip file to share or back up your plan. Anyone with that file can read its budget, expenses, and booking references. Ticket attachments stay on this device. Imports create a separate editable copy; there is no live cloud sync.")
        SectionTitle("About WanderMate")
        Text("Version 0.1 · Kotlin & Jetpack Compose\nCurrency: USD · Language: English".replace("\n", "\n"))
        TextButton({ openLink(context, "https://github.com/nishalk444/wandermate-android", { vm.message("Unable to open browser.") }) }) { Text("Source code and MIT license") }
    }
}
