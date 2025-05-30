package com.aemiio.braillelens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.aemiio.braillelens.R
import com.aemiio.braillelens.ui.BrailleLensColors
import com.aemiio.braillelens.ui.components.home.GuideBottomSheet
import com.aemiio.braillelens.ui.components.home.RecognitionCard
import com.aemiio.braillelens.utils.EnableFullScreen
import com.aemiio.braillelens.utils.WindowType
import com.aemiio.braillelens.utils.rememberWindowSize

@Composable
fun HomeScreen(openDrawer: () -> Unit, navController: NavController) {
    EnableFullScreen()
    val windowSize = rememberWindowSize()
    when (windowSize.height) {
        WindowType.Compact -> {
            SmallHomeScreen(openDrawer = openDrawer, navController = navController)
        }
        else -> {
            MediumHomeScreen(openDrawer = openDrawer, navController = navController)
        }
    }
}

@Composable
fun SmallHomeScreen(openDrawer: () -> Unit, navController: NavController) {
    val context = LocalContext.current

    var showGuideSheet by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primary)
    ) {

        Image(
            painter = painterResource(id = R.drawable.home),
            contentDescription = "Home Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .align(Alignment.TopCenter)
                .zIndex(1f)
        )

        IconButton(
            onClick = openDrawer,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopStart)
                .zIndex(3f)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.menu_24px),
                contentDescription = "Open Drawer",
                tint = BrailleLensColors.accentRed
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 200.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 55.dp, topEnd = 55.dp),
                    clip = false
                )
                .background(
                    MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(topStart = 55.dp, topEnd = 55.dp)
                )
                .padding(20.dp)
                .zIndex(0f)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
                    .clickable { showGuideSheet = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.quick_reference_24px),
                    contentDescription = "Guide",
                    tint = BrailleLensColors.darkOlive,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "How to Use Braille lens",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrailleLensColors.darkOlive
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                RecognitionCard(
                    navController = navController,
                    context = context
                )
            }
            GuideBottomSheet(
                showGuideSheet = showGuideSheet,
                onDismiss = { showGuideSheet = false }
            )
        }
    }
}

@Composable
fun MediumHomeScreen(openDrawer: () -> Unit, navController: NavController) {
    val context = LocalContext.current

    var showGuideSheet by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primary)
    ) {

        Image(
            painter = painterResource(id = R.drawable.home),
            contentDescription = "Home Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.TopCenter)
                .zIndex(1f)
        )


        IconButton(
            onClick = openDrawer,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopStart)
                .zIndex(3f)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.menu_24px),
                contentDescription = "Open Drawer",
                tint = BrailleLensColors.accentRed
            )
        }



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
                    MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(topStart = 55.dp, topEnd = 55.dp)
                )
                .padding(16.dp)
                .zIndex(0f)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 10.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Center
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.braille_logo),
//                    contentDescription = "Braille Lens Logo",
//                    modifier = Modifier.size(50.dp)
//                )

//                Column(modifier = Modifier.padding(start = 16.dp)) {
//                    Text(
//                        text = "Braille-Lens",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = BrailleLensColors.darkOrange
//                    )
//                    Text(
//                        text = "Making Braille Accessible",
//                        fontSize = 12.sp,
//                        color = MaterialTheme.colorScheme.onBackground
//                    )
//                }
//            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
                    .clickable { showGuideSheet = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.quick_reference_24px),
                    contentDescription = "Guide",
                    tint = BrailleLensColors.darkOlive,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "How to Use Braille-Lens",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrailleLensColors.darkOlive
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                RecognitionCard(
                    navController = navController,
                    context = context
                )
            }
            GuideBottomSheet(
                showGuideSheet = showGuideSheet,
                onDismiss = { showGuideSheet = false }
            )
        }
    }
}