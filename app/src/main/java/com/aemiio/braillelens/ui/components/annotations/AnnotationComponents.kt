package com.aemiio.braillelens.ui.components.annotations

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aemiio.braillelens.ui.BrailleLensColors
import com.aemiio.braillelens.ui.screens.AnnotationMode
import com.aemiio.braillelens.ui.screens.DetectedBox
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                Rect(0, 0, size.width.toInt(), size.height.toInt()),
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
                    Paint().apply {
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

@Composable
fun BoxDetailsCard(
    box: DetectedBox,
    currentMode: AnnotationMode,
    selectedBox: Int,
    boxes: List<DetectedBox>,
    onBoxDelete: (Int) -> Unit,
    onBoxUpdate: (Int, DetectedBox) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(
                color = when (currentMode) {
                    AnnotationMode.EDIT -> MaterialTheme.colorScheme.onSecondaryContainer
                    AnnotationMode.DELETE -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                },
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header section with box details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Information
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Box Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        "Class: ${box.className}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        "Position: (${box.x.toInt()}, ${box.y.toInt()}) • Size: ${box.width.toInt()}×${box.height.toInt()}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Right side: Only show Delete button in DELETE mode
                if (currentMode == AnnotationMode.DELETE) {
                    Button(
                        onClick = { onBoxDelete(selectedBox) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrailleLensColors.accentRed
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Box",
                            tint = BrailleLensColors.fontWhite
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 14.sp, color = BrailleLensColors.fontWhite)
                    }
                }
            }

            // Only show controls in EDIT mode
            if (currentMode == AnnotationMode.EDIT) {
                // Add a divider
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black.copy(alpha = 0.1f))
                )

                // Size Controls and Position Controls side by side in 2 columns
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // LEFT COLUMN: Size Controls
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Size Controls",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Width control with direct long press detection
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                "Width:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(52.dp)  // Fixed width for alignment
                            )
                            
                            // Decrease width button with long press
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .pointerInput(selectedBox) {
                                        detectTapGestures(
                                            onPress = { offset ->
                                                if (selectedBox < boxes.size) {
                                                    // Initial update
                                                    val currentBox = boxes[selectedBox]
                                                    val newWidth = (currentBox.width - 5).coerceAtLeast(10f)
                                                    onBoxUpdate(selectedBox, currentBox.copy(width = newWidth))
                                                    
                                                    // Continuous updates
                                                    val job = coroutineScope.launch {
                                                        delay(400)
                                                        while (true) {
                                                            val latestBox = boxes[selectedBox]
                                                            val latestNewWidth = (latestBox.width - 5).coerceAtLeast(10f)
                                                            onBoxUpdate(selectedBox, latestBox.copy(width = latestNewWidth))
                                                            delay(100)
                                                        }
                                                    }
                                                    
                                                    tryAwaitRelease()
                                                    job.cancel()
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "−",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Text(
                                "${box.width.toInt()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(30.dp),
                                textAlign = TextAlign.Center
                            )
                            
                            // Increase width button with long press
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .pointerInput(selectedBox) {
                                        detectTapGestures(
                                            onPress = { offset ->
                                                if (selectedBox < boxes.size) {
                                                    // Initial update
                                                    val currentBox = boxes[selectedBox]
                                                    onBoxUpdate(selectedBox, currentBox.copy(width = currentBox.width + 5))
                                                    
                                                    // Continuous updates
                                                    val job = coroutineScope.launch {
                                                        delay(400)
                                                        while (true) {
                                                            val latestBox = boxes[selectedBox]
                                                            onBoxUpdate(selectedBox, latestBox.copy(width = latestBox.width + 5))
                                                            delay(100)
                                                        }
                                                    }
                                                    
                                                    tryAwaitRelease()
                                                    job.cancel()
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Width",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        // Height control with direct long press detection
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                "Height:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(52.dp)  // Fixed width for alignment
                            )
                            
                            // Decrease height button with long press
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .pointerInput(selectedBox) {
                                        detectTapGestures(
                                            onPress = { offset ->
                                                if (selectedBox < boxes.size) {
                                                    // Initial update
                                                    val currentBox = boxes[selectedBox]
                                                    val newHeight = (currentBox.height - 5).coerceAtLeast(10f)
                                                    onBoxUpdate(selectedBox, currentBox.copy(height = newHeight))
                                                    
                                                    // Continuous updates
                                                    val job = coroutineScope.launch {
                                                        delay(400)
                                                        while (true) {
                                                            val latestBox = boxes[selectedBox]
                                                            val latestNewHeight = (latestBox.height - 5).coerceAtLeast(10f)
                                                            onBoxUpdate(selectedBox, latestBox.copy(height = latestNewHeight))
                                                            delay(100)
                                                        }
                                                    }
                                                    
                                                    tryAwaitRelease()
                                                    job.cancel()
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "−",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Text(
                                "${box.height.toInt()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(30.dp),
                                textAlign = TextAlign.Center
                            )
                            
                            // Increase height button with long press
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .pointerInput(selectedBox) {
                                        detectTapGestures(
                                            onPress = { offset ->
                                                if (selectedBox < boxes.size) {
                                                    // Initial update
                                                    val currentBox = boxes[selectedBox]
                                                    onBoxUpdate(selectedBox, currentBox.copy(height = currentBox.height + 5))
                                                    
                                                    // Continuous updates
                                                    val job = coroutineScope.launch {
                                                        delay(400)
                                                        while (true) {
                                                            val latestBox = boxes[selectedBox]
                                                            onBoxUpdate(selectedBox, latestBox.copy(height = latestBox.height + 5))
                                                            delay(100)
                                                        }
                                                    }
                                                    
                                                    tryAwaitRelease()
                                                    job.cancel()
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Height",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Vertical divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(120.dp)
                            .background(Color.Black.copy(alpha = 0.1f))
                    )

                    // RIGHT COLUMN: Position Controls
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Position Controls",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Movement controls in a compact directional pad layout
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Up button with long press
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .pointerInput(selectedBox) {
                                        detectTapGestures(
                                            onPress = { offset ->
                                                if (selectedBox < boxes.size) {
                                                    // Initial press movement
                                                    val currentBox = boxes[selectedBox]
                                                    onBoxUpdate(selectedBox, currentBox.copy(y = currentBox.y - 5))
                                                    
                                                    // Start repeating job
                                                    val job = coroutineScope.launch {
                                                        delay(400) // Initial delay before repeating
                                                        while (true) {
                                                            // Always get the latest box data
                                                            val latestBox = boxes[selectedBox]
                                                            onBoxUpdate(selectedBox, latestBox.copy(y = latestBox.y - 5))
                                                            delay(100) // Repeat interval
                                                        }
                                                    }
                                                    
                                                    // Wait for release and cancel job
                                                    tryAwaitRelease()
                                                    job.cancel()
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Move Up",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Left button with long press
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .pointerInput(selectedBox) {
                                            detectTapGestures(
                                                onPress = { offset ->
                                                    if (selectedBox < boxes.size) {
                                                        // Initial movement
                                                        val currentBox = boxes[selectedBox]
                                                        onBoxUpdate(selectedBox, currentBox.copy(x = currentBox.x - 5))
                                                        
                                                        // Start repeating job
                                                        val job = coroutineScope.launch {
                                                            delay(400)
                                                            while (true) {
                                                                val latestBox = boxes[selectedBox]
                                                                onBoxUpdate(selectedBox, latestBox.copy(x = latestBox.x - 5))
                                                                delay(100)
                                                            }
                                                        }
                                                        
                                                        tryAwaitRelease()
                                                        job.cancel()
                                                    }
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "Move Left",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                )
                                
                                // Right button with long press
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .pointerInput(selectedBox) {
                                            detectTapGestures(
                                                onPress = { offset ->
                                                    if (selectedBox < boxes.size) {
                                                        // Initial movement
                                                        val currentBox = boxes[selectedBox]
                                                        onBoxUpdate(selectedBox, currentBox.copy(x = currentBox.x + 5))
                                                        
                                                        // Start repeating job
                                                        val job = coroutineScope.launch {
                                                            delay(400)
                                                            while (true) {
                                                                val latestBox = boxes[selectedBox]
                                                                onBoxUpdate(selectedBox, latestBox.copy(x = latestBox.x + 5))
                                                                delay(100)
                                                            }
                                                        }
                                                        
                                                        tryAwaitRelease()
                                                        job.cancel()
                                                    }
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Move Right",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            // Down button with long press
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .pointerInput(selectedBox) {
                                        detectTapGestures(
                                            onPress = { offset ->
                                                if (selectedBox < boxes.size) {
                                                    // Initial movement
                                                    val currentBox = boxes[selectedBox]
                                                    onBoxUpdate(selectedBox, currentBox.copy(y = currentBox.y + 5))
                                                    
                                                    // Start repeating job
                                                    val job = coroutineScope.launch {
                                                        delay(400)
                                                        while (true) {
                                                            val latestBox = boxes[selectedBox]
                                                            onBoxUpdate(selectedBox, latestBox.copy(y = latestBox.y + 5))
                                                            delay(100)
                                                        }
                                                    }
                                                    
                                                    tryAwaitRelease()
                                                    job.cancel()
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Move Down",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}