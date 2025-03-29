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

    // Add a single box
    fun addBox(box: DetectedBox) {
        boxes.add(box)
        println("Box added directly: ${boxes.size}")
    }

    // Remove a box
    fun removeBox(index: Int) {
        if (index in 0 until boxes.size) {
            boxes.removeAt(index)
            println("Box removed directly: ${boxes.size}")
        }
    }

    // Update a box
    fun updateBox(index: Int, box: DetectedBox) {
        if (index in 0 until boxes.size) {
            boxes[index] = box
            println("Box updated directly at index $index")
        }
    }

    // Set detection results
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

    // Clear data
    fun clearData() {
        boxes.clear()
        originalBitmap.value = null
        selectedModel.value = "Grade 1 Braille"
        imagePath.value = ""
    }
}