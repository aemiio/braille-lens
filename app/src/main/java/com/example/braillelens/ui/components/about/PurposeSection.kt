package com.example.braillelens.ui.components.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun PurposeSection() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "The purpose of the Braille-Lens application is to enhance Braille literacy " +
                    "and accessibility for visually impaired individuals in the Philippines by " +
                    "recognizing both Grade 1 and Grade 2 Filipino-Tagalog Braille characters, " +
                    "digits, and contractions.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "It features offline Text-to-Speech (TTS) functionality that converts " +
                    "recognized Braille text into spoken words, an image capture/import option " +
                    "for recognizing Braille text from images, and a Tagalog Braille dictionary " +
                    "for user reference.\n\nBy empowering users through these functionalities, the app not only " +
                    "promotes literacy and educational opportunities for visually impaired " +
                    "individuals but also raises awareness of the Filipino Braille system among " +
                    "sighted individuals, fostering a more inclusive environment for " +
                    "communication and learning.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}