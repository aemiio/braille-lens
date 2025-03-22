package com.example.braillelens.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import java.io.File
import com.example.braillelens.utils.BrailleImageUtils

@Composable
fun CaptureScreen(navController: NavController, detectionMode: String) {
    val context = LocalContext.current
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    val fileName = "braille_${System.currentTimeMillis()}.jpg"
    val file = File(context.getExternalFilesDir(null), fileName)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            capturedImageUri = uri
            // Save the detection mode and image path
            BrailleImageUtils.saveCapturedData(context, detectionMode, file.absolutePath)
            navController.navigate("result/$detectionMode/${file.absolutePath}")
        } else {
            // Handle camera cancellation
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Back button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera preview frame
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .border(2.dp, Color.White, RoundedCornerShape(8.dp))
            ) {
                capturedImageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Captured Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = "Position braille text within the frame",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Retake button
            Button(
                onClick = { launcher.launch(uri) }
            ) {
                Text("Retake Photo")
            }
        }
    }
}