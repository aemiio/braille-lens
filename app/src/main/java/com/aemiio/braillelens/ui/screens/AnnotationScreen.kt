package com.aemiio.braillelens.ui.screens

import android.graphics.Bitmap
import android.graphics.Paint
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aemiio.braillelens.objectdetection.BrailleClassIdMapper
import com.aemiio.braillelens.objectdetection.BrailleMap
import com.aemiio.braillelens.ui.BrailleLensColors
import com.aemiio.braillelens.utils.AnnotationState
import kotlin.math.abs

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
        selectedModel.contains("Both") -> 3
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


    LaunchedEffect(classOptions) {
        if (classOptions.isNotEmpty() && currentClass.isEmpty()) {
            currentClass = classOptions[0]
        }
    }


    LaunchedEffect(imagePath) {
        if (originalBitmap == null && imagePath.isNotEmpty()) {
            println("Image path already set in AnnotationState: $imagePath")
        }
    }

    // normalize boxes if they're too large
    LaunchedEffect(canvasSize, boxes) {
        if (canvasSize.width > 0 && canvasSize.height > 0 && boxes.isNotEmpty()) {
            // Check if boxes need normalization (very large values)
            val needsNormalization = boxes.any { box -> 
                box.x > canvasSize.width * 2 || box.y > canvasSize.height * 2 
            }
            
            if (needsNormalization) {
                println("DEBUG: Emergency box coordinate normalization needed!")
                
                // Find the largest coordinate value
                val maxX = boxes.maxOfOrNull { it.x } ?: 1f
                val maxY = boxes.maxOfOrNull { it.y } ?: 1f
                
                // Calculate scale factors to fit within canvas
                val scaleX = canvasSize.width / maxX * 0.8f
                val scaleY = canvasSize.height / maxY * 0.8f
                val scaleFactor = minOf(scaleX, scaleY)
                
                println("DEBUG: Emergency normalization - Max values: $maxX x $maxY, Scale: $scaleFactor")

                val normalizedBoxes = boxes.mapIndexed { index, box ->
                    val normalizedBox = DetectedBox(
                        x = box.x * scaleFactor,
                        y = box.y * scaleFactor,
                        width = box.width * scaleFactor,
                        height = box.height * scaleFactor,
                        className = box.className,
                        classId = box.classId,
                        confidence = box.confidence
                    )
                    println("DEBUG: Normalized box $index: ${box.x},${box.y} -> ${normalizedBox.x},${normalizedBox.y}")
                    normalizedBox
                }

                AnnotationState.updateBoxes(normalizedBoxes)
            } else {
                println("DEBUG: Box coordinates appear to be within reasonable range")
            }
        }
    }


    LaunchedEffect(canvasSize) {
        if (canvasSize.width > 0 && canvasSize.height > 0 && boxes.isNotEmpty()) {
            // Check if any box is out of reasonable range
            val needsNormalization = boxes.any { box -> 
                box.x > canvasSize.width * 2 || box.y > canvasSize.height * 2 || 
                box.x < -canvasSize.width || box.y < -canvasSize.height
            }
            
            if (needsNormalization) {
                println("DEBUG: Applying complete box coordinate normalization")
                AnnotationState.normalizeAllBoxes(canvasSize.width, canvasSize.height)
            }
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


            if (currentMode == AnnotationMode.ADD || (currentMode == AnnotationMode.EDIT && selectedBox != null)) {

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
                            // update current class
                            currentClass = newClass
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }


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

                            val canvasWidth = canvasSize.width
                            val canvasHeight = canvasSize.height
                            
                            // Calculate box dimensions in canvas coordinates
                            val left = minOf(startPoint.x, endPoint.x)
                            val top = minOf(startPoint.y, endPoint.y)
                            val width = abs(endPoint.x - startPoint.x)
                            val height = abs(endPoint.y - startPoint.y)
                            

                            val centerX = left + width / 2
                            val centerY = top + height / 2

                            println("DEBUG: Drawing box in canvas coordinates: ($centerX,$centerY), size=${width}x${height}")
                            
                            // Convert from canvas coordinates to bitmap coordinates
                            val canvasScaleX = canvasWidth / originalBitmap.width.toFloat()
                            val canvasScaleY = canvasHeight / originalBitmap.height.toFloat()
                            
                            val bitmapCenterX = centerX / canvasScaleX
                            val bitmapCenterY = centerY / canvasScaleY
                            val bitmapWidth = width / canvasScaleX
                            val bitmapHeight = height / canvasScaleY
                            
                            println("DEBUG: Converted to bitmap coordinates: ($bitmapCenterX,$bitmapCenterY), size=${bitmapWidth}x${bitmapHeight}")


                            if (width > 10 && height > 10) {
                                // Create the box using bitmap coordinates for consistency with existing boxes
                                val box = DetectedBox(
                                    x = bitmapCenterX,
                                    y = bitmapCenterY,
                                    width = bitmapWidth,
                                    height = bitmapHeight,
                                    className = currentClass,
                                    classId = BrailleClassIdMapper.getMeaningToClassId(currentClass, grade)
                                )

                                println("DEBUG: Adding new box in bitmap coordinates: (${box.x},${box.y}), size=${box.width}x${box.height}")
                                addBox(box)
                            }
                        }
                    },
                    currentClass = currentClass,
                    onCanvasSizeChanged = { size ->
                        canvasSize = size
                    }
                )
            }


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


            if (currentMode == AnnotationMode.EDIT && selectedBox != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {

                        selectedBox?.let { boxIndex ->
                            val box = boxes[boxIndex]
                            val classId = BrailleClassIdMapper.getMeaningToClassId(currentClass, grade)
                            val updatedBox = box.copy(className = currentClass, classId = classId)
                            updateBox(boxIndex, updatedBox)

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

                                val canvasScaleX = size.width / bmp.width
                                val canvasScaleY = size.height / bmp.height
                                

                                var tappedBoxIndex: Int? = null
                                for (i in boxes.indices.reversed()) {
                                    val box = boxes[i]
                                    
                                    // Scale box coordinates to match canvas size
                                    val scaledX = box.x * canvasScaleX
                                    val scaledY = box.y * canvasScaleY
                                    val scaledWidth = box.width * canvasScaleX
                                    val scaledHeight = box.height * canvasScaleY
                                    
                                    // Calculate box boundaries with scaling applied
                                    val boxLeft = scaledX - scaledWidth / 2
                                    val boxTop = scaledY - scaledHeight / 2
                                    val boxRight = boxLeft + scaledWidth
                                    val boxBottom = boxTop + scaledHeight

                                    if (offset.x in boxLeft..boxRight && offset.y in boxTop..boxBottom) {
                                        tappedBoxIndex = i
                                        println("DEBUG: Box $i selected at tap ($offset)!")
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

        onCanvasSizeChanged(size)
        
        bitmap?.let { bmp ->

            drawContext.canvas.nativeCanvas.drawBitmap(
                bmp,
                null,
                android.graphics.Rect(0, 0, size.width.toInt(), size.height.toInt()),
                null
            )


            println("DEBUG: AnnotationCanvas - Canvas size: ${size.width} x ${size.height}, Bitmap size: ${bmp.width} x ${bmp.height}")
            
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
                
                println("DEBUG: Box $index: Original=(${box.x},${box.y}), Scaled=($scaledX,$scaledY), size=${scaledWidth}x${scaledHeight}")
                

                val isSelected = index == (selectedBox ?: -1)
                

                val boxColor = when {
                    isSelected && annotationMode == AnnotationMode.EDIT -> Color.Blue
                    isSelected && annotationMode == AnnotationMode.DELETE -> Color.Red
                    isSelected -> Color.Yellow
                    else -> Color.Green
                }
                
                val strokeWidth = if (isSelected) 4.dp.toPx() else 3.dp.toPx()
                

                drawRect(
                    color = boxColor,
                    topLeft = Offset(left, top),
                    size = Size(scaledWidth, scaledHeight),
                    style = Stroke(width = strokeWidth)
                )
                

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
