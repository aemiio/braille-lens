package com.aemiio.braillelens.ui.components.result

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aemiio.braillelens.R
import com.aemiio.braillelens.ui.BrailleLensColors
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit
) {
    if (showBottomSheet) {
        val sheetState = rememberModalBottomSheetState()

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
                    text = "Understanding Results",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrailleLensColors.darkOlive,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HelpModeItem(
                    icon = painterResource(id = R.drawable.camera_24px),
                    title = "Recognition Accuracy",
                    descriptions = listOf(
                        "Trained on Filipino Braille written using slate and stylus (embossed on paper) and simulated Braille (black dots).",
                        "Recognition may be inaccurate for slanted, distorted, or low-contrast Braille.",
                        "Translations follow Filipino Braille rules (e.g., capital, number, contraction signs)."
                    )
                )

                // 1. Detection Results Section
                HelpModeItem(
                    icon = painterResource(id = R.drawable.image_24px),
                    title = "Detection Visualization",
                    descriptions = listOf(
                        "Red boxes indicate detected Braille cells.",
                        "Each box is labeled with the corresponding Filipino character or contraction.",
                        "Translated text follows Filipino Braille reading rules and cell order."
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Confidence Threshold Section
                HelpModeItem(
                    icon = painterResource(id = R.drawable.linear_scale),
                    title = "Confidence Threshold",
                    descriptions = listOf(
                        "Controls how certain detections must be to appear.",
                        "Higher values = fewer, more accurate boxes.",
                        "Lower values = more boxes, but may include errors."
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Recognition Modes
                HelpModeItem(
                    icon = painterResource(id = R.drawable.language),
                    title = "Recognition Modes",
                    descriptions = listOf(
                        "Grade 1: Letter-for-letter Filipino Braille translation.",
                        "Grade 2: Includes Filipino contractions and shortcuts.",
                        "Both Grades: Attempts to detect both types in one pass."
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Available Actions Section
                Text(
                    text = "Available Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrailleLensColors.darkOlive,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Column(
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                ) {
                    ActionItem(text = "Read Aloud: Speak the translated Filipino Braille.")
                    ActionItem(text = "Retry: Reprocess the current image.")
                    ActionItem(text = "Edit: Adjust and save boxes to help train the model.")
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
                    Text("Got it!")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HelpModeItem(
    icon: Painter,
    title: String,
    descriptions: List<String>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = BrailleLensColors.darkOlive.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
                tint = BrailleLensColors.darkOlive,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            descriptions.forEach { description ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = BrailleLensColors.darkOlive,
                        modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                    )
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = BrailleLensColors.darkOlive,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}