package com.example.braillelens.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.braillelens.R

@Composable
fun BottomNavigationBar(onItemSelected: (String) -> Unit) {
    var selectedItem by remember { mutableStateOf("home") }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.height(64.dp)
    ) {
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.braille_logo), contentDescription = "Home") },
            label = { Text("Home") },
            selected = selectedItem == "home",
            onClick = {
                selectedItem = "home"
                onItemSelected("home")
            }
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.braille_logo), contentDescription = "Dictionary") },
            label = { Text("Dictionary") },
            selected = selectedItem == "dictionary",
            onClick = {
                selectedItem = "dictionary"
                onItemSelected("dictionary")
            }
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.braille_logo), contentDescription = "About") },
            label = { Text("About") },
            selected = selectedItem == "about",
            onClick = {
                selectedItem = "about"
                onItemSelected("about")
            }
        )
    }
}
