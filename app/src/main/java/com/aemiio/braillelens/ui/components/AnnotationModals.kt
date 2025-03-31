package com.aemiio.braillelens.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aemiio.braillelens.ui.BrailleLensColors

/**
 * Check if the user has accepted the annotation terms before
 */
fun hasAcceptedAnnotationTerms(context: Context): Boolean {
    val sharedPrefs = context.getSharedPreferences("annotation_prefs", Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean("terms_accepted", false)
}

/**
 * Save the user's terms acceptance status
 */
fun saveTermsAcceptanceStatus(context: Context, accepted: Boolean) {
    val sharedPrefs = context.getSharedPreferences("annotation_prefs", Context.MODE_PRIVATE)
    sharedPrefs.edit().putBoolean("terms_accepted", accepted).apply()
}

/**
 * Terms and Conditions modal for annotation
 */
@Composable
fun TermsAndConditionsModal(
    showTerms: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onDismissRequest: () -> Unit = onDecline
) {
    if (!showTerms) return
    
    val scrollState = rememberScrollState()
    var buttonsEnabled by remember { mutableStateOf(false) }
    
    // This will track if user has scrolled to the bottom
    val hasScrolledToBottom by remember {
        derivedStateOf {
            scrollState.value >= (scrollState.maxValue - 100)
        }
    }
    
    // Enable buttons once scrolled to bottom
    LaunchedEffect(hasScrolledToBottom) {
        if (hasScrolledToBottom) {
            buttonsEnabled = true
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Braille-Lens Annotation Terms",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(scrollState)
                    ) {
                        TermsContent()
                    }
                    

                    if (!hasScrolledToBottom) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )
                                )
                        )
                    }
                }
                

                if (!buttonsEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Scroll down",
                            tint = BrailleLensColors.darkOlive
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Scroll to the bottom to enable buttons",
                            fontSize = 14.sp,
                            color = BrailleLensColors.darkOlive,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    OutlinedButton(
                        onClick = onDecline,
                        enabled = buttonsEnabled,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrailleLensColors.accentRed
                        )
                    ) {
                        Text("Decline")
                    }
                    

                    Button(
                        onClick = onAccept,
                        enabled = buttonsEnabled,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrailleLensColors.darkOlive,
                            disabledContainerColor = BrailleLensColors.darkOlive.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}

/**
 * Styled content for the terms and conditions with proper formatting
 */
@Composable
private fun TermsContent() {
    val context = LocalContext.current
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "By accepting these terms, you agree to contribute your annotations (bounding boxes, class names, and images) to help improve the Filipino Braille recognition model.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Justify
        )
        
        SectionTitle(text = "1. Data Collection and Usage")
        BulletPoint(text = "The images you annotate, along with your bounding box corrections and class labels, will be stored in our public dataset using Supabase.")
        BulletPoint(text = "This data will be used exclusively for retraining the Braille-Lens model to enhance accuracy in recognizing Filipino Braille (Grade 1 and Grade 2).")
        BulletPoint(text = "Your contributions directly support Braille accessibility and literacy in the Philippines.")
        
        SectionTitle(text = "2. Privacy and Anonymity")
        BulletPoint(text = "Annotations are collected anonymously; no personal information is stored or linked to your contributions.")
        BulletPoint(text = "Uploaded images will be stored in a publicly accessible Supabase bucket and may be included in future public Braille datasets for research.")
        BulletPoint(text = "Images and annotations will only be used for Braille recognition training and development.")
        
        SectionTitle(text = "3. Image Content")
        BulletPoint(text = "Only annotate images containing Braille text. Do not upload or edit non-Braille images.")
        BulletPoint(text = "Do not annotate images containing personal, sensitive, or copyrighted content.")
        BulletPoint(text = "We reserve the right to remove inappropriate content from the dataset.")
        
        SectionTitle(text = "4. Ownership and Licensing")
        BulletPoint(text = "By submitting annotations, you grant us the right to use, modify, and distribute your contributions as part of the Braille-Lens dataset.")
        BulletPoint(text = "Your contributions become part of a public resource for Braille accessibility and may be used in future research projects.")
        BulletPoint(text = "The improved Braille recognition model will be available to benefit the entire Braille community.")
        
        SectionTitle(text = "5. Usage Limitations")
        BulletPoint(text = "The annotation editor is provided for educational and contributory purposes only.")
        BulletPoint(text = "Please use the annotation tools responsibly and accurately.")
        
        SectionTitle(text = "6. Updates to Terms")
        BulletPoint(text = "These terms may be updated periodically to reflect changes in data usage or policies.")
        BulletPoint(text = "Significant changes will be communicated within the Braille-Lens app.")
        BulletPoint(text = "Continuing to use the annotation editor after an update means you accept the revised terms.")
        
        SectionTitle(text = "7. Opting Out & Data Removal")
        BulletPoint(text = "You can stop contributing at any time by declining these terms when prompted.")
        BulletPoint(text = "Previously submitted annotations will remain in the dataset unless you specifically request removal.")
        

        Row(
            modifier = Modifier.padding(start = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "•",
                fontSize = 14.sp,
                color = BrailleLensColors.darkOlive,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp, top = 2.dp)
            )
            
            Column {
                Text(
                    text = "If you want to remove your contributions, please contact us via email:",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Justify
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "jyramae.celajes@cvsu.edu.ph",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:jyramae.celajes@cvsu.edu.ph")
                            }
                            context.startActivity(intent)
                        }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Thank You for Your Contribution!",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = BrailleLensColors.darkOlive,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Your efforts directly contribute to improving Filipino-Tagalog Braille literacy and accessibility through a mobile application designed for both visually impaired individuals and the general public.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = BrailleLensColors.darkOlive,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(start = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 14.sp,
            color = BrailleLensColors.darkOlive,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Justify
        )
    }
}

/**
 * A button to view the terms and conditions again
 */
@Composable
fun ViewTermsButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = BrailleLensColors.darkOlive
        ),
    ) {
        Text(
            text = "Terms",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Help button to open annotation help bottom sheet
 */
@Composable
fun HelpButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(BrailleLensColors.darkOlive)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Help",
            tint = BrailleLensColors.darkOlive,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "How to Use the Annotation Editor",
            fontSize = 14.sp,
            color = BrailleLensColors.darkOlive
        )
    }
}

/**
 * Help bottom sheet for annotation editor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationHelpBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit
) {
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "How to Use the Annotation Editor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrailleLensColors.darkOlive
                )
                
                Text(
                    text = "The annotation editor lets you review, correct, and refine Braille detections to improve the Filipino Braille recognition model.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                
                Text(
                    text = "1. Modes of Annotation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrailleLensColors.darkOlive,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                // View Mode
                HelpModeItem(
                    icon = Icons.Default.Search,
                    title = "View Mode",
                    descriptions = listOf(
                        "Displays detected Braille bounding boxes.",
                        "Allows users to review the results before making changes."
                    )
                )
                
                // Add Mode
                HelpModeItem(
                    icon = Icons.Default.Add,
                    title = "Add Mode",
                    descriptions = listOf(
                        "Enables adding new bounding boxes.",
                        "Shows a list of available class labels based on the selected model.",
                        "Users can draw new bounding boxes for Braille characters."
                    )
                )
                
                // Edit Mode
                HelpModeItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Mode",
                    descriptions = listOf(
                        "Allows modification of existing bounding boxes.",
                        "Selecting a box displays: Class Name, Position (X, Y coordinates), Size (Width & Height).",
                        "Users can change the class label if incorrect.",
                        "Adjust the size and position for accuracy."
                    )
                )
                
                // Delete Mode
                HelpModeItem(
                    icon = Icons.Default.Delete,
                    title = "Delete Mode",
                    descriptions = listOf(
                        "Users can remove incorrect bounding boxes.",
                        "Double-tap on a box to delete it."
                    )
                )
                
                // Saving Annotations
                Text(
                    text = "2. Saving Your Annotations",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrailleLensColors.darkOlive,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                Text(
                    text = "After making corrections, press \"Save Annotations\" to:",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Column(
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                ) {
                    HelpCheckItem(text = "Store updated bounding boxes and labels.")
                    HelpCheckItem(text = "Save the image and annotation data to the database.")
                    HelpCheckItem(text = "Help improve Filipino Braille recognition accuracy.")
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
    icon: ImageVector,
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
                imageVector = icon,
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
private fun HelpCheckItem(text: String) {
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
