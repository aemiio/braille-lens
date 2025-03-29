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
import androidx.compose.ui.platform.LocalDensity
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
                    Text("View", color = if (currentMode == AnnotationMode.VIEW) Color.White else Color.Black)
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
                    Text("Add", color = if (currentMode == AnnotationMode.ADD) Color.White else Color.Black)
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
                    Text("Edit", color = if (currentMode == AnnotationMode.EDIT) Color.White else Color.Black)
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
                    Text("Delete", color = if (currentMode == AnnotationMode.DELETE) Color.White else Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Class selector
            if (currentMode == AnnotationMode.ADD || (currentMode == AnnotationMode.EDIT && selectedBox != null)) {
                ClassSelector(
                    classOptions = classOptions,
                    currentClass = currentClass,
                    onClassChange = { newClass -> currentClass = newClass }
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
                    currentMode = currentMode,
                    selectedBox = selectedBox,
                    isDrawing = isDrawing,
                    startPoint = startPoint,
                    endPoint = endPoint,
                    onBoxSelect = { index -> selectedBox = index },
                    onBoxAdd = { newBox ->
                        val classId = BrailleClassIdMapper.getMeaningToClassId(currentClass, grade)
                        if (classId >= 0) {
                            val box = newBox.copy(className = currentClass, classId = classId)
                            AnnotationState.addBox(box)
                        }
                    },
                    onBoxDelete = { index ->
                        if (index != null) {
                            AnnotationState.removeBox(index)
                            selectedBox = null
                        }
                    },
                    onBoxUpdate = { index, updatedBox ->
                        if (index != null) {
                            val classId = BrailleClassIdMapper.getMeaningToClassId(currentClass, grade)
                            if (classId >= 0) {
                                val box = updatedBox.copy(className = currentClass, classId = classId)
                                AnnotationState.updateBox(index, box)
                            }
                        }
                    },
                    onStartDrawing = { offset ->
                        if (currentMode == AnnotationMode.ADD) {
                            isDrawing = true
                            startPoint = offset
                            endPoint = offset
                        }
                    },
                    onDrawing = { offset ->
                        if (isDrawing && currentMode == AnnotationMode.ADD) {
                            endPoint = offset
                        }
                    },
                    onEndDrawing = {
                        if (isDrawing && currentMode == AnnotationMode.ADD) {
                            isDrawing = false
                        }
                    },
                    currentClass = currentClass
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
                    }
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
                value = currentClass,
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
    currentMode: AnnotationMode,
    selectedBox: Int?,
    isDrawing: Boolean,
    startPoint: Offset,
    endPoint: Offset,
    onBoxSelect: (Int?) -> Unit,
    onBoxAdd: (DetectedBox) -> Unit,
    onBoxDelete: (Int?) -> Unit,
    onBoxUpdate: (Int?, DetectedBox) -> Unit,
    onStartDrawing: (Offset) -> Unit,
    onDrawing: (Offset) -> Unit,
    onEndDrawing: () -> Unit,
    currentClass: String
) {
    val density = LocalDensity.current

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(currentMode) {
                when (currentMode) {
                    AnnotationMode.ADD -> {
                        detectDragGestures(
                            onDragStart = { offset -> onStartDrawing(offset) },
                            onDrag = { _, dragAmount -> onDrawing(endPoint + dragAmount) },
                            onDragEnd = { onEndDrawing() },
                            onDragCancel = { onEndDrawing() }
                        )
                    }
                    AnnotationMode.VIEW, AnnotationMode.EDIT, AnnotationMode.DELETE -> {
                        detectTapGestures { offset ->
                            var foundBox = false
                            for (i in boxes.indices.reversed()) {
                                val box = boxes[i]

                                // Scale coordinates to the canvas size
                                val scaleX = size.width / (bitmap?.width?.toFloat() ?: 1f)
                                val scaleY = size.height / (bitmap?.height?.toFloat() ?: 1f)

                                // Calculate box boundaries in canvas coordinates
                                val left = box.x * scaleX
                                val top = box.y * scaleY
                                val boxWidth = box.width * scaleX
                                val boxHeight = box.height * scaleY

                                // Check if tap is inside the box
                                if (offset.x >= left && offset.x <= left + boxWidth &&
                                    offset.y >= top && offset.y <= top + boxHeight) {

                                    when (currentMode) {
                                        AnnotationMode.DELETE -> onBoxDelete(i)
                                        AnnotationMode.EDIT, AnnotationMode.VIEW -> onBoxSelect(i)
                                        else -> {}
                                    }

                                    foundBox = true
                                    break
                                }
                            }

                            if (!foundBox) {
                                onBoxSelect(null)
                            }
                        }
                    }
                }
            }
    ) {
        // Draw the bitmap as background
        bitmap?.let { bmp ->
            drawContext.canvas.nativeCanvas.drawBitmap(
                bmp,
                null,
                android.graphics.Rect(0, 0, size.width.toInt(), size.height.toInt()),
                Paint()
            )

            // Calculate scaling factors
            val scaleX = size.width / bmp.width
            val scaleY = size.height / bmp.height

            // Draw all boxes
            boxes.forEachIndexed { index, box ->
                val isSelected = index == selectedBox

                // Convert coordinates to canvas space
                val left = box.x * scaleX
                val top = box.y * scaleY
                val boxWidth = box.width * scaleX
                val boxHeight = box.height * scaleX

                // Choose color based on selection state
                val boxColor = if (isSelected) Color.Red else Color.Green
                val strokeWidth = if (isSelected) 3f else 2f

                // Draw rectangle
                drawRect(
                    color = boxColor,
                    topLeft = Offset(left, top),
                    size = Size(boxWidth, boxHeight),
                    style = Stroke(width = strokeWidth)
                )

                // Draw label if needed
                drawContext.canvas.nativeCanvas.drawText(
                    box.className,
                    left,
                    top - 5f,
                    Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 30f
                        style = Paint.Style.FILL
                        setShadowLayer(3f, 1f, 1f, android.graphics.Color.BLACK)
                    }
                )
            }

            // Draw current rectangle being created in ADD mode
            if (isDrawing && currentMode == AnnotationMode.ADD) {
                val drawStart = startPoint
                val drawEnd = endPoint

                val left = minOf(drawStart.x, drawEnd.x)
                val top = minOf(drawStart.y, drawEnd.y)
                val width = abs(drawEnd.x - drawStart.x)
                val height = abs(drawEnd.y - drawStart.y)

                drawRect(
                    color = Color.Yellow,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = 2f)
                )

                // Create box on end drawing if size is sufficient
                if (!isDrawing && width > 10 && height > 10) {
                    // Convert to bitmap coordinates
                    val bmpX = left / scaleX
                    val bmpY = top / scaleY
                    val bmpWidth = width / scaleX
                    val bmpHeight = height / scaleY

                    val newBox = DetectedBox(
                        x = bmpX,
                        y = bmpY,
                        width = bmpWidth,
                        height = bmpHeight,
                        className = currentClass,
                        classId = -1  // Will be set by onBoxAdd
                    )

                    onBoxAdd(newBox)
                }
            }
        }
    }
}