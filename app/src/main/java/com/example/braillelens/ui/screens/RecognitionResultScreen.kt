package com.example.braillelens.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.braillelens.R
import com.example.braillelens.objectdetection.ProcessedDetectionResult
import com.example.braillelens.services.ObjectDetectionService
import com.example.braillelens.ui.BrailleLensColors
import com.example.braillelens.utils.TTSManager
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import kotlin.text.toInt
import kotlin.times

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecognitionResultScreen(
    navController: NavController,
    detectionMode: String,
    imagePath: String,
    recognizedText: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val ttsManager = remember { TTSManager.getInstance(context) }
    val objectDetectionService = remember { ObjectDetectionService() }

    // Separate slider UI value from actual threshold used for detection
    var sliderValue by remember { mutableStateOf(0.25f) }
    var activeThreshold by remember { mutableStateOf(0.25f) }

    var isLoading by remember { mutableStateOf(true) }
    var detectionResult by remember { mutableStateOf<ProcessedDetectionResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Process image function remains the same
    val processImage = {
        coroutineScope.launch {
            try {
                isLoading = true
                errorMessage = null

                // Processing logic remains unchanged
                if (imagePath.startsWith("file:") || imagePath.startsWith("content:")) {
                    val uri = Uri.parse(imagePath)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    detectionResult = objectDetectionService.detectBrailleFromUri(
                        context,
                        uri,
                        detectionMode
                    )
                } else {
                    // Handle sample images
                    try {
                        val resourceId = imagePath.toInt()
                        originalBitmap = BitmapFactory.decodeResource(context.resources, resourceId)
                        detectionResult = objectDetectionService.detectBrailleFromDrawableResource(
                            context,
                            resourceId,
                            detectionMode
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
                            detectionMode
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

    // Only update the service threshold when activeThreshold changes
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
                            tint = androidx.compose.ui.graphics.Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Result - $detectionMode",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = androidx.compose.ui.graphics.Color.Black,
                        modifier = Modifier.padding(start = 8.dp)
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
            // Loading and error states remain unchanged
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
                // Original image display remains unchanged
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
                    // Processed Image display remains unchanged
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

                    // Modified confidence threshold slider
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
                                // Only update active threshold when sliding is complete
                                activeThreshold = sliderValue
                            },
                            valueRange = 0.05f..1.0f,
                            steps = 18, // Creates 19 positions (0.05, 0.10, 0.15, ..., 0.95)
                            colors = SliderDefaults.colors(
                                thumbColor = BrailleLensColors.accentRed,
                                activeTrackColor = BrailleLensColors.accentRed,
                                activeTickColor = BrailleLensColors.darkOlive,
                                inactiveTickColor = BrailleLensColors.darkOlive
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Rest of the UI remains unchanged
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = BrailleLensColors.lightCream
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        // Card content unchanged
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Detection Results", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Detected Text:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(result.detectionText, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Translated Text:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(result.translatedText, fontSize = 14.sp)
                        }
                    }

                    // Buttons row unchanged
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { ttsManager.speak(result.translatedText) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrailleLensColors.darkOlive
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.volume_up),
                                contentDescription = "Text to Speech"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Read Aloud")
                        }

                        Button(
                            onClick = { processImage() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrailleLensColors.darkOlive
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry Detection"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}