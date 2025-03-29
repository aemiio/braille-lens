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


        BrailleClassIdMapper.loadMappingsFromResources(context)

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

        BrailleClassIdMapper.loadMappingsFromResources(context)


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


        val scaleFactor = ObjectDetector.getScaleFactor()
        val offsetX = ObjectDetector.getOffsetX()
        val offsetY = ObjectDetector.getOffsetY()


        val preprocessedBitmap = ObjectDetector.getPreprocessedInputBitmap() ?: return emptyList()


        val contentWidth = preprocessedBitmap.width - (2 * offsetX)
        val contentHeight = preprocessedBitmap.height - (2 * offsetY)


        val originalWidth = (contentWidth / scaleFactor).toInt().coerceAtLeast(1)
        val originalHeight = (contentHeight / scaleFactor).toInt().coerceAtLeast(1)

        for (i in 0 until result.outputBox.size) {
            val box = result.outputBox[i]


            val normX = box[0].toFloat()
            val normY = box[1].toFloat()
            val normWidth = box[2].toFloat()
            val normHeight = box[3].toFloat()
            val confidence = box[4].toFloat()
            val classId = box[5].toInt()


            val x = normX * originalWidth
            val y = normY * originalHeight
            val width = normWidth * originalWidth
            val height = normHeight * originalHeight


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

//    fun getCanvasScaledBoxes(canvasWidth: Float, canvasHeight: Float): List<DetectedBox> {
//        val rawBoxes = getDetectedBoxes()
//
//        // Transform the boxes to fit the canvas
//        return rawBoxes.map { box ->
//            // Create a new box with reasonable canvas coordinates
//            DetectedBox(
//                x = box.x * canvasWidth / 640f,  // Scale to canvas width
//                y = box.y * canvasHeight / 480f, // Scale to canvas height
//                width = box.width * canvasWidth / 640f,
//                height = box.height * canvasHeight / 480f,
//                className = box.className,
//                classId = box.classId,
//                confidence = box.confidence
//            )
//        }
//    }
//
//    fun getDisplayBoxes(displayBitmap: Bitmap?): List<DetectedBox> {
//        if (displayBitmap == null || lastResult == null) return emptyList()
//        val result = lastResult ?: return emptyList()
//        val boxes = mutableListOf<DetectedBox>()
//
//        // When drawing on the display bitmap, these coordinates were used:
//        for (i in 0 until result.outputBox.size) {
//            val box = result.outputBox[i]
//
//            // Get the confidence and class ID
//            val confidence = box[4].toFloat()
//            val classId = box[5].toInt()
//
//            // These are the coordinates used in BraillePostProcessor
//            // to draw the boxes on the display bitmap
//            val modelX = if (box.size > 6) box[6] else (box[0] * ObjectDetector.getScaleFactor() + ObjectDetector.getOffsetX())
//            val modelY = if (box.size > 7) box[7] else (box[1] * ObjectDetector.getScaleFactor() + ObjectDetector.getOffsetY())
//            val modelW = if (box.size > 8) box[8] else (box[2] * ObjectDetector.getScaleFactor())
//            val modelH = if (box.size > 9) box[9] else (box[3] * ObjectDetector.getScaleFactor())
//
//            // The display bitmap is cropped to remove padding, so adjust coordinates
//            val offsetX = ObjectDetector.getOffsetX()
//            val offsetY = ObjectDetector.getOffsetY()
//
//            // Calculate center coordinates relative to the display bitmap
//            val x = modelX - offsetX
//            val y = modelY - offsetY
//
//            // Determine grade and meaning
//            val grade: Int
//            val actualClassId: Int
//
//            when (currentModel) {
//                ObjectDetector.MODEL_G2 -> {
//                    grade = 2
//                    actualClassId = classId
//                }
//                ObjectDetector.BOTH_MODELS -> {
//                    val isG2 = classId >= BothModelsMerger.G2_CLASS_OFFSET
//                    grade = if (isG2) 2 else 1
//                    actualClassId = if (isG2) classId - BothModelsMerger.G2_CLASS_OFFSET else classId
//                }
//                else -> {
//                    grade = 1
//                    actualClassId = classId
//                }
//            }
//
//            val className = BrailleClassIdMapper.getMeaning(actualClassId, grade) ?: "unknown"
//
//            // Create box with exact display coordinates
//            boxes.add(
//                DetectedBox(
//                    x = x,
//                    y = y,
//                    width = modelW,
//                    height = modelH,
//                    className = className,
//                    classId = actualClassId,
//                    confidence = confidence
//                )
//            )
//        }
//
//        println("DEBUG: Generated ${boxes.size} display boxes with exact coordinates")
//        return boxes
//    }
//
//    // Alternative method to get exact box coordinates
//    fun getExactBoxCoordinates(): List<DetectedBox> {
//        val result = lastResult ?: return emptyList()
//        val boxes = mutableListOf<DetectedBox>()
//
//        // Get scaling values
//        val offsetX = ObjectDetector.getOffsetX()
//        val offsetY = ObjectDetector.getOffsetY()
//
//        for (i in 0 until result.outputBox.size) {
//            val box = result.outputBox[i]
//
//            // These are the exact box coordinates used when drawing on the result bitmap
//            val modelX = if (box.size > 6) box[6] else (box[0] * ObjectDetector.getScaleFactor() + offsetX)
//            val modelY = if (box.size > 7) box[7] else (box[1] * ObjectDetector.getScaleFactor() + offsetY)
//            val modelW = if (box.size > 8) box[8] else (box[2] * ObjectDetector.getScaleFactor())
//            val modelH = if (box.size > 9) box[9] else (box[3] * ObjectDetector.getScaleFactor())
//
//            val confidence = box[4].toFloat()
//            val classId = box[5].toInt()
//
//            // Get the exact coordinates used to draw the rectangles in BraillePostProcessor
//            val left = modelX - modelW/2
//            val top = modelY - modelH/2
//            val right = modelX + modelW/2
//            val bottom = modelY + modelH/2
//
//            // Calculate center and dimensions
//            val x = left + (right - left) / 2
//            val y = top + (bottom - top) / 2
//            val width = right - left
//            val height = bottom - top
//
//            // Determine grade and class name
//            val grade: Int
//            val actualClassId: Int
//
//            when (currentModel) {
//                ObjectDetector.MODEL_G2 -> {
//                    grade = 2
//                    actualClassId = classId
//                }
//                ObjectDetector.BOTH_MODELS -> {
//                    val isG2 = classId >= BothModelsMerger.G2_CLASS_OFFSET
//                    grade = if (isG2) 2 else 1
//                    actualClassId = if (isG2) classId - BothModelsMerger.G2_CLASS_OFFSET else classId
//                }
//                else -> {
//                    grade = 1
//                    actualClassId = classId
//                }
//            }
//
//            val className = BrailleClassIdMapper.getMeaning(actualClassId, grade) ?: "unknown"
//
//            boxes.add(
//                DetectedBox(
//                    x = x - offsetX,  // Adjust for cropping in display bitmap
//                    y = y - offsetY,  // Adjust for cropping in display bitmap
//                    width = width,
//                    height = height,
//                    className = className,
//                    classId = actualClassId,
//                    confidence = confidence
//                )
//            )
//        }
//
//        println("DEBUG: Created ${boxes.size} boxes with exact coordinates that match the result screen")
//        return boxes
//    }
//
//    // Get raw detection boxes exactly as detected by the model
//    fun getRawDetectionBoxes(): List<DetectedBox> {
//        val result = lastResult ?: return emptyList()
//        val boxes = mutableListOf<DetectedBox>()
//
//        // For each box in the raw detection output
//        for (i in 0 until result.outputBox.size) {
//            val box = result.outputBox[i]
//
//            // Get the classname and confidence
//            val classId = box[5].toInt()
//            val confidence = box[4].toFloat()
//
//            // Determine grade and get proper class name
//            val grade: Int
//            val actualClassId: Int
//
//            when (currentModel) {
//                ObjectDetector.MODEL_G2 -> {
//                    grade = 2
//                    actualClassId = classId
//                }
//                ObjectDetector.BOTH_MODELS -> {
//                    val isG2 = classId >= BothModelsMerger.G2_CLASS_OFFSET
//                    grade = if (isG2) 2 else 1
//                    actualClassId = if (isG2) classId - BothModelsMerger.G2_CLASS_OFFSET else classId
//                }
//                else -> {
//                    grade = 1
//                    actualClassId = classId
//                }
//            }
//
//            val className = BrailleClassIdMapper.getMeaning(actualClassId, grade) ?: "unknown"
//
//            // Create a DetectedBox with coordinates that match what's shown in the result screen
//            boxes.add(
//                DetectedBox(
//                    x = box[0] * 640f,  // Scale to original image width
//                    y = box[1] * 240f,  // Scale to original image height
//                    width = box[2] * 640f,  // Width scaled to original image width
//                    height = box[3] * 240f, // Height scaled to original image height
//                    className = className,
//                    classId = actualClassId,
//                    confidence = confidence
//                )
//            )
//        }
//
//        println("DEBUG: Created ${boxes.size} raw detection boxes")
//        return boxes
//    }

    // Add this method to get the boxes exactly as they are drawn on the display bitmap
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

    // Add this new method to get a clean copy of the displayBitmap (without boxes)
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