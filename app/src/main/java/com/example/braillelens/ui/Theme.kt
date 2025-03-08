package com.example.braillelens.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkOrange = Color(0xFFFF9D23)
val DarkOlive = Color(0xFF71871E)
val DarkTeal = Color(0xFF08424E)

val LightCream = Color(0xFFFCF3E3)
val BackgroundCream = Color(0xFFF8EDD9)
val BackgroundGrey = Color(0xFFF7F7F7)

val PastelOrange = Color(0xFFFFDEAB)
val PastelGreen = Color(0xFFD3DB9B)
val PastelTeal = Color(0xFF8BAFB6)

val AccentRed = Color(0xFFC14600)
val AccentBeige = Color(0xFFE5D0AC)
val AccentPurple = Color(0xFF8C3061)
val FontWhite = Color(0xFFFFFFFF)
val FontBlack = Color(0xFF131010)


private val LightColors = lightColorScheme(
    primary = DarkOrange,
    secondary = DarkOlive,
    tertiary = DarkTeal,
    background = BackgroundCream,
    surface = LightCream,
    onPrimary = FontWhite,
    onSecondary = FontWhite,
    onTertiary = FontWhite,
    onBackground = FontBlack,
    onSurface = FontBlack
)


private val DarkColors = darkColorScheme(
    primary = PastelOrange,
    secondary = PastelGreen,
    tertiary = PastelTeal,
    background = DarkTeal,
    surface = DarkOlive,
    onPrimary = FontBlack,
    onSecondary = FontBlack,
    onTertiary = FontBlack,
    onBackground = FontWhite,
    onSurface = FontWhite
)

@Composable
fun BrailleLensTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
