package com.example.braillelens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.braillelens.R
import com.example.braillelens.ui.components.RecognitionCard

@Composable
fun HomeScreen(openDrawer: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Top Image
        Image(
            painter = painterResource(id = R.drawable.home),
            contentDescription = "Home Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.TopCenter)
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 250.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                )
                .padding(20.dp)
        ) {
            // Drawer Button
            IconButton(onClick = openDrawer) {
                Icon(
                    painter = painterResource(id = R.drawable.menu_24px),
                    contentDescription = "Open Drawer",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Logo & Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.braille_logo),
                    contentDescription = "Braille Lens Logo",
                    modifier = Modifier.size(80.dp)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "Braille Lens",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Your Vision, Our Mission",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recognition Card
            RecognitionCard(
                navController = navController,
                context = context
            )
        }
    }
}
