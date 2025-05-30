package com.aemiio.braillelens.ui.components.home

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.aemiio.braillelens.R
import com.aemiio.braillelens.ui.BrailleLensColors

@Composable
fun VideoPlayer(videoUri: Uri) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideBottomSheet(
    showGuideSheet: Boolean,
    onDismiss: () -> Unit
) {
    if (showGuideSheet) {
        val sheetState = rememberModalBottomSheetState()
        val context = LocalContext.current
        val videoUri = "android.resource://${context.packageName}/${R.raw.demo_video}".toUri()

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Getting Started with Braille-lens",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrailleLensColors.darkOlive,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Demo Video",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrailleLensColors.darkOlive,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                VideoPlayer(videoUri)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "📘 How to Use Braille-lens:\n\n" +
                            "1. From the home screen, you can:\n" +
                            "   • Capture an image using the camera\n" +
                            "   • Import an existing Braille image\n" +
                            "   • Use a sample image for testing\n" +
                            "2. Align the camera properly over a Braille document — shadows and raised dots must be clearly visible.\n" +
                            "3. Capture or import the image.\n" +
                            "4. The app detects Grade 1 Braille, Grade 2 Braille, or both.\n" +
                            "5. Tap the Read Aloud button 🔊 to hear the translated text.\n" +
                            "6. Use the Confidence Threshold slider to adjust the detection sensitivity.\n\n" +
                            "📚 Dictionary:\n" +
                            "Includes Filipino Grade 1 and Grade 2 Braille meanings.\n" +
                            "You can copy Braille dot patterns or listen to the audio pronunciation.\n\n" +
                            "💡 Tips for Best Results:\n" +
                            "   • Use clear lighting and a white background.\n" +
                            "   • Avoid blurry or slanted images.\n" +
                            "Examples of good lighting are shown below:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                val imageList = listOf(
                    R.drawable.guide1,
                    R.drawable.guide2,
                    R.drawable.guide3,
                    R.drawable.guide4,
                    R.drawable.guide5
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(imageList) { imageRes ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier
                                .width(200.dp)
                                .height(200.dp)
                        ) {
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = "Sample Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrailleLensColors.darkOlive
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Text("Close Guide")
                }
            }
        }
    }
}
