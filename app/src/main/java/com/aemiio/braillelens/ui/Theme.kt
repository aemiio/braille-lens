package com.aemiio.braillelens.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.aemiio.braillelens.utils.ThemeMode
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
val Cafe = Color(0xFF504530)
val Ecru = Color(0xFFE7BF74)
val DarkGreen = Color(0xFF39440F)
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
    onSurface = FontBlack,
    onPrimaryContainer = FontWhite,
    onSecondaryContainer = BackgroundCream
)


private val DarkColors = darkColorScheme(
    primary = DarkGreen,
    secondary = Citron,
    tertiary = DarkOrange,
    background = Night,
    surface = Cafe,
    onPrimary = FontWhite,
    onSecondary = FontBlack,
    onTertiary = FontBlack,
    onBackground = FontWhite,
    onSurface = FontWhite,
    onPrimaryContainer = FontBlack,
    onSecondaryContainer = Cafe
)

object BrailleLensColors {
    val darkOrange = DarkOrange
    val darkOlive = DarkOlive
    val backgroundGrey = BackgroundGrey
    val pastelOrange = PastelOrange
    val pastelGreen = PastelGreen
    val accentRed = AccentRed
    val accentBeige = AccentBeige
    val fontWhite = FontWhite
    val fontBlack = FontBlack
}

@Composable
fun BrailleLensTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val systemUiController = rememberSystemUiController()
    val statusBarColor = if (useDarkTheme) {
        DarkColors.secondary
    } else {
        LightColors.secondary
    }

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
