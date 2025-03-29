package com.aemiio.braillelens.ui.screens

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aemiio.braillelens.objectdetection.BrailleClassIdMapper
import com.aemiio.braillelens.objectdetection.BrailleMap
import com.aemiio.braillelens.ui.BrailleLensColors
import com.aemiio.braillelens.utils.AnnotationState
import kotlin.div
import kotlin.math.abs
import kotlin.text.contains
import kotlin.text.toFloat

data class DetectedBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val className: String,
    val classId: Int,
    val confidence: Float = 0.0f
)

enum class AnnotationMode {
    VIEW, ADD, EDIT, DELETE
}

@Composable
fun AnnotationScreen(
    navController: NavController,
    imagePath: String,
    onBoxUpdate: (List<DetectedBox>) -> Unit
) {
    val boxes = AnnotationState.boxes
    val originalBitmap = AnnotationState.originalBitmap.value
    val selectedModel = AnnotationState.selectedModel.value

    val grade = when {
        selectedModel.contains("Both") -> 3  // Use 3 to represent both models
        selectedModel.contains("1") -> 1
        selectedModel.contains("2") -> 2
        else -> 1
    }

    var currentMode by remember { mutableStateOf(AnnotationMode.VIEW) }
    var selectedBox by remember { mutableStateOf<Int?>(null) }
    var isDrawing by remember { mutableStateOf(false) }
    var startPoint by remember { mutableStateOf(Offset.Zero) }
    var endPoint by remember { mutableStateOf(Offset.Zero) }
    var currentClass by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Add a new mutable state to store canvas dimensions
    var canvasSize by remember { mutableStateOf(Size(0f, 0f)) }

    fun updateBox(index: Int, updatedBox: DetectedBox) {
        AnnotationState.updateBox(index, updatedBox)
        println("Updated box $index: class=${updatedBox.className}, classId=${updatedBox.classId}")
    }

    fun addBox(newBox: DetectedBox) {
        AnnotationState.addBox(newBox)
        println("Added new box: x=${newBox.x}, y=${newBox.y}, w=${newBox.width}, h=${newBox.height}, class=${newBox.className}")
        println("Current box count: ${AnnotationState.boxes.size}")
    }

    fun deleteBox(index: Int) {
        AnnotationState.removeBox(index)
    }

    // Get class options based on grade
    val classOptions = remember(grade) {
        val g1Meanings = BrailleMap.G1brailleMap.values.map { it.meaning }
        val g2Meanings = BrailleMap.G2brailleMap.values.map { it.meaning }

        val bothModelsMeanings = g1Meanings + g2Meanings

        when (grade) {
            1 -> g1Meanings
            2 -> g2Meanings
            else -> bothModelsMeanings
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Set initial class if available
    LaunchedEffect(classOptions) {
        if (classOptions.isNotEmpty() && currentClass.isEmpty()) {
            currentClass = classOptions[0]
        }
    }

    // Load bitmap from imagePath if needed
    LaunchedEffect(imagePath) {
        if (originalBitmap == null && imagePath.isNotEmpty()) {
            // Path is already in AnnotationState from RecognitionResultScreen
            println("Image path already set in AnnotationState: $imagePath")
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Annotation Editor",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${selectedModel}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode selection buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        currentMode = AnnotationMode.VIEW
                        selectedBox = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentMode == AnnotationMode.VIEW)
                            BrailleLensColors.darkOlive else BrailleLensColors.accentBeige
                    )
                ) {
                    Text(
                        "View",
                        color = if (currentMode == AnnotationMode.VIEW) Color.White else Color.Black
                    )
                }

                Button(
                    onClick = {
                        currentMode = AnnotationMode.ADD
                        selectedBox = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentMode == AnnotationMode.ADD)
                            BrailleLensColors.darkOlive else BrailleLensColors.accentBeige
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Mode",
                        tint = if (currentMode == AnnotationMode.ADD) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Add",
                        color = if (currentMode == AnnotationMode.ADD) Color.White else Color.Black
                    )
                }

                Button(
                    onClick = {
                        currentMode = AnnotationMode.EDIT
                        if (currentMode != AnnotationMode.EDIT) selectedBox = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentMode == AnnotationMode.EDIT)
                            BrailleLensColors.darkOlive else BrailleLensColors.accentBeige
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Mode",
                        tint = if (currentMode == AnnotationMode.EDIT) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Edit",
                        color = if (currentMode == AnnotationMode.EDIT) Color.White else Color.Black
                    )
                }

                Button(
                    onClick = {
                        currentMode = AnnotationMode.DELETE
                        if (currentMode != AnnotationMode.DELETE) selectedBox = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentMode == AnnotationMode.DELETE)
                            BrailleLensColors.darkOlive else BrailleLensColors.accentBeige
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Mode",
                        tint = if (currentMode == AnnotationMode.DELETE) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Delete",
                        color = if (currentMode == AnnotationMode.DELETE) Color.White else Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Class selector
            if (currentMode == AnnotationMode.ADD || (currentMode == AnnotationMode.EDIT && selectedBox != null)) {
                // Store the non-null value in a local variable for safe access
                val currentSelectedBox = selectedBox
                
                ClassSelector(
                    classOptions = classOptions,
                    currentClass = if (currentMode == AnnotationMode.EDIT && currentSelectedBox != null) 
                                  boxes[currentSelectedBox].className 
                               else 
                                  currentClass,
                    onClassChange = { newClass -> 
                        if (currentMode == AnnotationMode.EDIT && currentSelectedBox != null) {
                            // Update the selected box with the new class
                            val box = boxes[currentSelectedBox]
                            val classId = BrailleClassIdMapper.getMeaningToClassId(newClass, grade)
                            val updatedBox = box.copy(className = newClass, classId = classId)
                            updateBox(currentSelectedBox, updatedBox)
                        } else {
                            // Just update current class for ADD mode
                            currentClass = newClass
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Annotation canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        ratio = if (originalBitmap != null) {
                            originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                        } else 1f
                    )
            ) {
                AnnotationCanvas(
                    bitmap = originalBitmap,
                    boxes = boxes,
                    annotationMode = currentMode,
                    selectedBox = selectedBox,
                    isDrawing = isDrawing,
                    startPoint = startPoint,
                    endPoint = endPoint,
                    onBoxSelect = { selectedBox = it },
                    onBoxAdd = { newBox ->
                        val classId = BrailleClassIdMapper.getMeaningToClassId(currentClass, grade)
                        val box = newBox.copy(className = currentClass, classId = classId)
                        addBox(box)
                    },
                    onBoxDelete = { index ->
                        deleteBox(index)
                    },
                    onStartDrawing = { offset ->
                        isDrawing = true
                        startPoint = offset
                        endPoint = offset
                    },
                    onDrawing = { offset ->
                        endPoint = offset
                    },
                    onEndDrawing = {
                        isDrawing = false

                        // Create and add the new box when drawing is complete
                        if (currentMode == AnnotationMode.ADD && originalBitmap != null) {
                            // Now use the stored canvas size
                            val canvasWidth = canvasSize.width
                            val canvasHeight = canvasSize.height
                            
                            // Calculate box dimensions in canvas coordinates
                            val left = minOf(startPoint.x, endPoint.x)
                            val top = minOf(startPoint.y, endPoint.y)
                            val width = abs(endPoint.x - startPoint.x)
                            val height = abs(endPoint.y - startPoint.y)
                            
                            // Calculate center point
                            val centerX = left + width / 2
                            val centerY = top + height / 2

                            println("DEBUG: Drawing box dimensions: left=$left, top=$top, width=$width, height=$height")
                            println("DEBUG: Canvas size: $canvasWidth x $canvasHeight")
                            println("DEBUG: Bitmap size: ${originalBitmap.width} x ${originalBitmap.height}")

                            // Only create the box if it has a reasonable size
                            if (width > 10 && height > 10) {
                                // Create the box using exact canvas coordinates 
                                val box = DetectedBox(
                                    x = centerX,
                                    y = centerY,
                                    width = width,
                                    height = height,
                                    className = currentClass,
                                    classId = BrailleClassIdMapper.getMeaningToClassId(currentClass, grade)
                                )

                                println("DEBUG: Adding box: center=(${box.x}, ${box.y}), size=${box.width}x${box.height}")
                                addBox(box)
                            }
                        }
                    },
                    currentClass = currentClass,
                    onCanvasSizeChanged = { size ->
                        // Store the canvas size when it's reported
                        canvasSize = size
                    }
                )
            }

            // Box details display
            selectedBox?.let { index ->
                if (index in 0 until boxes.size) {
                    val box = boxes[index]
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            "Selected Box Details",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Class: ${box.className}")
                        Text("Position: (${box.x.toInt()}, ${box.y.toInt()})")
                        Text("Size: ${box.width.toInt()} x ${box.height.toInt()}")
                        if (box.confidence > 0) {
                            Text("Confidence: ${(box.confidence * 100).toInt()}%")
                        }
                        
                        // Add instruction text for editing
                        if (currentMode == AnnotationMode.EDIT) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Select a new class from the dropdown above to change this box's label",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        } else if (currentMode == AnnotationMode.DELETE) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap this box again to delete it",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // For Edit mode, add a button to apply changes when a box is selected
            if (currentMode == AnnotationMode.EDIT && selectedBox != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Use safe call with let to handle the non-null case
                        selectedBox?.let { boxIndex ->
                            val box = boxes[boxIndex]
                            val classId = BrailleClassIdMapper.getMeaningToClassId(currentClass, grade)
                            val updatedBox = box.copy(className = currentClass, classId = classId)
                            updateBox(boxIndex, updatedBox)
                            // Optional: provide user feedback
                            snackbarMessage = "Box updated successfully"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrailleLensColors.darkOlive
                    )
                ) {
                    Text("Update Box Class")
                }
            }
        }
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
    val density = LocalDensity.current

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
                            bitmap?.let { bmp ->
                                val scaleX = size.width / bmp.width
                                val scaleY = size.height / bmp.height

                                // Find tapped box
                                var tappedBoxIndex: Int? = null
                                for (i in boxes.indices.reversed()) {
                                    val box = boxes[i]
                                    val boxLeft = (box.x - box.width / 2) * scaleX
                                    val boxTop = (box.y - box.height / 2) * scaleY
                                    val boxRight = boxLeft + box.width * scaleX
                                    val boxBottom = boxTop + box.height * scaleY

                                    if (offset.x in boxLeft..boxRight &&
                                        offset.y in boxTop..boxBottom) {
                                        tappedBoxIndex = i
                                        break
                                    }
                                }

                                if (annotationMode == AnnotationMode.DELETE && tappedBoxIndex != null) {
                                    onBoxDelete(tappedBoxIndex)
                                    onBoxSelect(null)
                                } else {
                                    onBoxSelect(tappedBoxIndex)
                                }
                            }
                        }
                    }
                }
            }
    ) {
        // Report the canvas size as soon as it's known
        onCanvasSizeChanged(size)
        
        bitmap?.let { bmp ->
            // Draw bitmap
            drawContext.canvas.nativeCanvas.drawBitmap(
                bmp,
                null,
                android.graphics.Rect(0, 0, size.width.toInt(), size.height.toInt()),
                null
            )

            // Log canvas and bitmap dimensions for debugging
            println("DEBUG: AnnotationCanvas - Canvas size: ${size.width} x ${size.height}, Bitmap size: ${bmp.width} x ${bmp.height}")
            
            // Calculate canvas-to-bitmap scale factors (for reference only)
            val canvasScaleX = size.width / bmp.width
            val canvasScaleY = size.height / bmp.height
            println("DEBUG: Canvas scale factors: X=$canvasScaleX, Y=$canvasScaleY")
            
            // Draw all boxes with enhanced styling
            boxes.forEachIndexed { index, box ->
                // Use box coordinates directly (they're already in canvas space)
                val boxX = box.x 
                val boxY = box.y
                val boxWidth = box.width
                val boxHeight = box.height
                
                // Calculate rectangle coordinates for drawing
                val left = boxX - boxWidth / 2
                val top = boxY - boxHeight / 2
                
                println("DEBUG: Drawing box $index: center=($boxX, $boxY), size=${boxWidth}x${boxHeight}, rect=($left,$top,${left+boxWidth},${top+boxHeight})")
                
                // Fix: Use safe comparison with selectedBox
                val isSelected = index == (selectedBox ?: -1)
                
                // Set color based on selection and mode
                val boxColor = when {
                    isSelected && annotationMode == AnnotationMode.EDIT -> Color.Blue
                    isSelected && annotationMode == AnnotationMode.DELETE -> Color.Red
                    isSelected -> Color.Yellow
                    else -> Color.Green
                }
                
                val strokeWidth = if (isSelected) 4.dp.toPx() else 3.dp.toPx()
                
                // Draw rectangle with appropriate styling
                drawRect(
                    color = boxColor,
                    topLeft = Offset(left, top),
                    size = Size(boxWidth, boxHeight),
                    style = Stroke(width = strokeWidth)
                )
                
                // Draw label
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
            }

            // Draw temporary box during dragging with more visibility
            if (isDrawing) {
                val left = minOf(startPoint.x, endPoint.x)
                val top = minOf(startPoint.y, endPoint.y)
                val width = abs(endPoint.x - startPoint.x)
                val height = abs(endPoint.y - startPoint.y)

                // Log the temporary box coordinates for debugging
                println("DEBUG: Drawing temp box: left=$left, top=$top, width=$width, height=$height")
                
                // Draw the dragging box with a more visible style
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = 3.dp.toPx())
                )
                
                // Also draw the center point for reference
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
