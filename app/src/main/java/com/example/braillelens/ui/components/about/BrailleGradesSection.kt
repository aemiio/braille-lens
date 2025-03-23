package com.example.braillelens.ui.components.about

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.braillelens.ui.BrailleLensColors


@Composable
fun BrailleGradesSection() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {

        Text(
            text = "Grade 1",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "A letter-by-letter transcription where each Braille cell corresponds directly to a letter, number, or symbol. It includes the full alphabet, numerals, and a limited number of punctuation marks but does not include any contractions or shorthand forms that represent whole words or groups of letters.",
            fontSize = 16.sp,
            color = Color(0xFF333333),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(20.dp))


        Text(
            text = "Grade 2",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "An advanced system including contractions and shorthand, allowing efficient reading and writing through abbreviated representations. In Grade 2 Braille, certain common words and letter groups are represented by single Braille cells (one-cell contractions), which saves time when reading and reduces the amount of paper required for written texts.",
            fontSize = 16.sp,
            color = Color(0xFF333333),
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Additionally, Grade 2 Braille incorporates part-word contractions, where portions of words can be represented by specific Braille characters. For example, in Filipino Grade 2 Braille, there are specific dot patterns that stand for words like \"kaya\" or \"ngayon,\" thus helping to streamline the reading process.",
            fontSize = 16.sp,
            color = Color(0xFF333333),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://archive.org/details/instructionmanua00depa")
                )
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 8.dp),
            colors = ButtonDefaults.buttonColors(
                BrailleLensColors.darkOlive
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Learn more",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.ArrowForward,
                    contentDescription = "Learn more",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}