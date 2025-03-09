package com.example.braillelens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.braillelens.R
import com.example.braillelens.ui.BrailleLensColors


@Composable
fun CustomNavigationBar(
    currentScreen: String = "home",
    onItemSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BrailleLensColors.darkOlive)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Home tab
            NavItem(
                title = "Home",
                icon = R.drawable.home_24px,
                selected = currentScreen == "home",
                onClick = { onItemSelected("home") }
            )

            // Dictionary tab
            NavItem(
                title = "Dictionary",
                icon = R.drawable.dictionary_24px,
                selected = currentScreen == "dictionary",
                onClick = { onItemSelected("dictionary") }
            )

            // About tab
            NavItem(
                title = "About",
                icon = R.drawable.info_24px,
                selected = currentScreen == "about",
                onClick = { onItemSelected("about") }
            )
        }
    }
}

@Composable
private fun NavItem(
    title: String,
    icon: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        modifier = modifier
            .fillMaxHeight()
            .width(92.dp) // Fixed width for equal distribution
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            tint = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .height(3.dp)
                .width(if (selected) 32.dp else 0.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(if (selected) Color.White else Color.Transparent)
        )
    }
}