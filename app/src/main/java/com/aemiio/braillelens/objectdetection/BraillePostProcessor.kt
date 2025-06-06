package com.aemiio.braillelens.objectdetection

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import kotlin.math.max

data class ProcessedDetectionResult(
    val displayBitmap: Bitmap,
    val detectionText: String,
    val translatedText: String
)

object BraillePostProcessor {
    private const val TAG = "BraillePostProcessor"

    // State tracking variable
    private var processingState = false

    // Reset state between detections
    fun resetStates() {
        processingState = false
    }

    fun processDetections(
        result: Result,
        currentModel: String,
        classes: List<String>
    ): ProcessedDetectionResult {
        Log.d(TAG, "Processing ${result.outputBox.size} detections")

        // Get scaling info from ObjectDetector
        val scaleFactor = ObjectDetector.getScaleFactor()
        val offsetX = ObjectDetector.getOffsetX()
        val offsetY = ObjectDetector.getOffsetY()

        // Get the preprocessed input bitmap
        val preprocessedBitmap = ObjectDetector.getPreprocessedInputBitmap()
            ?: result.outputBitmap

        // Create annotated result bitmap
        val fullResultBitmap = createAnnotatedResultBitmap(result, offsetX, offsetY, currentModel)

        // Calculate content dimensions (non-padded area)
        val contentWidth = preprocessedBitmap.width - (2 * offsetX)
        val contentHeight = preprocessedBitmap.height - (2 * offsetY)

        // Crop the result bitmap to remove padding
        val displayBitmap = Bitmap.createBitmap(
            fullResultBitmap,
            offsetX,
            offsetY,
            contentWidth,
            contentHeight
        )

        // Calculate original image dimensions
        val originalWidth = max(1, (contentWidth / scaleFactor).toInt())
        val originalHeight = max(1, (contentHeight / scaleFactor).toInt())

        // Process detections for text output
        val processedDetections = mutableListOf<Map<String, Any>>()
        val classDetailsMap = mutableListOf<Map<String, String>>()

        // Use direct index-based iteration
        for (i in 0 until result.outputBox.size) {
            val box = result.outputBox[i]

            val detection = BrailleResult.processRawDetection(
                box,
                originalWidth.toFloat(),
                originalHeight.toFloat()
            )

            val classId = detection["classId"] as Int
            val classDetails = BrailleResult.getClassDetails(
                classId,
                currentModel,
                classes
            )

            processedDetections.add(detection)
            classDetailsMap.add(classDetails)
        }

        // Format detection results for text display
        val brailleFormatter = BrailleFormatter()
        val cells = brailleFormatter.convertToBrailleCells(
            processedDetections,
            classDetailsMap
        )

        val sortedLines = brailleFormatter.organizeCellsByLines(cells)
        val sortedCells = sortedLines.flatten()

        val detectionText = brailleFormatter.formatDetectionResults(sortedCells)
        val translatedText = brailleFormatter.formatTranslatedText(sortedCells, currentModel)

        return ProcessedDetectionResult(
            displayBitmap = displayBitmap,
            detectionText = detectionText,
            translatedText = translatedText
        )
    }

    private fun createAnnotatedResultBitmap(
        result: Result,
        offsetX: Int,
        offsetY: Int,
        currentModel: String
    ): Bitmap {
        val inputSize = ObjectDetector.INPUT_SIZE
        val preprocessedBitmap = ObjectDetector.getPreprocessedInputBitmap()
            ?: return result.outputBitmap.copy(Bitmap.Config.ARGB_8888, false)

        val resultBitmap = preprocessedBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)

        // Paint for detection boxes
        val boxPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            setShadowLayer(2f, 0f, 0f, Color.BLACK)
            typeface = Typeface.DEFAULT_BOLD
        }

        // Draw boxes with braille character labels
        for (i in 0 until result.outputBox.size) {
            val box = result.outputBox[i]

            // Use the stored model space coordinates with explicit access
            val modelX = if (box.size > 6) box[6] else (box[0] * ObjectDetector.getScaleFactor() + offsetX)
            val modelY = if (box.size > 7) box[7] else (box[1] * ObjectDetector.getScaleFactor() + offsetY)
            val modelW = if (box.size > 8) box[8] else (box[2] * ObjectDetector.getScaleFactor())
            val modelH = if (box.size > 9) box[9] else (box[3] * ObjectDetector.getScaleFactor())

            val left = modelX - modelW/2
            val top = modelY - modelH/2
            val right = modelX + modelW/2
            val bottom = modelY + modelH/2

            canvas.drawRect(left, top, right, bottom, boxPaint)

            // Get meaningful braille character name
            val classId = box[5].toInt()
            val conf = box[4]

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

            val meaning = BrailleClassIdMapper.getMeaning(actualClassId, grade)
            val label = "$meaning: ${(conf * 100).toInt()}%"
            canvas.drawText(label, left, top - 5f, textPaint)
        }

        return resultBitmap
    }
}