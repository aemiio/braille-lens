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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.rememberNavController
import com.example.braillelens.R
import com.example.braillelens.ui.BrailleLensColors
import com.example.braillelens.ui.components.MediumRecognitionCard
import com.example.braillelens.ui.components.RecognitionCard
import com.example.braillelens.utils.EnableFullScreen
import com.example.braillelens.utils.WindowType
import com.example.braillelens.utils.rememberWindowSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Arrangement

@Composable
fun HomeScreen(openDrawer: () -> Unit) {
    EnableFullScreen()
    val windowSize = rememberWindowSize()
    when (windowSize.height) {
        WindowType.Compact -> {
            SmallHomeScreen(openDrawer = openDrawer)
        } else -> {
            MediumHomeScreen(openDrawer = openDrawer)
        }
    }
}

@Composable
fun SmallHomeScreen(openDrawer: () -> Unit) {
    val context = LocalContext.current
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BrailleLensColors.pastelGreen)
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

        // App Drawer Button
        IconButton(
            onClick = openDrawer,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
                .zIndex(3f)
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
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 55.dp, topEnd = 55.dp),
                    clip = false
                )
                .background(
                    BrailleLensColors.backgroundGrey,
                    shape = RoundedCornerShape(topStart = 55.dp, topEnd = 55.dp)
                )
                .padding(20.dp)
                .zIndex(0f)
        ) {
            // Centered Logo & Title with minimal padding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.braille_logo),
                    contentDescription = "Braille Lens Logo",
                    modifier = Modifier.size(40.dp)
                )

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "Braille Lens",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrailleLensColors.darkOrange
                    )
                    Text(
                        text = "Your Vision, Our Mission",
                        fontSize = 10.sp,
                        color = BrailleLensColors.fontBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Recognition Card
            RecognitionCard(
                navController = navController,
                context = context
            )
        }
    }
}

@Composable
fun MediumHomeScreen(openDrawer: () -> Unit) {
    val context = LocalContext.current
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BrailleLensColors.pastelGreen)
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

        // App Drawer Button
        IconButton(
            onClick = openDrawer,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
                .zIndex(3f)
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
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 55.dp, topEnd = 55.dp),
                    clip = false
                )
                .background(
                    BrailleLensColors.backgroundGrey,
                    shape = RoundedCornerShape(topStart = 55.dp, topEnd = 55.dp)
                )
                .padding(16.dp)
                .zIndex(0f)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            // Centered Logo & Title with minimal padding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.braille_logo),
                    contentDescription = "Braille Lens Logo",
                    modifier = Modifier.size(50.dp)
                )

                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = "Braille Lens",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrailleLensColors.darkOrange
                    )
                    Text(
                        text = "Your Vision, Our Mission",
                        fontSize = 12.sp,
                        color = BrailleLensColors.fontBlack
                    )
                }
            }


            // Recognition Card
            MediumRecognitionCard(
                navController = navController,
                context = context
            )
        }
    }
}