package com.aemiio.braillelens.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import com.aemiio.braillelens.objectdetection.BothModelsMerger
import com.aemiio.braillelens.objectdetection.BrailleClassIdMapper
import com.aemiio.braillelens.objectdetection.BraillePostProcessor
import com.aemiio.braillelens.objectdetection.ObjectDetector
import com.aemiio.braillelens.objectdetection.ProcessedDetectionResult
import com.aemiio.braillelens.ui.screens.DetectedBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.collections.get
import kotlin.div
import kotlin.text.toFloat
import kotlin.text.toInt

class ObjectDetectionService {
    private val objDetector = ObjectDetector()
    private val TAG = "ObjectDetectionService"

    var confidenceThreshold: Float = 0.25f

    private var lastResult: com.aemiio.braillelens.objectdetection.Result? = null

    private var currentModel: String = ObjectDetector.MODEL_G1


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
            currentModel = modelName

            val result = objDetector.detect(inputStream, context, confidenceThreshold, modelName)

            lastResult = result

            // Process and return the results
            return@withContext BraillePostProcessor.processDetections(
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

            currentModel = modelName

            // Use the detect method directly
            val result = objDetector.detect(inputStream, context, confidenceThreshold, modelName)

            lastResult = result

            // Process and return the results
            return@withContext BraillePostProcessor.processDetections(
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

    fun getDetectedBoxes(): List<DetectedBox> {
        val result = lastResult ?: return emptyList()
        val boxes = mutableListOf<DetectedBox>()

        // Get scaling info
        val scaleFactor = ObjectDetector.getScaleFactor()
        val offsetX = ObjectDetector.getOffsetX()
        val offsetY = ObjectDetector.getOffsetY()

        // Get preprocessed bitmap
        val preprocessedBitmap = ObjectDetector.getPreprocessedInputBitmap() ?: return emptyList()

        // Calculate content dimensions
        val contentWidth = preprocessedBitmap.width - (2 * offsetX)
        val contentHeight = preprocessedBitmap.height - (2 * offsetY)

        // Calculate original image dimensions
        val originalWidth = (contentWidth / scaleFactor).toInt().coerceAtLeast(1)
        val originalHeight = (contentHeight / scaleFactor).toInt().coerceAtLeast(1)

        for (i in 0 until result.outputBox.size) {
            val box = result.outputBox[i]

            // Get normalized coordinates (0-1) with explicit type casting
            val normX = box[0].toFloat()
            val normY = box[1].toFloat()
            val normWidth = box[2].toFloat()
            val normHeight = box[3].toFloat()
            val confidence = box[4].toFloat()
            val classId = box[5].toInt()

            // Convert to image coordinates
            val x = normX * originalWidth
            val y = normY * originalHeight
            val width = normWidth * originalWidth
            val height = normHeight * originalHeight

            // Determine grade and meaning
            val grade: Int
            val actualClassId: Int

            when (currentModel) {
                ObjectDetector.MODEL_G2 -> {
                    grade = 2
                    actualClassId = classId
                }
                ObjectDetector.BOTH_MODELS -> {
                    val isG2 = classId >= BothModelsMerger.G2_CLASS_OFFSET
                    grade = if (isG2) 2 else 1
                    actualClassId = if (isG2) classId - BothModelsMerger.G2_CLASS_OFFSET else classId
                }
                else -> {
                    grade = 1
                    actualClassId = classId
                }
            }

            val className = BrailleClassIdMapper.getMeaning(actualClassId, grade) ?: "unknown"

            boxes.add(
                DetectedBox(
                    x = x,
                    y = y,
                    width = width,
                    height = height,
                    className = className,
                    classId = actualClassId,
                    confidence = confidence
                )
            )
        }

        return boxes
    }
}