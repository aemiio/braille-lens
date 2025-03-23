package com.example.braillelens.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import com.example.braillelens.objectdetection.BrailleClassIdMapper
import com.example.braillelens.objectdetection.BraillePostProcessor
import com.example.braillelens.objectdetection.ObjectDetector
import com.example.braillelens.objectdetection.ProcessedDetectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class ObjectDetectionService {
    private val objDetector = ObjectDetector()
    private val TAG = "ObjectDetectionService"

    // Expose property for UI control
    var confidenceThreshold: Float = 0.25f

    suspend fun detectBrailleFromUri(
        context: Context,
        imageUri: Uri,
        detectionMode: String
    ): ProcessedDetectionResult = withContext(Dispatchers.IO) {
        val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw IllegalArgumentException("Could not open image")

        // Initialize BrailleClassIdMapper
        BrailleClassIdMapper.loadMappingsFromResources(context)

        // Reset BraillePostProcessor states for new detection
        BraillePostProcessor.resetStates()

        val modelName = mapDetectionModeToModel(detectionMode)
        val classes = readClasses(context, modelName)

        // Configure paint objects for visualization
        val boxPaint = createBoxPaint()
        val textPaint = createTextPaint()

        try {
            // Use the detect method directly instead of accessing internal methods
            val result = objDetector.detect(inputStream, context, confidenceThreshold, modelName)

            // Process and return the results
            BraillePostProcessor.processDetections(
                result = result,
                context = context,
                currentModel = modelName,
                classes = classes,
                boxPaint = boxPaint,
                textPaint = textPaint
            )
        } finally {
            inputStream.close()
            objDetector.close()
        }
    }

    suspend fun detectBrailleFromDrawableResource(
        context: Context,
        resourceId: Int,
        detectionMode: String
    ): ProcessedDetectionResult = withContext(Dispatchers.IO) {
        // Initialize BrailleClassIdMapper
        BrailleClassIdMapper.loadMappingsFromResources(context)

        // Reset BraillePostProcessor states for new detection
        BraillePostProcessor.resetStates()

        val modelName = mapDetectionModeToModel(detectionMode)
        val classes = readClasses(context, modelName)

        // Get bitmap from drawable resource
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)

        // Configure paint objects for visualization
        val boxPaint = createBoxPaint()
        val textPaint = createTextPaint()

        try {
            // Construct an InputStream from the bitmap
            val inputStream = bitmap.let {
                val stream = java.io.ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.PNG, 100, stream)
                java.io.ByteArrayInputStream(stream.toByteArray())
            }

            // Use the detect method directly
            val result = objDetector.detect(inputStream, context, confidenceThreshold, modelName)

            // Process and return the results
            BraillePostProcessor.processDetections(
                result = result,
                context = context,
                currentModel = modelName,
                classes = classes,
                boxPaint = boxPaint,
                textPaint = textPaint
            )
        } finally {
            objDetector.close()
        }
    }

    private fun mapDetectionModeToModel(detectionMode: String): String {
        return when (detectionMode) {
            "Grade 1 Braille" -> ObjectDetector.MODEL_G1
            "Grade 2 Braille" -> ObjectDetector.MODEL_G2
            "Both Grades" -> ObjectDetector.BOTH_MODELS
            else -> ObjectDetector.MODEL_G1
        }
    }

    private fun createBoxPaint(): Paint {
        return Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
    }

    private fun createTextPaint(): Paint {
        return Paint().apply {
            color = Color.WHITE
            textSize = 25f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(3f, 1f, 1f, Color.BLACK)
        }
    }

    private fun readClasses(context: Context, modelName: String): List<String> {
        return when (modelName) {
            ObjectDetector.MODEL_G2 -> {
                context.resources.openRawResource(
                    context.resources.getIdentifier("g2_classes", "raw", context.packageName)
                ).bufferedReader().readLines()
            }

            ObjectDetector.BOTH_MODELS -> {
                val g1Classes = context.resources.openRawResource(
                    context.resources.getIdentifier("g1_classes", "raw", context.packageName)
                ).bufferedReader().readLines()

                val g2Classes = context.resources.openRawResource(
                    context.resources.getIdentifier("g2_classes", "raw", context.packageName)
                ).bufferedReader().readLines()

                g1Classes + g2Classes
            }

            else -> { // Default to G1
                context.resources.openRawResource(
                    context.resources.getIdentifier("g1_classes", "raw", context.packageName)
                ).bufferedReader().readLines()
            }
        }
    }
}