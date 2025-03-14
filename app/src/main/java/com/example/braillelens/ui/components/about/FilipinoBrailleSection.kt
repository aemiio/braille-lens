package com.example.braillelens.ui.components.about

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

@Composable
fun FilipinoBrailleSection() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "What is Filipino Braille?",
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
                painter = painterResource(id = R.drawable.filipino_braille),
                contentDescription = "Filipino Braille Example",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(160.dp)
                    .align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "In the Philippines, the Filipino Braille Code was adapted from the English Braille code to provide a standardized method for visually impaired individuals to read and write in Tagalog, as well as in several regional languages such as Ilocano, Cebuano, Hiligaynon, and Bicol. These standards were developed by the Philippine Printing House for the Blind and the Department of Education (World Blind Union, 2013).",
            fontSize = 16.sp,
            color = Color(0xFF333333),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "According to the World Blind Union (2013), Tagalog is the only language with available contracted words derived from five regional languages that have adapted the Filipino Braille Code. The code consists of 28 letters representing the Philippine alphabet, and the numbers and punctuation marks are similar to those found in traditional English Braille.",
            fontSize = 16.sp,
            color = Color(0xFF333333),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(28.dp))


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
                containerColor = Color(0xFF3700B3)
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
