package com.aemiio.braillelens.ui.components.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aemiio.braillelens.R

@Composable
fun AppDrawer(
    currentRoute: String = "home",
    onItemSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 24.dp)
    ) {

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
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Your Vision, Our Mission",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }


        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Spacer(modifier = Modifier.height(8.dp))


        NavigationItem(
            icon = painterResource(id = R.drawable.home_24px),
            text = "Home",
            isSelected = currentRoute == "home",
            onClick = { onItemSelected("home") },
            color = MaterialTheme.colorScheme.onSurface,
            iconTint = MaterialTheme.colorScheme.onSurface
        )

        NavigationItem(
            icon = painterResource(id = R.drawable.dictionary_24px),
            text = "Dictionary",
            isSelected = currentRoute == "dictionary",
            onClick = { onItemSelected("dictionary") },
            color = MaterialTheme.colorScheme.onSurface,
            iconTint = MaterialTheme.colorScheme.onSurface
        )

        NavigationItem(
            icon = painterResource(id = R.drawable.info_24px),
            text = "About",
            isSelected = currentRoute == "about",
            onClick = { onItemSelected("about") },
            color = MaterialTheme.colorScheme.onSurface,
            iconTint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun NavigationItem(
    icon: Painter,
    text: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    color: Color,
    iconTint: Color = color
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.background
    }

    val textColor =
        color


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
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(iconTint)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            color = textColor,
        )
    }
}