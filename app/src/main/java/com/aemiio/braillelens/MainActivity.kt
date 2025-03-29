package com.aemiio.braillelens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aemiio.braillelens.ui.BrailleLensColors
import com.aemiio.braillelens.ui.BrailleLensTheme
import com.aemiio.braillelens.ui.components.AppDrawer
import com.aemiio.braillelens.ui.components.CustomNavigationBar
import com.aemiio.braillelens.ui.screens.AboutScreen
import com.aemiio.braillelens.ui.screens.AnnotationScreen
import com.aemiio.braillelens.ui.screens.CaptureScreen
import com.aemiio.braillelens.ui.screens.DictionaryScreen
import com.aemiio.braillelens.ui.screens.Grade1Screen
import com.aemiio.braillelens.ui.screens.Grade2Screen
import com.aemiio.braillelens.ui.screens.HomeScreen
import com.aemiio.braillelens.ui.screens.ImportScreen
import com.aemiio.braillelens.ui.screens.OnboardingScreen
import com.aemiio.braillelens.ui.screens.RecognitionResultScreen
import com.aemiio.braillelens.ui.screens.SampleScreen
import com.aemiio.braillelens.ui.screens.findActivity
import com.aemiio.braillelens.ui.screens.hasCompletedOnboarding
import com.aemiio.braillelens.ui.screens.setOnboardingComplete
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrailleLensTheme {
                Surface() {
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


    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = view.context.findActivity()?.window
        WindowCompat.setDecorFitsSystemWindows(
            window ?: return@DisposableEffect onDispose {},
            false
        )
        onDispose {}
    }

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
                ModalDrawerSheet(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    drawerShape = RoundedCornerShape(
                        topEnd = 70.dp,
                        bottomEnd = 50.dp
                    ),
                    drawerContainerColor = BrailleLensColors.backgroundGrey
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BrailleLensColors.backgroundGrey)
                    ) {
                        AppDrawer { screen ->
                            selectedScreen = screen
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    }
                }
            }
        ) {
            Scaffold(
                bottomBar = {
                    CustomNavigationBar(
                        currentScreen = selectedScreen,
                        onItemSelected = { screen ->
                            selectedScreen = screen
                        }
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    NavHost(navController = navController, startDestination = selectedScreen) {
                        composable("home") { HomeScreen(
                            openDrawer = { scope.launch { drawerState.open() } },
                            navController = navController
                        )  }
                        composable("dictionary") {
                            DictionaryScreen(
                                openDrawer = { scope.launch { drawerState.open() } },
                                navController = navController
                            )
                        }
                        composable("about") { AboutScreen() }
                        composable("grade1") { Grade1Screen() }
                        composable("grade2") { Grade2Screen() }


                        composable("capture/{mode}") { backStackEntry ->
                            CaptureScreen(
                                navController = navController,
                                detectionMode = backStackEntry.arguments?.getString("mode") ?: "Grade 1 Braille"
                            )
                        }
                        composable("import/{mode}") { backStackEntry ->
                            ImportScreen(
                                navController = navController,
                                detectionMode = backStackEntry.arguments?.getString("mode") ?: "Grade 1 Braille"
                            )
                        }
                        composable("sample/{mode}/{sampleId}") { backStackEntry ->
                            SampleScreen(
                                navController = navController,
                                detectionMode = backStackEntry.arguments?.getString("mode") ?: "Grade 1 Braille",
                                sampleId = backStackEntry.arguments?.getString("sampleId")?.toIntOrNull() ?: R.drawable.sample1
                            )
                        }
                        composable("result/{mode}/{imagePath}") { backStackEntry ->
                            RecognitionResultScreen(
                                navController = navController,
                                detectionMode = backStackEntry.arguments?.getString("mode") ?: "Grade 1 Braille",
                                imagePath = backStackEntry.arguments?.getString("imagePath") ?: "",
                                recognizedText = "",
                            )
                        }

                        composable("annotation/{imagePath}") { backStackEntry ->
                            AnnotationScreen(
                                navController = navController,
                                imagePath = backStackEntry.arguments?.getString("imagePath") ?: "",
                                onBoxUpdate = { /* Handle box updates if needed */ }
                            )
                        }

                    }
                }
            }
        }
    }
}