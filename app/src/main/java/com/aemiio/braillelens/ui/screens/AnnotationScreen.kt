package com.aemiio.braillelens.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aemiio.braillelens.R
import com.aemiio.braillelens.objectdetection.BrailleMap
import com.aemiio.braillelens.objectdetection.ProcessedDetectionResult
import com.aemiio.braillelens.ui.BrailleLensColors
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlin.math.abs
import com.aemiio.braillelens.utils.BoxEditControls
import com.aemiio.braillelens.utils.GradeAndClassSelector
import com.aemiio.braillelens.utils.isPointInBox
import androidx.compose.ui.platform.LocalDensity
import kotlin.text.toInt
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

enum class AnnotationMode {
    VIEW, ADD, EDIT, DELETE
}

enum class ResizeHandle {
    TOP, RIGHT, BOTTOM, LEFT,
    TOP_LEFT, TOP_RIGHT,
    BOTTOM_LEFT, BOTTOM_RIGHT,
    NONE
}

data class DetectedBox(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    var classId: String
)

data class AnnotationStyle(
    val boxColors: Map<AnnotationMode, Color> = mapOf(
        AnnotationMode.VIEW to Color.Green,
        AnnotationMode.EDIT to Color.Red,
        AnnotationMode.ADD to Color.Blue,
        AnnotationMode.DELETE to Color.Red
    ),
    val strokeWidth: Map<AnnotationMode, Float> = mapOf(
        AnnotationMode.VIEW to 2f,
        AnnotationMode.EDIT to 3f,
        AnnotationMode.ADD to 2f,
        AnnotationMode.DELETE to 2f
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationScreen(
    navController: NavController,
    imagePath: String,
    detectionResult: ProcessedDetectionResult? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // State variables
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val db = Firebase.firestore
    val boxes = remember { mutableStateListOf<DetectedBox>() }
    var currentMode by remember { mutableStateOf(AnnotationMode.VIEW) }
    var selectedBox by remember { mutableStateOf<Int?>(null) }
    var isDrawing by remember { mutableStateOf(false) }
    var startPoint by remember { mutableStateOf(Offset.Zero) }
    var endPoint by remember { mutableStateOf(Offset.Zero) }
    var classSelectionOpen by remember { mutableStateOf(false) }
    var currentClass by remember { mutableStateOf("A") }
    var gradeSelected by remember { mutableStateOf("Grade 1") }
    var isLoading by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val annotationStyle = remember { AnnotationStyle() }

    // Load detections from ProcessedDetectionResult
    LaunchedEffect(detectionResult) {
        detectionResult?.let { result ->
            try {
                val detectionsField = result::class.java.getDeclaredField("detections")
                detectionsField.isAccessible = true
                val detections = detectionsField.get(result) as? List<*>

                detections?.forEach { detection ->
                    val boundingBoxField = detection?.javaClass?.getDeclaredField("boundingBox")
                    boundingBoxField?.isAccessible = true
                    val boundingBox = boundingBoxField?.get(detection)

                    val classLabelField = detection?.javaClass?.getDeclaredField("classLabel")
                    classLabelField?.isAccessible = true
                    val classLabel = classLabelField?.get(detection) as? String ?: "unknown"

                    val left = boundingBox?.javaClass?.getMethod("left")?.invoke(boundingBox) as? Float ?: 0f
                    val top = boundingBox?.javaClass?.getMethod("top")?.invoke(boundingBox) as? Float ?: 0f
                    val width = boundingBox?.javaClass?.getMethod("width")?.invoke(boundingBox) as? Float ?: 0f
                    val height = boundingBox?.javaClass?.getMethod("height")?.invoke(boundingBox) as? Float ?: 0f

                    boxes.add(
                        DetectedBox(
                            x = left,
                            y = top,
                            width = width,
                            height = height,
                            classId = classLabel
                        )
                    )
                }
            } catch (e: Exception) {
                snackbarMessage = "Error importing detections: ${e.message}"
            }
        }
    }

    // Load bitmap
    LaunchedEffect(imagePath) {
        try {
            bitmap = when {
                imagePath.startsWith("file:") || imagePath.startsWith("content:") -> {
                    val uri = Uri.parse(imagePath)
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                }
                else -> {
                    try {
                        val resourceId = imagePath.toInt()
                        BitmapFactory.decodeResource(context.resources, resourceId)
                    } catch (e: NumberFormatException) {
                        val resourceId = when (imagePath) {
                            "1" -> R.drawable.sample1
                            "2" -> R.drawable.sample2
                            "3" -> R.drawable.sample3
                            "4" -> R.drawable.sample4
                            "5" -> R.drawable.sample5
                            else -> R.drawable.sample1
                        }
                        BitmapFactory.decodeResource(context.resources, resourceId)
                    }
                }
            }
        } catch (e: Exception) {
            snackbarMessage = "Error loading image: ${e.message}"
        }
    }

    // Snackbar handling
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    // Get class options based on grade
    val classOptions = remember(gradeSelected) {
        val brailleMap = if (gradeSelected == "Grade 1")
            BrailleMap.G1brailleMap
        else
            BrailleMap.G2brailleMap

        brailleMap.values.map { it.meaning }.distinct().sorted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Annotation Editor") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(state = scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode selection buttons
            ModeSelectionButtons(
                currentMode = currentMode,
                onModeChange = {
                    currentMode = it
                    selectedBox = null
                }
            )

            // Grade and class selection for ADD/EDIT modes
            if (currentMode == AnnotationMode.ADD ||
                (currentMode == AnnotationMode.EDIT && selectedBox != null)) {
                GradeAndClassSelector(
                    gradeSelected = gradeSelected,
                    onGradeChange = { gradeSelected = it },
                    classOptions = classOptions,
                    selectedBox = selectedBox,
                    boxes = boxes,
                    currentClass = currentClass,
                    onClassChange = { newClass ->
                        if (currentMode == AnnotationMode.EDIT && selectedBox != null) {
                            boxes[selectedBox!!] = boxes[selectedBox!!].copy(classId = newClass)
                        } else {
                            currentClass = newClass
                        }
                    }
                )
            }

            // Annotation canvas
            AnnotationCanvas(
                bitmap = bitmap,
                boxes = boxes,
                currentMode = currentMode,
                selectedBox = selectedBox,
                onBoxSelect = { selectedBox = it },
                onBoxAdd = { boxes.add(it) },
                onBoxDelete = { index -> boxes.removeAt(index) },
                annotationStyle = annotationStyle,
                currentClass = currentClass  // Pass the currentClass
            )

            // Box edit controls for selected box
            if (currentMode == AnnotationMode.EDIT && selectedBox != null && selectedBox!! < boxes.size) {
                BoxEditControls(
                    box = boxes[selectedBox!!],
                    onBoxUpdate = { updatedBox ->
                        boxes[selectedBox!!] = updatedBox
                    }
                )
            }

            // Always show save button when boxes exist
            if (boxes.isNotEmpty()) {
                SaveAnnotationsButton(
                    boxes = boxes,
                    imagePath = imagePath,
                    onSaveStart = { isLoading = true },
                    onSaveComplete = { success, message ->
                        isLoading = false
                        snackbarMessage = message
                    }
                )
            }

            // Show loading indicator if needed
            if (isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ModeSelectionButtons(
    currentMode: AnnotationMode,
    onModeChange: (AnnotationMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(
            Pair(AnnotationMode.VIEW, "View"),
            Pair(AnnotationMode.ADD, "Add"),
            Pair(AnnotationMode.EDIT, "Edit"),
            Pair(AnnotationMode.DELETE, "Delete")
        ).forEach { (mode, label) ->
            Button(
                onClick = { onModeChange(mode) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentMode == mode)
                        BrailleLensColors.darkOlive
                    else
                        BrailleLensColors.pastelGreen
                )
            ) {
                if (mode != AnnotationMode.VIEW) {
                    Icon(
                        imageVector = when (mode) {
                            AnnotationMode.ADD -> Icons.Default.Add
                            AnnotationMode.EDIT -> Icons.Default.Edit
                            AnnotationMode.DELETE -> Icons.Default.Delete
                            else -> Icons.Default.Add
                        },
                        contentDescription = label
                    )
                }
                Text(label)
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
    onBoxSelect: (Int?) -> Unit,
    onBoxAdd: (DetectedBox) -> Unit,
    onBoxDelete: (Int) -> Unit,
    annotationStyle: AnnotationStyle,
    currentClass: String  // New parameter
) {
    val density = LocalDensity.current
    val canvasHeight = 300.dp
    val canvasHeightPx = with(density) { canvasHeight.toPx() }

    // Remember bitmap as ImageBitmap
    val imageBitmap = remember(bitmap) { bitmap?.asImageBitmap() }

    // Calculate scaling factors
    val scale = remember(imageBitmap) {
        imageBitmap?.let { bmp ->
            with(density) {
                minOf(
                    canvasHeightPx / bmp.width,
                    canvasHeightPx / bmp.height
                )
            }
        } ?: 1f
    }

    val scaledWidth = remember(imageBitmap, scale) {
        (imageBitmap?.width?.toFloat() ?: 0f) * scale
    }

    val scaledHeight = remember(imageBitmap, scale) {
        (imageBitmap?.height?.toFloat() ?: 0f) * scale
    }

    val left = remember(scaledWidth) { (canvasHeightPx - scaledWidth) / 2 }
    val top = remember(scaledHeight) { (canvasHeightPx - scaledHeight) / 2 }

    // Track box drawing state
    var isDragging by remember { mutableStateOf(false) }
    var startPoint by remember { mutableStateOf(Offset.Zero) }
    var currentPoint by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(canvasHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        imageBitmap?.let { bmp ->
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(currentMode, currentClass) {
                        when (currentMode) {
                            AnnotationMode.ADD -> {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        isDragging = true
                                        startPoint = offset
                                        currentPoint = offset
                                    },
                                    onDrag = { change, _ ->
                                        currentPoint = change.position
                                    },
                                    onDragEnd = {
                                        // Create box from drag points
                                        val topLeft = Offset(
                                            minOf(startPoint.x, currentPoint.x),
                                            minOf(startPoint.y, currentPoint.y)
                                        )
                                        val boxWidth = abs(currentPoint.x - startPoint.x)
                                        val boxHeight = abs(currentPoint.y - startPoint.y)


                                        if (boxWidth > 10 && boxHeight > 10) {
                                            onBoxAdd(
                                                DetectedBox(
                                                    x = topLeft.x,
                                                    y = topLeft.y,
                                                    width = boxWidth,
                                                    height = boxHeight,
                                                    classId = currentClass
                                                )
                                            )
                                        }
                                        isDragging = false
                                    }
                                )
                            }
                            AnnotationMode.EDIT, AnnotationMode.VIEW -> {
                                detectTapGestures(
                                    onTap = { offset ->
                                        val index = boxes.indexOfLast { isPointInBox(offset, it) }
                                        onBoxSelect(if (index >= 0) index else null)
                                    }
                                )
                            }
                            AnnotationMode.DELETE -> {
                                detectTapGestures(
                                    onTap = { offset ->
                                        val index = boxes.indexOfLast { isPointInBox(offset, it) }
                                        if (index >= 0) onBoxDelete(index)
                                    }
                                )
                            }
                        }
                    }
            ) {
                // Draw the image
                drawImage(
                    image = bmp,
                    dstOffset = IntOffset(left.toInt(), top.toInt()),
                    dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt())
                )

                // Draw existing boxes
                boxes.forEachIndexed { index, box ->
                    val isSelected = index == selectedBox
                    val color = if (isSelected) {
                        annotationStyle.boxColors[AnnotationMode.EDIT]
                    } else {
                        annotationStyle.boxColors[currentMode]
                    } ?: Color.Green

                    val strokeWidth = if (isSelected) {
                        annotationStyle.strokeWidth[AnnotationMode.EDIT]
                    } else {
                        annotationStyle.strokeWidth[currentMode]
                    } ?: 2f

                    drawRect(
                        color = color,
                        topLeft = Offset(box.x, box.y),
                        size = Size(box.width, box.height),
                        style = Stroke(width = strokeWidth)
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        box.classId,
                        box.x,
                        box.y - 5f,
                        Paint().apply {
                            this.color = android.graphics.Color.WHITE
                            this.textSize = 30f
                            this.style = Paint.Style.FILL
                            setShadowLayer(2f, 0f, 0f, android.graphics.Color.BLACK)
                        }
                    )
                }

                // Draw preview box during drag
                if (isDragging && currentMode == AnnotationMode.ADD) {
                    val topLeft = Offset(
                        minOf(startPoint.x, currentPoint.x),
                        minOf(startPoint.y, currentPoint.y)
                    )
                    val width = abs(currentPoint.x - startPoint.x)
                    val height = abs(currentPoint.y - startPoint.y)

                    drawRect(
                        color = annotationStyle.boxColors[AnnotationMode.ADD] ?: Color.Blue,
                        topLeft = topLeft,
                        size = Size(width, height),
                        style = Stroke(width = annotationStyle.strokeWidth[AnnotationMode.ADD] ?: 2f)
                    )
                }
            }
        }
    }
}

@Composable
fun SaveAnnotationsButton(
    boxes: List<DetectedBox>,
    imagePath: String,
    onSaveStart: () -> Unit,
    onSaveComplete: (Boolean, String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val db = Firebase.firestore

    if (boxes.isNotEmpty()) {
        Button(
            onClick = {
                onSaveStart()
                coroutineScope.launch {
                    try {
                        val annotationData = hashMapOf(
                            "imagePath" to imagePath,
                            "boxes" to boxes.map { box ->
                                hashMapOf(
                                    "x" to box.x,
                                    "y" to box.y,
                                    "width" to box.width,
                                    "height" to box.height,
                                    "classId" to box.classId
                                )
                            }
                        )

                        db.collection("annotations")
                            .add(annotationData)
                            .addOnSuccessListener {
                                onSaveComplete(true, "Annotations saved successfully")
                            }
                            .addOnFailureListener { e ->
                                onSaveComplete(false, "Error saving annotations: ${e.message}")
                            }
                    } catch (e: Exception) {
                        onSaveComplete(false, "Error saving annotations: ${e.message}")
                    }
                }
            }
        ) {
            Text("Save Annotations")
        }
    }
}
