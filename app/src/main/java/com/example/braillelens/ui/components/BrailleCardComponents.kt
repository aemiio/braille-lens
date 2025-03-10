package com.example.braillelens.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.braillelens.ui.BrailleLensColors

@Composable
fun BrailleCardItem(card: BrailleCardData) {
    var showModal by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { showModal = true }
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrailleLensColors.backgroundCream
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                // Braille character
                Text(
                    text = card.braille,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Label
                Text(
                    text = card.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showModal) {
        ShowBrailleCardModal(card = card, onDismiss = { showModal = false })
    }
}

@Composable
fun FilterBubble(text: String, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) BrailleLensColors.darkOlive
    else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (selected) BrailleLensColors.darkOlive
    else BrailleLensColors.fontBlack

    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = BrailleLensColors.backgroundGrey,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, backgroundColor),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowBrailleCardModal(card: BrailleCardData, onDismiss: () -> Unit) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                color = BrailleLensColors.backgroundCream
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    BrailleCard(card = card)
                }
            }
        }
    )
}