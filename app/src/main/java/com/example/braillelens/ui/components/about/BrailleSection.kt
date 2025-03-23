package com.example.braillelens.ui.components.about

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.braillelens.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.ui.layout.ContentScale
import com.example.braillelens.ui.BrailleLensColors

@Composable
fun BrailleSection() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "What is Braille?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(16.dp))


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .align(Alignment.CenterHorizontally)
        ) {
            Image(
                painter = painterResource(id = R.drawable.braille_cell),
                contentDescription = "Braille Example",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(160.dp)
                    .align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Braille is a tactile writing and reading system designed for visually impaired individuals, enabling them to read and write through the use of raised dots. Each Braille character is formed by a combination of six dots arranged in a 3x2 matrix, allowing for a variety of patterns to represent letters, numbers, punctuation marks, and even whole words.",
            fontSize = 16.sp,
            color = Color(0xFF333333),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "How to read and write Braille?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "When reading Braille, the sequence starts from left to right (123-456), to enable tactile recognition of the patterns by feeling the raised dots. However, when writing Braille using a slate and stylus, the sequence is mirrored (456-123). This is because the writer punches dots from right to left on the back of the paper, so the Braille appears correctly embossed on the front.",
            fontSize = 16.sp,
            color = Color(0xFF333333),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(28.dp))


        Button(
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://brailleworks.com/braille-resources/braille-alphabet/")
                )
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrailleLensColors.darkOlive
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