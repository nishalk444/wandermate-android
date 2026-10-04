package com.wandermate.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wandermate.app.data.*
import java.text.NumberFormat
import java.util.Locale

fun money(cents: Long): String = NumberFormat.getCurrencyInstance(Locale.US).format(cents / 100.0)
fun openLink(context: Context, url: String, failure: () -> Unit) {
    try {
        val uri = Uri.parse(url)
        require(uri.scheme == "https" || uri.scheme == "geo" || uri.scheme == "content")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { if (uri.scheme == "content") addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) })
    } catch (_: Exception) { failure() }
}
fun mapUrl(place: Place) = "https://www.google.com/maps/search/?api=1&query=${place.latitude},${place.longitude}"

@Composable fun SectionTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable fun Note(text: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)) {
        Text(text, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
    }
}
@Composable fun EmptyState(title: String, description: String, action: String? = null, onAction: () -> Unit = {}) {
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Outlined.Explore, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(description, style = MaterialTheme.typography.bodyMedium)
        if (action != null) Button(onClick = onAction) { Text(action) }
    }
}
@Composable fun Chips(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option -> FilterChip(selected == option, { onSelect(option) }, { Text(option) }) }
    }
}
@Composable fun DestinationCard(destination: Destination, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(24.dp)) {
        Box(Modifier.fillMaxWidth().height(210.dp).background(MaterialTheme.colorScheme.primaryContainer)) {
            AsyncImage(destination.photo, "Scenic inspiration for ${destination.name}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .8f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Text(destination.region.uppercase(), color = Color.White, style = MaterialTheme.typography.labelMedium)
                Text(destination.name, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(destination.tagline, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
@Composable fun PlaceCard(place: Place, saved: Boolean, onSave: () -> Unit, onOpen: () -> Unit) {
    OutlinedCard(onClick = onOpen, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(place.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(place.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${place.minutes} min · ${if (place.estimatedCost == 0) "Est. free entry" else "~${money(place.estimatedCost * 100L)}"}", style = MaterialTheme.typography.bodySmall)
                Text(listOfNotNull(if (place.family) "Family option" else null, if (place.indoor) "Indoors" else "Outdoors").joinToString(" · "), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onSave) { Icon(if (saved) Icons.Outlined.BookmarkAdded else Icons.Outlined.BookmarkBorder, if (saved) "Unsave ${place.name}" else "Save ${place.name}") }
            Icon(Icons.Outlined.ChevronRight, null)
        }
    }
}
@Composable fun LabeledField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, supporting: String? = null) {
    OutlinedTextField(value, onChange, modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        supportingText = if (supporting == null) null else ({ Text(supporting) }))
}
