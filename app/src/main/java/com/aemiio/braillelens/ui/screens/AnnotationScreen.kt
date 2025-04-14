package com.aemiio.braillelens.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aemiio.braillelens.R
import com.aemiio.braillelens.objectdetection.BrailleClassIdMapper
import com.aemiio.braillelens.objectdetection.BrailleMap
import com.aemiio.braillelens.services.SupabaseService
import com.aemiio.braillelens.ui.BrailleLensColors
import com.aemiio.braillelens.ui.components.annotations.AnnotationCanvas
import com.aemiio.braillelens.ui.components.annotations.AnnotationHelpBottomSheet
import com.aemiio.braillelens.ui.components.annotations.BoxDetailsCard
import com.aemiio.braillelens.ui.components.annotations.HelpButton
import com.aemiio.braillelens.ui.components.annotations.TermsAndConditionsModal
import com.aemiio.braillelens.ui.components.annotations.hasAcceptedAnnotationTerms
import com.aemiio.braillelens.ui.components.annotations.saveTermsAcceptanceStatus
import com.aemiio.braillelens.utils.AnnotationState
import com.aemiio.braillelens.utils.ClassSelector
import com.aemiio.braillelens.utils.constrainBoxToCanvas
import kotlinx.coroutines.launch
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

    var showTermsModal by remember { mutableStateOf(false) }
    
    // Check if terms have been accepted - moved inside the composable function
    val termsAccepted = remember { hasAcceptedAnnotationTerms(context) }
    var isEditorUnlocked by remember { mutableStateOf(termsAccepted) }
    
    // Handle the terms and conditions modal
    if (showTermsModal || (!termsAccepted && !isEditorUnlocked)) {
        TermsAndConditionsModal(
            showTerms = true,
            onAccept = {
                saveTermsAcceptanceStatus(context, true)
                isEditorUnlocked = true
                showTermsModal = false
            },
            onDecline = {
                saveTermsAcceptanceStatus(context, false)
                showTermsModal = false
                navController.popBackStack()
            }
        )
    }

    LaunchedEffect(imagePath) {
        if (originalBitmap == null) {
            println("DEBUG: Bitmap null in AnnotationScreen, attempting to load from path: $imagePath")
            try {
                // Decode from path
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

                        val resourceId = imagePath.toInt()
                        val loadedBitmap =
                            BitmapFactory.decodeResource(context.resources, resourceId)
                        if (loadedBitmap != null) {
                            println("DEBUG: Successfully loaded bitmap from resource: $resourceId")
                            AnnotationState.originalBitmap.value = loadedBitmap
                            originalBitmap = loadedBitmap
                        }
                    } catch (e: NumberFormatException) {

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

    // Display error message if bitmap is null
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

    var showHelpBottomSheet by remember { mutableStateOf(false) }
    
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
                    text = selectedModel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    ) { paddingValues ->
        // Only show the editor content if terms have been accepted
        if (isEditorUnlocked) {
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

                // Class selector
                if (currentMode == AnnotationMode.ADD || (currentMode == AnnotationMode.EDIT && selectedBox != null)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(8.dp)
                    ) {
                        ClassSelector(
                            classOptions = classOptions,
                            currentClass = if (currentMode == AnnotationMode.EDIT && selectedBox != null)
                                boxes[selectedBox!!].className
                            else
                                currentClass,
                            onClassChange = { newClass: String ->
                                if (currentMode == AnnotationMode.EDIT && selectedBox != null) {
                                    // Update the selected box with the new class
                                    val box = boxes[selectedBox!!]
                                    val classId =
                                        BrailleClassIdMapper.getMeaningToClassId(newClass, grade)
                                    println("DEBUG: Class change in EDIT mode: $newClass (ID: $classId)")
                                    val updatedBox = box.copy(className = newClass, classId = classId)
                                    updateBox(selectedBox!!, updatedBox)
                                } else {

                                    currentClass = newClass
                                }
                            }
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))


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
                        onBoxSelect = { index: Int? -> selectedBox = index },
                        onBoxAdd = { newBox: DetectedBox ->
                            val classId = BrailleClassIdMapper.getMeaningToClassId(currentClass, grade)
                            val box = newBox.copy(className = currentClass, classId = classId)
                            addBox(box)
                        },
                        onBoxDelete = { index: Int ->
                            deleteBox(index)
                        },
                        onStartDrawing = { offset: Offset ->
                            isDrawing = true
                            startPoint = offset
                            endPoint = offset
                        },
                        onDrawing = { offset: Offset ->
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
                                        classId = BrailleClassIdMapper.getMeaningToClassId(
                                            currentClass,
                                            grade
                                        )
                                    )

                                    // Apply the same constraints as in edit mode to keep the box within bounds
                                    box = constrainBoxToCanvas(box, canvasSize, originalBitmap)

                                    println("DEBUG: Adding constrained box in bitmap coordinates: (${box.x},${box.y}), size=${box.width}x${box.height}")
                                    addBox(box)
                                }
                            }
                        },
                        currentClass = currentClass,
                        onCanvasSizeChanged = { newSize: Size -> canvasSize = newSize }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                selectedBox?.let { indexNullable ->
                    val index = indexNullable
                    if (index in 0 until boxes.size) {
                        val box = boxes[index]

                        BoxDetailsCard(
                            box = box,
                            currentMode = currentMode,
                            selectedBox = index,
                            boxes = boxes,
                            onBoxDelete = { boxIndex ->
                                deleteBox(boxIndex)
                                selectedBox = null
                            },
                            onBoxUpdate = { boxIndex, updatedBox ->
                                // Ensure box stays within canvas boundaries
                                val constrainedBox =
                                    constrainBoxToCanvas(updatedBox, canvasSize, originalBitmap)
                                updateBox(boxIndex, constrainedBox)
                            }
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))
                
                // Save Annotations Button
                var isSaving by remember { mutableStateOf(false) }
                var saveMessage by remember { mutableStateOf<String?>(null) }
                var saveMessageColor by remember { mutableStateOf(BrailleLensColors.darkOlive) }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (boxes.isNotEmpty() && originalBitmap != null) {
                                    isSaving = true
                                    saveMessage = null
                                    coroutineScope.launch {
                                        try {
                                            val result = SupabaseService.saveAnnotations(
                                                context = context,
                                                boxes = boxes,
                                                imagePath = AnnotationState.imagePath.value,
                                                bitmap = originalBitmap,
                                                grade = grade.toString()
                                            )
                                            
                                            result.fold(
                                                onSuccess = { message ->
                                                    saveMessage = "Saved successfully!"
                                                    saveMessageColor = BrailleLensColors.darkOlive
                                                    isSaving = false
                                                },
                                                onFailure = { error ->
                                                    saveMessage = "Error: ${error.message}"
                                                    saveMessageColor = Color.Red
                                                    isSaving = false
                                                }
                                            )
                                        } catch (e: Exception) {
                                            saveMessage = "Error: ${e.message}"
                                            saveMessageColor = Color.Red
                                            isSaving = false
                                        }
                                    }
                                } else {
                                    saveMessage = "Nothing to save. Add annotations first."
                                    saveMessageColor = Color.Red
                                }
                            },
                            enabled = !isSaving && boxes.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrailleLensColors.darkOlive,
                                disabledContainerColor = BrailleLensColors.darkOlive.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isSaving) "Saving..." else "Save Annotations",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                        
                        // Display success/error message
                        saveMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it,
                                color = saveMessageColor,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                

                Spacer(modifier = Modifier.height(16.dp))
                
                // Help Button
                HelpButton(onClick = { showHelpBottomSheet = true })
                
                // Terms and Conditions Button
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { showTermsModal = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = BrailleLensColors.darkOlive.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "View Terms & Conditions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = BrailleLensColors.darkOlive
                )
            }
        }
    }

    // Display the help bottom sheet
    AnnotationHelpBottomSheet(
        showBottomSheet = showHelpBottomSheet,
        onDismiss = { showHelpBottomSheet = false }
    )
    
    // Check if terms have been accepted when the screen is first launched
    LaunchedEffect(Unit) {
        if (!hasAcceptedAnnotationTerms(context)) {
            showTermsModal = true
        }
    }
}


