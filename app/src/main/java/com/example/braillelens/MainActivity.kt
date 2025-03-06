package com.example.braillelens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import com.example.braillelens.ui.screens.HomeScreen
import com.example.braillelens.ui.screens.DictionaryScreen
import com.example.braillelens.ui.screens.AboutScreen
import com.example.braillelens.ui.components.AppDrawer
import com.example.braillelens.ui.components.BottomNavigationBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}


@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var selectedScreen by remember { mutableStateOf("home") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                AppDrawer { selectedScreen = it }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar { selectedScreen = it }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedScreen) {
                    "home" -> HomeScreen { coroutineScope.launch { drawerState.open() } }
                    "dictionary" -> DictionaryScreen()
                    "about" -> AboutScreen()
                }
            }
        }
    }
}
