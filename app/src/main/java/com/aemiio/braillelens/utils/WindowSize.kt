package com.aemiio.braillelens.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

data class WindowSize (
    val width: WindowType,
    val height: WindowType
)

enum class WindowType {
    Compact, Medium, Expanded
}

@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current


    return WindowSize(
        width = when {
            configuration.screenWidthDp.dp < 600.dp -> WindowType.Compact
            configuration.screenWidthDp.dp < 840.dp -> WindowType.Medium
            else -> WindowType.Expanded
        },
        height = when {
            configuration.screenHeightDp.dp < 600.dp -> WindowType.Compact
            configuration.screenHeightDp.dp < 840.dp -> WindowType.Medium
            else -> WindowType.Expanded
        }
    )
}