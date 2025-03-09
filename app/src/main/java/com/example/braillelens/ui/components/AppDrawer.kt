package com.example.braillelens.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.braillelens.R
import com.example.braillelens.ui.BackgroundGrey
import com.example.braillelens.ui.FontBlack

@Composable
fun AppDrawer(
    currentRoute: String = "home",
    onItemSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGrey)
            .padding(vertical = 24.dp)
    ) {
        // Header with Logo and App Title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.braille_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Braille-Lens",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Your Vision, Our Mission",
                        fontSize = 14.sp,
                        color = FontBlack
                    )
                }
            }
        }

        // Divider between header and navigation items
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Items
        NavigationItem(
            icon = painterResource(id = R.drawable.home_24px),
            text = "Home",
            isSelected = currentRoute == "home",
            onClick = { onItemSelected("home") }
        )

        NavigationItem(
            icon = painterResource(id = R.drawable.dictionary_24px),
            text = "Dictionary",
            isSelected = currentRoute == "dictionary",
            onClick = { onItemSelected("dictionary") }
        )

        NavigationItem(
            icon = painterResource(id = R.drawable.info_24px),
            text = "About",
            isSelected = currentRoute == "about",
            onClick = { onItemSelected("about") }
        )

    }
}

@Composable
fun NavigationItem(
    icon: Painter,
    text: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = BackgroundGrey

    val textColor = FontBlack

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )


        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}