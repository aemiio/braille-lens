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


class ObjectDetectionService {
    private val objDetector = ObjectDetector()

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


        BrailleClassIdMapper.loadMappingsFromResources()

        BraillePostProcessor.resetStates()

        val modelName = mapDetectionModeToModel(detectionMode)
        val classes = readClasses(context, modelName)


        val boxPaint = createBoxPaint()
        val textPaint = createTextPaint()

        try {

            currentModel = modelName

            val result = objDetector.detect(inputStream, context, confidenceThreshold, modelName)

            lastResult = result


            return@withContext BraillePostProcessor.processDetections(
                result = result,
                currentModel = modelName,
                classes = classes
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

        BrailleClassIdMapper.loadMappingsFromResources()


        BraillePostProcessor.resetStates()

        val modelName = mapDetectionModeToModel(detectionMode)
        val classes = readClasses(context, modelName)


        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)


        val boxPaint = createBoxPaint()
        val textPaint = createTextPaint()

        try {

            val inputStream = bitmap.let {
                val stream = java.io.ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.PNG, 100, stream)
                java.io.ByteArrayInputStream(stream.toByteArray())
            }

            currentModel = modelName


            val result = objDetector.detect(inputStream, context, confidenceThreshold, modelName)

            lastResult = result


            return@withContext BraillePostProcessor.processDetections(
                result = result,
                currentModel = modelName,
                classes = classes
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

    // get the boxes exactly as they are drawn on the display bitmap
    fun getResultBitmapBoxes(displayBitmap: Bitmap?): List<DetectedBox> {
        val result = lastResult ?: return emptyList()
        val boxes = mutableListOf<DetectedBox>()
        
        // Get the drawing parameters
        val offsetX = ObjectDetector.getOffsetX()
        val offsetY = ObjectDetector.getOffsetY()
        val scaleFactor = ObjectDetector.getScaleFactor()
        
        // Get display bitmap dimensions
        val displayWidth = displayBitmap?.width ?: return emptyList()
        val displayHeight = displayBitmap?.height ?: return emptyList()
        
        // For each box detected
        for (i in 0 until result.outputBox.size) {
            val box = result.outputBox[i]
            
            // Get confidence and class ID
            val confidence = box[4].toFloat()
            val classId = box[5].toInt()
            
            // In PostProcessor, boxes are drawn at these coordinates
            // These are indices 6-9 in the output array, or calculated from indices 0-3
            val modelX = if (box.size > 6) box[6] else (box[0] * scaleFactor + offsetX)
            val modelY = if (box.size > 7) box[7] else (box[1] * scaleFactor + offsetY)
            val modelW = if (box.size > 8) box[8] else (box[2] * scaleFactor)
            val modelH = if (box.size > 9) box[9] else (box[3] * scaleFactor)
            
            // Calculate box coordinates in the non-padded display bitmap
            // This exactly matches how they're drawn in BraillePostProcessor
            val displayX = modelX - offsetX
            val displayY = modelY - offsetY
            val displayW = modelW
            val displayH = modelH
            
            // Determine grade and class name
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
            
            // Create box with the exact coordinates as shown in the result screen
            boxes.add(
                DetectedBox(
                    x = displayX,
                    y = displayY,
                    width = displayW,
                    height = displayH,
                    className = className,
                    classId = actualClassId,
                    confidence = confidence
                )
            )
            
            println("DEBUG: Result box $i: x=$displayX, y=$displayY, w=$displayW, h=$displayH")
        }
        
        println("DEBUG: Created ${boxes.size} boxes with display bitmap coordinates")
        return boxes
    }

    // get a clean copy of the displayBitmap (without boxes)
    fun getCleanDisplayBitmap(): Bitmap? {
        val result = lastResult ?: return null
        val preprocessedBitmap = ObjectDetector.getPreprocessedInputBitmap() ?: return null
        
        // Get the offsets used for padding
        val offsetX = ObjectDetector.getOffsetX()
        val offsetY = ObjectDetector.getOffsetY()
        
        // Calculate content dimensions (non-padded area)
        val contentWidth = preprocessedBitmap.width - (2 * offsetX)
        val contentHeight = preprocessedBitmap.height - (2 * offsetY)
        
        // Create a clean crop of the preprocessed bitmap (without any boxes drawn)
        return Bitmap.createBitmap(
            preprocessedBitmap,
            offsetX,
            offsetY,
            contentWidth,
            contentHeight
        )
    }
}