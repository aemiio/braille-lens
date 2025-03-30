package com.aemiio.braillelens.utils

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.aemiio.braillelens.ui.BrailleLensColors
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassSelector(
    classOptions: List<String>,
    currentClass: String,
    onClassChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // Force the selector to update when currentClass changes
    var displayValue by remember(currentClass) { mutableStateOf(currentClass) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = displayValue,
                onValueChange = {},
                readOnly = true,
                label = { Text("Braille Class") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrailleLensColors.darkOlive,
                    unfocusedBorderColor = BrailleLensColors.accentBeige
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                classOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            displayValue = option
                            onClassChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

fun constrainBoxToCanvas(box: DetectedBox, canvasSize: Size, bitmap: Bitmap?): DetectedBox {
    if (bitmap == null || canvasSize.width <= 0 || canvasSize.height <= 0) return box

    // Calculate bitmap-to-canvas scale factors
    val scaleX = canvasSize.width / bitmap.width.toFloat()
    val scaleY = canvasSize.height / bitmap.height.toFloat()

    // Get box edges in bitmap coordinates
    val halfWidth = box.width / 2
    val halfHeight = box.height / 2
    val left = box.x - halfWidth
    val right = box.x + halfWidth
    val top = box.y - halfHeight
    val bottom = box.y + halfHeight

    // Calculate constrained values
    val constrainedLeft = left.coerceAtLeast(0f)
    val constrainedRight = right.coerceAtMost(bitmap.width.toFloat())
    val constrainedTop = top.coerceAtLeast(0f)
    val constrainedBottom = bottom.coerceAtMost(bitmap.height.toFloat())

    // Calculate new center coordinates and dimensions
    val newWidth = (constrainedRight - constrainedLeft).coerceAtLeast(10f)
    val newHeight = (constrainedBottom - constrainedTop).coerceAtLeast(10f)
    val newX = constrainedLeft + (newWidth / 2)
    val newY = constrainedTop + (newHeight / 2)

    return box.copy(x = newX, y = newY, width = newWidth, height = newHeight)
}