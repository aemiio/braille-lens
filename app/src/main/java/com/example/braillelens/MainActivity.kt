package com.example.braillelens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.braillelens.ui.BrailleLensTheme
import com.example.braillelens.ui.components.AppDrawer
import com.example.braillelens.ui.components.BottomNavigationBar
import com.example.braillelens.ui.screens.AboutScreen
import com.example.braillelens.ui.screens.DictionaryScreen
import com.example.braillelens.ui.screens.HomeScreen
import com.example.braillelens.ui.screens.OnboardingScreen
import com.example.braillelens.ui.screens.hasCompletedOnboarding
import com.example.braillelens.ui.screens.setOnboardingComplete
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrailleLensTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedScreen by remember { mutableStateOf("home") }
    val navController = rememberNavController()
    val context = LocalContext.current

    // DEBUG FLAGS
    val forceShowOnboarding = true // Set to true to show onboarding during development
    val allowOnboardingNavigation = true // Set to true to allow navigation from onboarding to home

    // Onboarding state
    var onboardingCompleted by remember {
        mutableStateOf(if (forceShowOnboarding) false else hasCompletedOnboarding(context))
    }

    // Show only the onboarding screen if not completed
    if (!onboardingCompleted) {
        OnboardingScreen(
            navController = navController,
            onFinishOnboarding = {
                setOnboardingComplete(context)
                // Use allowOnboardingNavigation to determine if we should navigate away
                if (allowOnboardingNavigation) {
                    onboardingCompleted = true
                }
            }
        )
    } else {
        // Normal app flow
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    AppDrawer { screen ->
                        selectedScreen = screen
                        scope.launch {
                            drawerState.close()
                        }
                    }
                }
            }
        ) {
            Scaffold(
                bottomBar = {
                    BottomNavigationBar { screen ->
                        selectedScreen = screen
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (selectedScreen) {
                        "home" -> HomeScreen { scope.launch { drawerState.open() } }
                        "dictionary" -> DictionaryScreen()
                        "about" -> AboutScreen()
                    }
                }
            }
        }
    }
}