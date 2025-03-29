package com.aemiio.braillelens.utils

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.aemiio.braillelens.ui.screens.DetectedBox

object AnnotationState {
    // Use mutableStateOf for direct state management
    val boxes = mutableStateListOf<DetectedBox>()
    val originalBitmap = mutableStateOf<Bitmap?>(null)
    val selectedModel = mutableStateOf("Grade 1 Braille")
    val imagePath = mutableStateOf("")

    // Update boxes without delay
    fun updateBoxes(newBoxes: List<DetectedBox>) {
        boxes.clear()
        boxes.addAll(newBoxes)
        println("Boxes updated directly: ${boxes.size}")
    }


    fun addBox(box: DetectedBox) {
        boxes.add(box)
        println("Box added directly: ${boxes.size}")
    }


    fun removeBox(index: Int) {
        if (index in 0 until boxes.size) {
            boxes.removeAt(index)
            println("Box removed directly: ${boxes.size}")
        }
    }


    fun updateBox(index: Int, box: DetectedBox) {
        if (index in 0 until boxes.size) {
            boxes[index] = box
            println("Box updated directly at index $index")
        }
    }


    fun setDetectionResults(
        detectedBoxes: List<DetectedBox>,
        bitmap: Bitmap?,
        model: String,
        path: String
    ) {
        boxes.clear()
        boxes.addAll(detectedBoxes)
        originalBitmap.value = bitmap
        selectedModel.value = model
        imagePath.value = path
        println("Detection results set directly: ${boxes.size} boxes")
    }


    fun normalizeAllBoxes(canvasWidth: Float, canvasHeight: Float) {
        if (boxes.isEmpty()) return
        
        // Get the range of box coordinates
        val minX = boxes.minOfOrNull { it.x - it.width/2 } ?: 0f
        val minY = boxes.minOfOrNull { it.y - it.height/2 } ?: 0f
        val maxX = boxes.maxOfOrNull { it.x + it.width/2 } ?: 1f
        val maxY = boxes.maxOfOrNull { it.y + it.height/2 } ?: 1f
        
        val boxesWidth = maxX - minX
        val boxesHeight = maxY - minY
        
        println("DEBUG: Box coordinate ranges: x=$minX-$maxX, y=$minY-$maxY")
        
        // Calculate scale and offset to fit in canvas
        val scaleX = canvasWidth * 0.9f / boxesWidth
        val scaleY = canvasHeight * 0.9f / boxesHeight
        val scale = minOf(scaleX, scaleY)
        
        val offsetX = (canvasWidth - boxesWidth * scale) / 2
        val offsetY = (canvasHeight - boxesHeight * scale) / 2
        
        println("DEBUG: Normalizing with scale=$scale, offset=($offsetX,$offsetY)")
        

        val normalizedBoxes = boxes.map { box ->
            val normalizedBox = DetectedBox(
                x = (box.x - minX) * scale + offsetX,
                y = (box.y - minY) * scale + offsetY,
                width = box.width * scale,
                height = box.height * scale,
                className = box.className,
                classId = box.classId,
                confidence = box.confidence
            )
            println("DEBUG: Normalized: (${box.x},${box.y}) -> (${normalizedBox.x},${normalizedBox.y})")
            normalizedBox
        }
        

        boxes.clear()
        boxes.addAll(normalizedBoxes)
        println("DEBUG: All boxes normalized to fit canvas")
    }
}