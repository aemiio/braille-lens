package com.aemiio.braillelens

import android.app.Application
import com.aemiio.braillelens.utils.TTSManager

class BrailleLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize TTS at app startup
        TTSManager.getInstance(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        // Clean up TTS resources
        TTSManager.getInstance(this).shutdown()
    }
}