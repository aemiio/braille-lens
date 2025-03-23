package com.example.braillelens.ui.components.about

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun ResourcesSection() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {

        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = "Braille:",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(modifier = Modifier.height(8.dp))

        ResourceLink("Braille Alphabet", "https://brailleworks.com/braille-resources/braille-alphabet/")
        ResourceLink("Introduction to Braille", "https://www.afb.org/blindness-and-low-vision/braille")
        ResourceLink("Louis Braille Biography", "https://www.biography.com/inventor/louis-braille")

        Spacer(modifier = Modifier.height(20.dp))


        Text(
            text = "Filipino Braille:",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(modifier = Modifier.height(8.dp))

        ResourceLink("Filipino Braille Code Manual", "https://archive.org/details/instructionmanua00depa")
        ResourceLink("RESOURCES FOR THE BLIND, INC", "https://blind.org.ph/")
        ResourceLink("Braille ng Pilipinas", "https://tl.wikipedia.org/wiki/Braille_ng_Pilipinas")

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ResourceLink(text: String, url: String) {
    val context = LocalContext.current

    Text(
        text = text,
        fontSize = 16.sp,
        color = Color(0xFF3366CC),
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                context.startActivity(intent)
            }
    )
}
