package com.aemiio.braillelens.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aemiio.braillelens.R
import com.aemiio.braillelens.objectdetection.BrailleClassIdMapper
import com.aemiio.braillelens.objectdetection.BrailleMap
import com.aemiio.braillelens.ui.BrailleLensColors
import com.aemiio.braillelens.utils.AnnotationState
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

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

@Composable
fun AnnotationScreen(
    navController: NavController,
    imagePath: String,
    onBoxUpdate: (List<DetectedBox>) -> Unit
) {
    val context = LocalContext.current
    val boxes = AnnotationState.boxes
    var originalBitmap by remember { mutableStateOf(AnnotationState.originalBitmap.value) }
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

    LaunchedEffect(imagePath) {
        if (originalBitmap == null) {
            println("DEBUG: Bitmap null in AnnotationScreen, attempting to load from path: $imagePath")
            try {
                // Try to decode from path
                if (imagePath.startsWith("file:") || imagePath.startsWith("content:")) {
                    val uri = Uri.parse(imagePath)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val loadedBitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream.close()
                        if (loadedBitmap != null) {
                            println("DEBUG: Successfully loaded bitmap from URI: $imagePath")
                            AnnotationState.originalBitmap.value = loadedBitmap
                            originalBitmap = loadedBitmap
                        }
                    }
                } else {
                    try {
                        // Try to load as resource ID
                        val resourceId = imagePath.toInt()
                        val loadedBitmap =
                            BitmapFactory.decodeResource(context.resources, resourceId)
                        if (loadedBitmap != null) {
                            println("DEBUG: Successfully loaded bitmap from resource: $resourceId")
                            AnnotationState.originalBitmap.value = loadedBitmap
                            originalBitmap = loadedBitmap
                        }
                    } catch (e: NumberFormatException) {
                        // Try sample images as fallback
                        val resourceId = when (imagePath) {
                            "1" -> R.drawable.sample1
                            "2" -> R.drawable.sample2
                            "3" -> R.drawable.sample3
                            "4" -> R.drawable.sample4
                            "5" -> R.drawable.sample5
                            else -> null
                        }

                        if (resourceId != null) {
                            val loadedBitmap =
                                BitmapFactory.decodeResource(context.resources, resourceId)
                            if (loadedBitmap != null) {
                                println("DEBUG: Successfully loaded bitmap from sample: $resourceId")
                                AnnotationState.originalBitmap.value = loadedBitmap
                                originalBitmap = loadedBitmap
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR loading bitmap from path: ${e.message}")
            }
        }
    }

    // Display error message if bitmap is still null
    if (originalBitmap == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Unable to load the image for annotation.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrailleLensColors.darkOlive
                )
            ) {
                Text("Go Back")
            }
        }
        return
    }

    fun updateBox(index: Int, updatedBox: DetectedBox) {
        // Ensure box stays within canvas boundaries
        val constrainedBox = constrainBoxToCanvas(updatedBox, canvasSize, originalBitmap)
        AnnotationState.updateBox(index, constrainedBox)
        println("Updated box $index: class=${constrainedBox.className}, classId=${constrainedBox.classId}")
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
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // VIEW button
                if (currentMode == AnnotationMode.VIEW) {
                Button(
                    onClick = {
                        currentMode = AnnotationMode.VIEW
                        selectedBox = null
                    },
                    colors = ButtonDefaults.buttonColors(
                            containerColor = BrailleLensColors.darkOlive
                    )
                ) {
                    Text(
                        "View",
                            color = Color.White
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            currentMode = AnnotationMode.VIEW
                            selectedBox = null
                        },
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(BrailleLensColors.darkOlive)
                        )
                    ) {
                        Text(
                            "View",
                            color = BrailleLensColors.darkOlive
                        )
                    }
                }

                // ADD button
                if (currentMode == AnnotationMode.ADD) {
                Button(
                    onClick = {
                        currentMode = AnnotationMode.ADD
                        selectedBox = null
                    },
                    colors = ButtonDefaults.buttonColors(
                            containerColor = BrailleLensColors.darkOlive
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Mode",
                            tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Add",
                            color = Color.White
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            currentMode = AnnotationMode.ADD
                            selectedBox = null
                        },
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(BrailleLensColors.darkOlive)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Mode",
                            tint = BrailleLensColors.darkOlive
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "Add",
                            color = BrailleLensColors.darkOlive
                        )
                    }
                }

                // EDIT button
                if (currentMode == AnnotationMode.EDIT) {
                Button(
                    onClick = {
                        currentMode = AnnotationMode.EDIT
                        if (currentMode != AnnotationMode.EDIT) selectedBox = null
                    },
                    colors = ButtonDefaults.buttonColors(
                            containerColor = BrailleLensColors.darkOlive
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Mode",
                            tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Edit",
                            color = Color.White
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            currentMode = AnnotationMode.EDIT
                            if (currentMode != AnnotationMode.EDIT) selectedBox = null
                        },
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(BrailleLensColors.darkOlive)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Mode",
                            tint = BrailleLensColors.darkOlive
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "Edit",
                            color = BrailleLensColors.darkOlive
                        )
                    }
                }

                // DELETE button
                if (currentMode == AnnotationMode.DELETE) {
                Button(
                    onClick = {
                        currentMode = AnnotationMode.DELETE
                        if (currentMode != AnnotationMode.DELETE) selectedBox = null
                    },
                    colors = ButtonDefaults.buttonColors(
                            containerColor = BrailleLensColors.darkOlive
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Mode",
                            tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Delete",
                            color = Color.White
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            currentMode = AnnotationMode.DELETE
                            if (currentMode != AnnotationMode.DELETE) selectedBox = null
                        },
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(BrailleLensColors.darkOlive)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Mode",
                            tint = BrailleLensColors.darkOlive
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "Delete",
                            color = BrailleLensColors.darkOlive
                        )
                    }
                }
            }

            // Class selector - Now placed ABOVE the canvas when in ADD or EDIT mode
            if (currentMode == AnnotationMode.ADD || (currentMode == AnnotationMode.EDIT && selectedBox != null)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .background(Color.White.copy(alpha = 0.95f))
                        .padding(8.dp)
                ) {
                ClassSelector(
                    classOptions = classOptions,
                        currentClass = if (currentMode == AnnotationMode.EDIT && selectedBox != null)
                            boxes[selectedBox!!].className
                        else
                            currentClass,
                        onClassChange = { newClass ->
                            if (currentMode == AnnotationMode.EDIT && selectedBox != null) {
                                // Update the selected box with the new class
                                val box = boxes[selectedBox!!]
                                val classId =
                                    BrailleClassIdMapper.getMeaningToClassId(newClass, grade)
                                println("DEBUG: Class change in EDIT mode: $newClass (ID: $classId)")
                                val updatedBox = box.copy(className = newClass, classId = classId)
                                updateBox(selectedBox!!, updatedBox)
                            } else {
                                // update current class
                                currentClass = newClass
                            }
                        }
                    )
                }
            }

            // Add this spacer here, after the class selector or mode buttons
            Spacer(modifier = Modifier.height(16.dp))

            // Canvas with consistent container height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
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
                            val canvasScaleX = canvasWidth / originalBitmap!!.width.toFloat()
                            val canvasScaleY = canvasHeight / originalBitmap!!.height.toFloat()
                            
                            val bitmapCenterX = centerX / canvasScaleX
                            val bitmapCenterY = centerY / canvasScaleY
                            val bitmapWidth = width / canvasScaleX
                            val bitmapHeight = height / canvasScaleY
                            
                            println("DEBUG: Converted to bitmap coordinates: ($bitmapCenterX,$bitmapCenterY), size=${bitmapWidth}x${bitmapHeight}")

                            if (width > 10 && height > 10) {
                                // Create the box using bitmap coordinates
                                var box = DetectedBox(
                                    x = bitmapCenterX,
                                    y = bitmapCenterY,
                                    width = bitmapWidth,
                                    height = bitmapHeight,
                                className = currentClass,
                                    classId = BrailleClassIdMapper.getMeaningToClassId(currentClass, grade)
                                )
                                
                                // Apply the same constraints as in edit mode to keep the box within bounds
                                box = constrainBoxToCanvas(box, canvasSize, originalBitmap)
                                
                                println("DEBUG: Adding constrained box in bitmap coordinates: (${box.x},${box.y}), size=${box.width}x${box.height}")
                                addBox(box)
                            }
                        }
                    },
                    currentClass = currentClass,
                    onCanvasSizeChanged = { canvasSize = it }
                )
            }

            // Fixed spacing after canvas - always consistent regardless of canvas height
            Spacer(modifier = Modifier.height(24.dp))

            // Box details panel - Now appears below the canvas
            selectedBox?.let { indexNullable ->
                val index = indexNullable
                if (index in 0 until boxes.size) {
                    val box = boxes[index]

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .background(
                                color = when (currentMode) {
                                    AnnotationMode.EDIT -> BrailleLensColors.pastelGreen.copy(alpha = 0.95f)
                                    AnnotationMode.DELETE -> Color.Red.copy(alpha = 0.85f)
                                    else -> Color.White.copy(alpha = 0.95f)
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
                                        color = Color.Black.copy(alpha = 0.9f)
                                    )

                                    Text(
                                        "Class: ${box.className}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black.copy(alpha = 0.9f)
                                    )

                                    Text(
                                        "Position: (${box.x.toInt()}, ${box.y.toInt()}) • Size: ${box.width.toInt()}×${box.height.toInt()}",
                                        fontSize = 14.sp,
                                        color = Color.Black.copy(alpha = 0.8f)
                                    )
                                }

                                // Right side: Only show Delete button in DELETE mode
                                if (currentMode == AnnotationMode.DELETE) {
                                    Button(
                                        onClick = {
                                            deleteBox(index)
                                            selectedBox = null
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Box",
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete", fontSize = 14.sp)
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
                                            color = Color.Black.copy(alpha = 0.7f),
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
                                                color = Color.Black.copy(alpha = 0.8f),
                                                modifier = Modifier.width(52.dp)  // Fixed width for alignment
                                            )
                                            
                                            // Decrease width button with long press
                                            var decreaseWidthJob by remember { mutableStateOf<Job?>(null) }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .pointerInput(Unit) {
                                                        detectTapGestures(
                                                            onPress = { offset ->
                                                                val newWidth = (box.width - 5).coerceAtLeast(10f)
                                                                updateBox(index, box.copy(width = newWidth))
                                                                
                                                                decreaseWidthJob = coroutineScope.launch {
                                                                    delay(400)
                                                                    while (true) {
                                                                        val currentWidth = (box.width - 5).coerceAtLeast(10f)
                                                                        updateBox(index, box.copy(width = currentWidth))
                                                                        delay(100)
                                                                    }
                                                                }
                                                                
                                                                tryAwaitRelease()
                                                                decreaseWidthJob?.cancel()
                                                                decreaseWidthJob = null
                                                            }
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "−",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    color = BrailleLensColors.darkOlive
                                                )
                                            }
                                            
                                            Text(
                                                "${box.width.toInt()}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.Black.copy(alpha = 0.9f),
                                                modifier = Modifier.width(30.dp),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            
                                            // Increase width button with long press
                                            var increaseWidthJob by remember { mutableStateOf<Job?>(null) }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .pointerInput(Unit) {
                                                        detectTapGestures(
                                                            onPress = { offset ->
                                                                updateBox(index, box.copy(width = box.width + 5))
                                                                
                                                                increaseWidthJob = coroutineScope.launch {
                                                                    delay(400)
                                                                    while (true) {
                                                                        updateBox(index, box.copy(width = box.width + 5))
                                                                        delay(100)
                                                                    }
                                                                }
                                                                
                                                                tryAwaitRelease()
                                                                increaseWidthJob?.cancel()
                                                                increaseWidthJob = null
                                                            }
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Increase Width",
                                                    tint = BrailleLensColors.darkOlive
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
                                                color = Color.Black.copy(alpha = 0.8f),
                                                modifier = Modifier.width(52.dp)  // Fixed width for alignment
                                            )
                                            
                                            // Decrease height button with long press
                                            var decreaseHeightJob by remember { mutableStateOf<Job?>(null) }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .pointerInput(Unit) {
                                                        detectTapGestures(
                                                            onPress = { offset ->
                                                                val newHeight = (box.height - 5).coerceAtLeast(10f)
                                                                updateBox(index, box.copy(height = newHeight))
                                                                
                                                                decreaseHeightJob = coroutineScope.launch {
                                                                    delay(400)
                                                                    while (true) {
                                                                        val currentHeight = (box.height - 5).coerceAtLeast(10f)
                                                                        updateBox(index, box.copy(height = currentHeight))
                                                                        delay(100)
                                                                    }
                                                                }
                                                                
                                                                tryAwaitRelease()
                                                                decreaseHeightJob?.cancel()
                                                                decreaseHeightJob = null
                                                            }
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Reduce Height",
                                                    tint = BrailleLensColors.darkOlive
                                                )
                                            }
                                            
                                            Text(
                                                "${box.height.toInt()}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.Black.copy(alpha = 0.9f),
                                                modifier = Modifier.width(30.dp),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            
                                            // Increase height button with long press
                                            var increaseHeightJob by remember { mutableStateOf<Job?>(null) }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .pointerInput(Unit) {
                                                        detectTapGestures(
                                                            onPress = { offset ->
                                                                updateBox(index, box.copy(height = box.height + 5))
                                                                
                                                                increaseHeightJob = coroutineScope.launch {
                                                                    delay(400)
                                                                    while (true) {
                                                                        updateBox(index, box.copy(height = box.height + 5))
                                                                        delay(100)
                                                                    }
                                                                }
                                                                
                                                                tryAwaitRelease()
                                                                increaseHeightJob?.cancel()
                                                                increaseHeightJob = null
                                                            }
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Increase Height",
                                                    tint = BrailleLensColors.darkOlive
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
                                            color = Color.Black.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        
                                        // Movement controls in a compact directional pad layout
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            // Up button with long press
                                            var upRepeatJob by remember { mutableStateOf<Job?>(null) }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .pointerInput(Unit) {
                                                        detectTapGestures(
                                                            onPress = { offset ->
                                                                // Handle the initial press
                                                                updateBox(index, box.copy(y = box.y - 5))
                                                                
                                                                // Start a repeating job for continuous movement
                                                                upRepeatJob = coroutineScope.launch {
                                                                    delay(400) // Initial delay before repeating
                                                                    while (true) {
                                                                        updateBox(index, box.copy(y = box.y - 5))
                                                                        delay(100) // Repeat interval
                                                                    }
                                                                }
                                                                
                                                                // Wait for release
                                                                tryAwaitRelease()
                                                                
                                                                // Cancel the job when released
                                                                upRepeatJob?.cancel()
                                                                upRepeatJob = null
                                                            }
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowUp,
                                                    contentDescription = "Move Up",
                                                    tint = BrailleLensColors.darkOlive
                                                )
                                            }
                                            
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Left button with long press
                                                var leftRepeatJob by remember { mutableStateOf<Job?>(null) }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .pointerInput(Unit) {
                                                            detectTapGestures(
                                                                onPress = { offset ->
                                                                    updateBox(index, box.copy(x = box.x - 5))
                                                                    
                                                                    leftRepeatJob = coroutineScope.launch {
                                                                        delay(400)
                                                                        while (true) {
                                                                            updateBox(index, box.copy(x = box.x - 5))
                                                                            delay(100)
                                                                        }
                                                                    }
                                                                    
                                                                    tryAwaitRelease()
                                                                    leftRepeatJob?.cancel()
                                                                    leftRepeatJob = null
                                                                }
                                                            )
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                                        contentDescription = "Move Left",
                                                        tint = BrailleLensColors.darkOlive
                                                    )
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(
                                                            color = BrailleLensColors.darkOlive,
                                                            shape = CircleShape
                                                        )
                                                )
                                                
                                                // Right button with long press
                                                var rightRepeatJob by remember { mutableStateOf<Job?>(null) }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .pointerInput(Unit) {
                                                            detectTapGestures(
                                                                onPress = { offset ->
                                                                    updateBox(index, box.copy(x = box.x + 5))
                                                                    
                                                                    rightRepeatJob = coroutineScope.launch {
                                                                        delay(400)
                                                                        while (true) {
                                                                            updateBox(index, box.copy(x = box.x + 5))
                                                                            delay(100)
                                                                        }
                                                                    }
                                                                    
                                                                    tryAwaitRelease()
                                                                    rightRepeatJob?.cancel()
                                                                    rightRepeatJob = null
                                                                }
                                                            )
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.KeyboardArrowRight,
                                                        contentDescription = "Move Right",
                                                        tint = BrailleLensColors.darkOlive
                                                    )
                                                }
                                            }
                                            
                                            // Down button with long press
                                            var downRepeatJob by remember { mutableStateOf<Job?>(null) }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .pointerInput(Unit) {
                                                        detectTapGestures(
                                                            onPress = { offset ->
                                                                updateBox(index, box.copy(y = box.y + 5))
                                                                
                                                                downRepeatJob = coroutineScope.launch {
                                                                    delay(400)
                                                                    while (true) {
                                                                        updateBox(index, box.copy(y = box.y + 5))
                                                                        delay(100)
                                                                    }
                                                                }
                                                                
                                                                tryAwaitRelease()
                                                                downRepeatJob?.cancel()
                                                                downRepeatJob = null
                                                            }
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Move Down",
                                                    tint = BrailleLensColors.darkOlive
                                                )
                                            }
                                        }
                                    }
                                }
                            }
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
