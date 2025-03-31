package com.aemiio.braillelens.ui.components

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Terms for Data Contribution",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Terms content in a scrollable box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = termsAndConditionsText,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Gradient fade at the bottom to indicate more content
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
                
                // Scroll instruction
                if (!buttonsEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please scroll to the bottom to continue",
                        fontSize = 12.sp,
                        color = BrailleLensColors.darkOlive,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Decline button
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
                    
                    // Accept button
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
 * Hook to handle terms acceptance flow
 */
@Composable
fun HandleAnnotationTerms(
    onAccept: () -> Unit,
    onDecline: () -> Unit
): Boolean {
    val context = LocalContext.current
    var showTerms by remember { mutableStateOf(false) }
    
    // Check if terms have already been accepted
    val alreadyAccepted = remember { hasAcceptedAnnotationTerms(context) }
    
    // Show terms if not already accepted
    LaunchedEffect(Unit) {
        if (!alreadyAccepted) {
            showTerms = true
        }
    }
    
    if (showTerms) {
        TermsAndConditionsModal(
            showTerms = true,
            onAccept = {
                saveTermsAcceptanceStatus(context, true)
                showTerms = false
                onAccept()
            },
            onDecline = {
                saveTermsAcceptanceStatus(context, false)
                showTerms = false
                onDecline()
            }
        )
    }
    
    return alreadyAccepted || !showTerms
}

// Terms and conditions text content
private val termsAndConditionsText = """
By accepting these terms, you agree to contribute your annotations to help improve our braille recognition technology. Here's how your data will be used:

1. Data Collection and Usage
   • The images you annotate and your annotations may be stored in our database.
   • This data will be used to train and improve our braille recognition algorithms.
   • Your contributions help make braille more accessible to everyone.

2. Privacy and Anonymity
   • All annotations are collected anonymously.
   • We do not store personal identifying information alongside your annotations.
   • Images are stored securely and used only for training purposes.

3. Image Content
   • Please only annotate images containing braille text.
   • Do not annotate images containing personal information or sensitive content.
   • We reserve the right to remove inappropriate content.

4. Ownership and Licensing
   • Annotations you submit become part of our training dataset.
   • Your contributions help build a public resource for braille accessibility.
   • The improved algorithms will benefit the entire community of braille users.

5. Usage Limitations
   • The annotation tools are provided for educational and contributory purposes.
   • Please use the tools responsibly and accurately.
   • Intentional submission of incorrect annotations may result in restrictions.

6. Updates to Terms
   • These terms may be updated periodically.
   • Significant changes will be communicated within the app.
   • Continued use after changes constitutes acceptance of new terms.

7. Opting Out
   • You can stop contributing at any time by declining these terms.
   • Previously submitted annotations will remain in our dataset unless you specifically request removal.
   • To request removal of your annotations, please contact our support team.

Thank you for helping make braille more accessible through your contributions! Your efforts directly improve the accuracy of our recognition technology and help people with visual impairments access more written content.
""".trimIndent()
