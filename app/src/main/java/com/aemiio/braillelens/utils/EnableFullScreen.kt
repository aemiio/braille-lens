package com.aemiio.braillelens.utils

import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.aemiio.braillelens.ui.screens.findActivity

@Composable
fun EnableFullScreen() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = view.context.findActivity()?.window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.setDecorFitsSystemWindows(false)
        } else {
            window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        WindowCompat.setDecorFitsSystemWindows(
            window ?: return@DisposableEffect onDispose {},
            false
        )

        onDispose {}
    }
}