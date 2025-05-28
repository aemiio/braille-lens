package com.aemiio.braillelens.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Utility functions for image capture and processing
 */
object BrailleImageUtils {
    /**
     * Save detection mode and image path to shared preferences
     */
    fun saveCapturedData(context: Context, mode: String, imagePath: String) {
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("lastDetectionMode", mode)
            .putString("lastImagePath", imagePath)
            .apply()
    }

    /**
     * Save an image from URI to a file
     */
    fun saveImageToFile(context: Context, uri: Uri, file: File): String {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
            }
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
    
}