package com.aemiio.braillelens.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aemiio.braillelens.R
import com.aemiio.braillelens.objectdetection.ProcessedDetectionResult
import com.aemiio.braillelens.services.ObjectDetectionService
import com.aemiio.braillelens.ui.BrailleLensColors
import com.aemiio.braillelens.utils.AnnotationState
import com.aemiio.braillelens.utils.TTSManager
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import com.aemiio.braillelens.ui.components.result.ResultBottomSheet


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecognitionResultScreen(
    navController: NavController,
    detectionMode: String,
    imagePath: String,

    ) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val ttsManager = remember { TTSManager.getInstance(context) }
    val objectDetectionService = remember { ObjectDetectionService() }

    var sliderValue by remember { mutableStateOf(0.25f) }
    var activeThreshold by remember { mutableStateOf(0.25f) }

    var isLoading by remember { mutableStateOf(true) }
    var detectionResult by remember { mutableStateOf<ProcessedDetectionResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var selectedModel by remember { mutableStateOf(detectionMode) }

    var showHelpBottomSheet by remember { mutableStateOf(false) }

    val processImage = {
        coroutineScope.launch {
            try {
                isLoading = true
                errorMessage = null

                if (imagePath.startsWith("file:") || imagePath.startsWith("content:")) {
                    val uri = Uri.parse(imagePath)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    detectionResult = objectDetectionService.detectBrailleFromUri(
                        context,
                        uri,
                        selectedModel
                    )
                } else {

                    try {
                        val resourceId = imagePath.toInt()
                        originalBitmap = BitmapFactory.decodeResource(context.resources, resourceId)
                        detectionResult = objectDetectionService.detectBrailleFromDrawableResource(
                            context,
                            resourceId,
                            selectedModel
                        )
                    } catch (e: NumberFormatException) {
                        val resourceId = when (imagePath) {
                            "1" -> R.drawable.sample1
                            "2" -> R.drawable.sample2
                            "3" -> R.drawable.sample3
                            "4" -> R.drawable.sample4
                            "5" -> R.drawable.sample5
                            else -> R.drawable.sample1
                        }
                        originalBitmap = BitmapFactory.decodeResource(context.resources, resourceId)
                        detectionResult = objectDetectionService.detectBrailleFromDrawableResource(
                            context,
                            resourceId,
                            selectedModel
                        )
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error processing image: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Reset state when inputs change
    DisposableEffect(imagePath, detectionMode) {
        detectionResult = null
        originalBitmap = null
        errorMessage = null
        onDispose { }
    }


    LaunchedEffect(activeThreshold) {
        objectDetectionService.confidenceThreshold = activeThreshold
        if (!isLoading && detectionResult != null) {
            processImage()
        }
    }

    // Initial processing
    LaunchedEffect(imagePath, detectionMode) {
        processImage()
    }


    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Result - $selectedModel",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { showHelpBottomSheet = true }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.info_24px),
                            contentDescription = "Help",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    ResultBottomSheet(
                        showBottomSheet = showHelpBottomSheet,
                        onDismiss = { showHelpBottomSheet = false }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(32.dp)
                        .size(64.dp)
                )
                Text("Processing braille image...")
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {
                        errorMessage = null
                        processImage()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrailleLensColors.darkOlive
                    )
                ) {
                    Text("Retry")
                }
            } else {

                Text(
                    "Original Image",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                originalBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Original Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                detectionResult?.let { result ->
                    // Processed Image display
                    Text(
                        "Detected Braille",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Image(
                        bitmap = result.displayBitmap.asImageBitmap(),
                        contentDescription = "Detected Braille",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    Text(
                        "Recognition Mode",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val gradeOptions = listOf("Grade 1 Braille", "Grade 2 Braille", "Both Grades")

                        gradeOptions.forEach { grade ->
                            FilterChip(
                                selected = selectedModel == grade,
                                onClick = {
                                    if (selectedModel != grade) {
                                        selectedModel = grade
                                        // Reprocess with new model
                                        processImage()
                                    }
                                },
                                label = {
                                    Text(
                                        text = grade.replace(" Braille", ""),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrailleLensColors.darkOlive,
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedModel == grade,
                                    borderColor = BrailleLensColors.darkOlive,
                                    selectedBorderColor = BrailleLensColors.darkOlive,
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Confidence Threshold: ${String.format("%.2f", sliderValue)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            onValueChangeFinished = {

                                activeThreshold = sliderValue
                            },
                            valueRange = 0.05f..1.0f,
                            steps = 18,
                            colors = SliderDefaults.colors(
                                thumbColor = BrailleLensColors.darkOlive,
                                activeTrackColor = BrailleLensColors.pastelGreen,
                                activeTickColor = BrailleLensColors.darkOlive,
                                inactiveTickColor = BrailleLensColors.accentBeige
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        var detectionResultExpanded by remember { mutableStateOf(false) }

                        // Header row with expand/collapse functionality
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable { detectionResultExpanded = !detectionResultExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Detection Results",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (detectionResultExpanded) 
                                    Icons.Default.KeyboardArrowUp 
                                else 
                                    Icons.Default.KeyboardArrowDown,
                                contentDescription = if (detectionResultExpanded) 
                                    "Collapse" 
                                else 
                                    "Expand",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Content shown only when expanded
                        if (detectionResultExpanded) {
                            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                                Text(result.detectionText, fontSize = 14.sp)
                            }
                        }
                    }

                    // Separate card for translated text
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Translated Braille",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(result.translatedText, fontSize = 14.sp)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Button(
                            onClick = { ttsManager.speak(result.translatedText) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrailleLensColors.darkOlive
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.volume_up),
                                contentDescription = "Text to Speech",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Read Aloud")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Retry button
                            Button(
                                onClick = { processImage() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrailleLensColors.darkOlive
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry Detection",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry")
                            }
                            
                            // Correct Annotations button
                            Button(
                                onClick = {
                                    // Store detection results in AnnotationState before navigating
                                    if (detectionResult != null) {
                                        try {
                                            val cleanDisplayBitmap = objectDetectionService.getCleanDisplayBitmap()
                                            
                                            if (cleanDisplayBitmap == null) {
                                                // Fallback if clean bitmap isn't available - use the original bitmap
                                                println("WARNING: Clean display bitmap is null, using original bitmap instead")
                                                originalBitmap?.let { origBitmap ->
                                                    AnnotationState.setDetectionResults(
                                                        detectedBoxes = objectDetectionService.getResultBitmapBoxes(detectionResult?.displayBitmap),
                                                        bitmap = origBitmap,
                                                        model = selectedModel,
                                                        path = imagePath
                                                    )
                                                }
                                            } else {
                                                // Use clean bitmap as intended
                                                AnnotationState.setDetectionResults(
                                                    detectedBoxes = objectDetectionService.getResultBitmapBoxes(detectionResult?.displayBitmap),
                                                    bitmap = cleanDisplayBitmap,
                                                    model = selectedModel,
                                                    path = imagePath
                                                )
                                            }
                                            
                                            println("DEBUG: Successfully set bitmap and ${AnnotationState.boxes.size} boxes for annotation")
                                            navController.navigate("annotation/${Uri.encode(imagePath)}")
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            println("ERROR during annotation preparation: ${e.message}")
                                            // Show error feedback to user
                                            errorMessage = "Failed to prepare image for editing: ${e.message}"
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrailleLensColors.darkOlive
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Create,
                                    contentDescription = "Edit Annotations",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit")
                            }
                        }
                    }
                }
            }
        }
    }
}