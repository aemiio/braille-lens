package com.example.braillelens.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.braillelens.ui.BrailleLensColors
import com.example.braillelens.utils.BrailleImageUtils
import java.io.File
import androidx.camera.core.Camera
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput


@Composable
fun CaptureScreen(navController: NavController, detectionMode: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Configure the image capture use case to capture a 640x640 image
    val imageCaptureUseCase = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetResolution(android.util.Size(640, 640))
            .build()
    }

    val outputDirectory = context.getExternalFilesDir(null)
    val fileName = "braille_${System.currentTimeMillis()}.jpg"
    val file = File(outputDirectory, fileName)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (hasCameraPermission) {
            Column(modifier = Modifier.fillMaxSize()) {

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    color = Color.White
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }

                        Text(
                            text = "Capture Braille",
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }


                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(1f)
                            .border(width = 2.dp, color = BrailleLensColors.pastelGreen, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        var camera by remember { mutableStateOf<Camera?>(null) }
                        var minZoom by remember { mutableStateOf(1f) }
                        var maxZoom by remember { mutableStateOf(5f) }
                        var scaleFactor by remember { mutableStateOf(1f) }

                        AndroidView(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, _, zoom, _ ->
                                        scaleFactor = (scaleFactor * zoom).coerceIn(minZoom, maxZoom)
                                        camera?.cameraControl?.setZoomRatio(scaleFactor)
                                    }
                                },
                            factory = { context ->
                                val previewView = PreviewView(context).apply {
                                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }

                                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder()
                                        .setTargetResolution(android.util.Size(640, 640))
                                        .build()
                                        .also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }

                                    try {
                                        cameraProvider.unbindAll()
                                        camera = cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageCaptureUseCase
                                        )

                                        // Get zoom limits
                                        val cameraInfo = camera?.cameraInfo
                                        if (cameraInfo?.zoomState?.value != null) {
                                            minZoom = cameraInfo.zoomState.value?.minZoomRatio ?: 1f
                                            maxZoom = cameraInfo.zoomState.value?.maxZoomRatio ?: 5f
                                        }

                                    } catch (exc: Exception) {
                                        exc.printStackTrace()
                                    }
                                }, ContextCompat.getMainExecutor(context))

                                previewView
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BrailleLensColors.pastelOrange
                    ) {
                        Text(
                            text = detectionMode,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = BrailleLensColors.fontBlack,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))


                    FloatingActionButton(
                        onClick = {
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                            imageCaptureUseCase.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        // Convert to a proper file:// URI
                                        val fileUri = Uri.fromFile(file)
                                        imageUri = fileUri

                                        BrailleImageUtils.saveCapturedData(
                                            context,
                                            detectionMode,
                                            fileUri.toString()  // Use URI string with file:// scheme instead of raw path
                                        )

                                        // Pass the URI string in navigation
                                        val encodedMode = Uri.encode(detectionMode)
                                        val encodedPath = Uri.encode(fileUri.toString())
                                        navController.navigate("result/$encodedMode/$encodedPath")
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        exception.printStackTrace()
                                    }
                                }
                            )
                        },
                        containerColor = BrailleLensColors.darkOlive,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_camera),
                            contentDescription = "Capture",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        } else {

            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.85f)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_camera),
                        contentDescription = "Camera Permission",
                        tint = Color.Black,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Camera Access Required",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Please allow camera access to scan and detect Braille characters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrailleLensColors.darkOlive)
                    ) {
                        Text(
                            "GRANT ACCESS",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
