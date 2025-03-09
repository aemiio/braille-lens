package com.example.braillelens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.rememberNavController
import com.example.braillelens.R
import com.example.braillelens.ui.BrailleLensColors
import com.example.braillelens.ui.components.RecognitionCard

@Composable
fun HomeScreen(openDrawer: () -> Unit) {
    val context = LocalContext.current
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BrailleLensColors.backgroundGrey)
    ) {
        // Top Image reaching the status bar
        Image(
            painter = painterResource(id = R.drawable.home),
            contentDescription = "Home Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.TopCenter)
                .zIndex(1f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // App Drawer Button
        IconButton(
            onClick = openDrawer,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.menu_24px),
                contentDescription = "Open Drawer",
                tint = BrailleLensColors.accentRed
            )
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 300.dp)
                .background(
                    BrailleLensColors.backgroundCream,
                    shape = RoundedCornerShape(topStart = 55.dp, topEnd = 55.dp)
                )
                .padding(20.dp)
                .zIndex(0f)
        ) {

            Spacer(modifier = Modifier.height(20.dp))
            // Logo & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = painterResource(id = R.drawable.braille_logo),
                    contentDescription = "Braille Lens Logo",
                    modifier = Modifier.size(80.dp)
                )

                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = "Braille Lens",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrailleLensColors.darkTeal
                    )
                    Text(
                        text = "Your Vision, Our Mission",
                        fontSize = 16.sp,
                        color = BrailleLensColors.darkOlive
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
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