package com.aemiio.braillelens.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.aemiio.braillelens.ui.screens.AnnotationMode
import com.aemiio.braillelens.ui.screens.DetectedBox
import kotlin.math.abs

@Composable
fun AnnotationCanvas(
    bitmap: Bitmap?,
    boxes: List<DetectedBox>,
    annotationMode: AnnotationMode,
    selectedBox: Int?,
    isDrawing: Boolean,
    startPoint: Offset,
    endPoint: Offset,
    onBoxSelect: (Int?) -> Unit,
    onBoxAdd: (DetectedBox) -> Unit,
    onBoxDelete: (Int) -> Unit,
    onStartDrawing: (Offset) -> Unit,
    onDrawing: (Offset) -> Unit,
    onEndDrawing: () -> Unit,
    currentClass: String,
    onCanvasSizeChanged: (Size) -> Unit
) {
    // Remember last tap time for double-tap detection
    var lastTapTime by remember { mutableStateOf(0L) }
    // Remember box boundaries for better debugging/visualization
    var lastTapPoint by remember { mutableStateOf<Offset?>(null) }

    // Cache for box boundaries to ensure drawing and hit detection use the same values
    val boxDrawBounds = remember { mutableMapOf<Int, Triple<Offset, Size, Boolean>>() }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(bitmap?.width?.toFloat()?.div(bitmap?.height?.toFloat() ?: 1f) ?: 1f)
            .pointerInput(annotationMode) {
                when (annotationMode) {
                    AnnotationMode.ADD -> {
                        detectDragGestures(
                            onDragStart = { offset ->
                                onStartDrawing(offset)
                                println("DEBUG: Drag started at ($offset)")
                                println("DEBUG: Canvas size: ${size.width} x ${size.height}, Bitmap size: ${bitmap?.width} x ${bitmap?.height}")
                            },
                            onDrag = { change, _ ->
                                onDrawing(change.position)
                                println("DEBUG: Dragging at (${change.position})")
                            },
                            onDragEnd = {
                                println("DEBUG: Drag ended. Start: ($startPoint), End: ($endPoint)")
                                onEndDrawing()
                            }
                        )
                    }

                    AnnotationMode.EDIT, AnnotationMode.DELETE, AnnotationMode.VIEW -> {
                        detectTapGestures { offset ->
                            // Remember the tap point for debugging
                            lastTapPoint = offset
                            println("DEBUG: Tap detected at $offset")

                            bitmap?.let { bmp ->
                                // Use the cached box bounds for hit detection instead of recalculating
                                var tappedBoxIndex: Int? = null

                                // Process boxes in reverse order (top-most first)
                                for (i in boxes.indices.reversed()) {
                                    val boxData = boxDrawBounds[i]
                                    if (boxData == null) continue

                                    val topLeft = boxData.first
                                    val size = boxData.second

                                    val boxLeft = topLeft.x
                                    val boxTop = topLeft.y
                                    val boxRight = boxLeft + size.width
                                    val boxBottom = boxTop + size.height

                                    println("DEBUG: Box $i EXACT bounds: L=$boxLeft, T=$boxTop, R=$boxRight, B=$boxBottom")

                                    if (offset.x >= boxLeft && offset.x <= boxRight &&
                                        offset.y >= boxTop && offset.y <= boxBottom
                                    ) {
                                        tappedBoxIndex = i
                                        println("DEBUG: Hit detected on Box $i at tap ($offset)")

                                        // For immediate action in DELETE mode
                                        if (annotationMode == AnnotationMode.DELETE && selectedBox == i) {
                                            println("DEBUG: Delete action on box $i")
                                            onBoxDelete(i)
                                            onBoxSelect(null)
                                            return@detectTapGestures
                                        }
                                        break
                                    }
                                }

                                // Handle tap actions based on mode
                                when (annotationMode) {
                                    AnnotationMode.DELETE -> {
                                        if (tappedBoxIndex != null) {
                                            onBoxSelect(tappedBoxIndex)
                                        } else {
                                            onBoxSelect(null)
                                        }
                                    }

                                    AnnotationMode.EDIT -> {
                                        println("DEBUG: EDIT mode tap handled: ${tappedBoxIndex ?: "no box"}")
                                        onBoxSelect(tappedBoxIndex)
                                    }

                                    else -> onBoxSelect(tappedBoxIndex)
                                }
                            }
                        }
                    }
                }
            }
    ) {
        onCanvasSizeChanged(size)

        bitmap?.let { bmp ->
            drawContext.canvas.nativeCanvas.drawBitmap(
                bmp,
                null,
                android.graphics.Rect(0, 0, size.width.toInt(), size.height.toInt()),
                null
            )

            // Clear the previous box bounds
            boxDrawBounds.clear()

            // Calculate canvas-to-bitmap scale factors
            val canvasScaleX = size.width / bmp.width
            val canvasScaleY = size.height / bmp.height
            println("DEBUG: Canvas scale factors: X=$canvasScaleX, Y=$canvasScaleY")

            boxes.forEachIndexed { index, box ->
                val scaledX = box.x * canvasScaleX
                val scaledY = box.y * canvasScaleY
                val scaledWidth = box.width * canvasScaleX
                val scaledHeight = box.height * canvasScaleY

                val left = scaledX - scaledWidth / 2
                val top = scaledY - scaledHeight / 2

                // Store the exact draw bounds for hit detection
                boxDrawBounds[index] = Triple(
                    Offset(left, top),
                    Size(scaledWidth, scaledHeight),
                    index == selectedBox
                )

                println("DEBUG: Box $index: Original=(${box.x},${box.y}), Scaled=($scaledX,$scaledY), size=${scaledWidth}x${scaledHeight}")
                println("DEBUG: Box $index ACTUAL draw bounds: L=$left, T=$top, R=${left + scaledWidth}, B=${top + scaledHeight}")

                val isSelected = index == (selectedBox ?: -1)

                val boxColor = when {
                    isSelected && annotationMode == AnnotationMode.EDIT -> Color.Blue
                    isSelected && annotationMode == AnnotationMode.DELETE -> Color.Red
                    isSelected -> Color.Yellow
                    else -> Color.Green
                }

                val strokeWidth = if (isSelected) 3.dp.toPx() else 2.dp.toPx()

                // Draw the box
                drawRect(
                    color = boxColor,
                    topLeft = Offset(left, top),
                    size = Size(scaledWidth, scaledHeight),
                    style = Stroke(width = strokeWidth)
                )

                // Draw the class name
                drawContext.canvas.nativeCanvas.drawText(
                    box.className,
                    left,
                    top - 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.YELLOW
                        textSize = 40f
                        isAntiAlias = true
                        setShadowLayer(3f, 1f, 1f, android.graphics.Color.BLACK)
                    }
                )

                // Draw a selection indicator
                if (isSelected) {
                    drawCircle(
                        color = boxColor,
                        radius = 8f,
                        center = Offset(scaledX, scaledY)
                    )
                }
            }

            // Debug visualization: draw last tap point
            if (lastTapPoint != null) {
                drawCircle(
                    color = Color.Red,
                    radius = 10f,
                    center = lastTapPoint!!,
                    alpha = 0.7f
                )
            }

            if (isDrawing) {
                val left = minOf(startPoint.x, endPoint.x)
                val top = minOf(startPoint.y, endPoint.y)
                val width = abs(endPoint.x - startPoint.x)
                val height = abs(endPoint.y - startPoint.y)

                println("DEBUG: Drawing temp box: left=$left, top=$top, width=$width, height=$height")

                drawRect(
                    color = Color.Red,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = 3.dp.toPx())
                )

                val centerX = left + width / 2
                val centerY = top + height / 2
                drawCircle(
                    color = Color.Yellow,
                    radius = 5f,
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
}