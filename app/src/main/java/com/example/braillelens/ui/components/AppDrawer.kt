package com.example.braillelens.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.braillelens.R

@Composable
fun AppDrawer(onItemSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // App Logo, Name, and Subtitle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.braille_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Braille Lens",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Your Vision, Our Mission",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Drawer Navigation Items
        DrawerItem(text = "Home", onClick = { onItemSelected("home") })
        DrawerItem(text = "Dictionary", onClick = { onItemSelected("dictionary") })
        DrawerItem(text = "About", onClick = { onItemSelected("about") })
    }
}

@Composable
fun DrawerItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onClick() }
    )
}
