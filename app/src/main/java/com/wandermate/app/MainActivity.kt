package com.wandermate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import com.wandermate.app.ui.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: TravelViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by viewModel.theme.collectAsStateWithLifecycle()
            WanderMateTheme(theme) { WanderMateApp(viewModel) }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun WanderMateApp(vm: TravelViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "explore"
    val roots = listOf("explore", "trips", "saved", "settings")
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(vm) { vm.messages.collect { snackbar.showSnackbar(it) } }
    Scaffold(
        topBar = { TopAppBar(title = { Text(if (route in roots) "WanderMate" else "Your next discovery") },
            navigationIcon = { if (route !in roots) IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } }) },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { if (route in roots) NavigationBar {
            val icons = listOf(Icons.Outlined.Explore, Icons.Outlined.Luggage, Icons.Outlined.BookmarkBorder, Icons.Outlined.Settings)
            roots.forEachIndexed { i, item -> NavigationBarItem(selected = route == item, onClick = {
                nav.navigate(item) { popUpTo("explore") { saveState = true }; launchSingleTop = true; restoreState = true }
            }, icon = { Icon(icons[i], null) }, label = { Text(listOf("Explore", "My trips", "Saved", "Settings")[i]) }) }
        } },
    ) { padding ->
        NavHost(nav, "explore", Modifier.padding(padding)) {
            composable("explore") { ExploreScreen(vm, { nav.navigate("destination/$it") }, { nav.navigate("place/$it") }, { nav.navigate("plan/nyc") }) }
            composable("destination/{id}") { DestinationScreen(it.arguments!!.getString("id")!!, vm, { id -> nav.navigate("place/$id") }, { id -> nav.navigate("plan/$id") }) }
            composable("place/{id}") { PlaceScreen(it.arguments!!.getString("id")!!, vm) }
            composable("plan/{id}") { PlanScreen(it.arguments!!.getString("id")!!, vm) { id -> nav.navigate("trip/$id") { popUpTo("plan/{id}") { inclusive = true } } } }
            composable("trips") { TripsScreen(vm, { nav.navigate("trip/$it") }, { nav.navigate("plan/nyc") }) }
            composable("trip/{id}") { TripScreen(it.arguments!!.getString("id")!!, vm, { nav.navigate("place/$it") }, { nav.popBackStack() }) }
            composable("saved") { SavedScreen(vm) { nav.navigate("place/$it") } }
            composable("settings") { SettingsScreen(vm) }
        }
    }
}
