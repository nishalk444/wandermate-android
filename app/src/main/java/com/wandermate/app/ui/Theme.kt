package com.wandermate.app.ui
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val light = lightColorScheme(
    primary = Color(0xFF176B58), onPrimary = Color.White,
    primaryContainer = Color(0xFFD5EDE3), onPrimaryContainer = Color(0xFF073D30),
    secondary = Color(0xFF805724), secondaryContainer = Color(0xFFF4DEBC),
    background = Color(0xFFFAF9F5), surface = Color(0xFFFAF9F5),
    surfaceVariant = Color(0xFFECEFE7), onSurface = Color(0xFF202B27),
)
private val dark = darkColorScheme(primary = Color(0xFF8ED5B9), primaryContainer = Color(0xFF18513F),
    secondary = Color(0xFFE5BC81), background = Color(0xFF111B17), surface = Color(0xFF111B17))
@Composable fun WanderMateTheme(theme: String, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (theme == "Dark" || (theme == "System" && isSystemInDarkTheme())) dark else light, content = content)
}
