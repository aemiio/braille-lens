package com.example.braillelens.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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

val Night = Color(0xFF121212)
val Cafe = Color(0xFF3D2200)
val Ecru = Color(0xFFE7BF74)
val DarkGreen = Color(0xFF2A320B)
val Citron = Color(0xFFC2CD79)
val Navajo = Color(0xFFFFDEAB)


private val LightColors = lightColorScheme(
    primary = PastelGreen,
    secondary = DarkOlive,
    tertiary = PastelOrange,
    background = BackgroundGrey,
    surface = LightCream,
    onPrimary = FontWhite,
    onSecondary = FontWhite,
    onTertiary = FontWhite,
    onBackground = FontBlack,
    onSurface = FontBlack
)


private val DarkColors = darkColorScheme(
    primary = DarkOlive,
    secondary = Citron,
    tertiary = DarkOrange,
    background = Night,
    surface = DarkOlive,
    onPrimary = FontBlack,
    onSecondary = FontBlack,
    onTertiary = FontBlack,
    onBackground = FontWhite,
    onSurface = FontWhite
)

object BrailleLensColors {
    val darkOrange = DarkOrange
    val darkOlive = DarkOlive
    val darkTeal = DarkTeal
    val lightCream = LightCream
    val backgroundCream = BackgroundCream
    val backgroundGrey = BackgroundGrey
    val pastelOrange = PastelOrange
    val pastelGreen = PastelGreen
    val pastelTeal = PastelTeal
    val accentRed = AccentRed
    val accentBeige = AccentBeige
    val accentPurple = AccentPurple
    val fontWhite = FontWhite
    val fontBlack = FontBlack
}

@Composable
fun BrailleLensTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val statusBarColor = BrailleLensColors.pastelGreen

    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = !useDarkTheme
        )
    }

    MaterialTheme(
        colorScheme = if (useDarkTheme) {
            DarkColors
        } else {
            LightColors
        },
        content = content
    )
}
