package com.aemiio.braillelens.ui.components.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aemiio.braillelens.R
import com.aemiio.braillelens.ui.BrailleLensColors

@Composable
fun CustomNavigationBar(
    currentScreen: String = "home",
    onItemSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(
                elevation = 8.dp,
                shape = RectangleShape,
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(vertical = 0.dp),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = BrailleLensColors.pastelGreen,
            tonalElevation = 0.dp
        ) {

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.home_24px),
                        contentDescription = "Home",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "Home",
                        fontSize = 12.sp,
                        fontWeight = if (currentScreen == "home") FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = currentScreen == "home",
                onClick = { onItemSelected("home") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrailleLensColors.darkOlive,
                    selectedTextColor = BrailleLensColors.darkOlive,
                    unselectedIconColor = BrailleLensColors.pastelGreen,
                    unselectedTextColor = BrailleLensColors.pastelGreen,
                    indicatorColor = BrailleLensColors.backgroundGrey
                )
            )


            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.dictionary_24px),
                        contentDescription = "Dictionary",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "Dictionary",
                        fontSize = 12.sp,
                        fontWeight = if (currentScreen == "dictionary") FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = currentScreen == "dictionary",
                onClick = { onItemSelected("dictionary") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrailleLensColors.darkOlive,
                    selectedTextColor = BrailleLensColors.darkOlive,
                    unselectedIconColor = BrailleLensColors.pastelGreen,
                    unselectedTextColor = BrailleLensColors.pastelGreen,
                    indicatorColor = BrailleLensColors.backgroundGrey
                )
            )


            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.info_24px),
                        contentDescription = "About",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "About",
                        fontSize = 12.sp,
                        fontWeight = if (currentScreen == "about") FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = currentScreen == "about",
                onClick = { onItemSelected("about") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrailleLensColors.darkOlive,
                    selectedTextColor = BrailleLensColors.darkOlive,
                    unselectedIconColor = BrailleLensColors.pastelGreen,
                    unselectedTextColor = BrailleLensColors.pastelGreen,
                    indicatorColor = BrailleLensColors.backgroundGrey
                )
            )
        }
    }
}