package com.aemiio.braillelens.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.aemiio.braillelens.R
import com.aemiio.braillelens.utils.EnableFullScreen
import com.aemiio.braillelens.utils.WindowType
import com.aemiio.braillelens.utils.rememberWindowSize
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

// Helper functions for onboarding state
fun hasCompletedOnboarding(context: Context): Boolean {
    val sharedPref = context.getSharedPreferences("braillelens_prefs", Context.MODE_PRIVATE)
    return sharedPref.getBoolean("onboarding_complete", false)
}

fun setOnboardingComplete(context: Context) {
    val sharedPref = context.getSharedPreferences("braillelens_prefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putBoolean("onboarding_complete", true)
        apply()
    }
}

// Helper function for animation interpolation
fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

@Composable
fun OnboardingScreen(
    navController: NavController,
    onFinishOnboarding: () -> Unit
) {
    EnableFullScreen()
    val windowSize = rememberWindowSize()
    when (windowSize.height) {
        WindowType.Compact -> {
            SmallOnboardingScreen(
                onFinishOnboarding = onFinishOnboarding
            )
        }

        else -> {
            MediumOnboardingScreen(
                onFinishOnboarding = onFinishOnboarding
            )
        }
    }
}

// Common shared data class for OnboardingScreen
data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val imageRes: Int,
    val contentDescription: String,
    val backgroundColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SmallOnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    OnboardingScreenImplementation(
        onFinishOnboarding = onFinishOnboarding,
        spacerHeight = 16.dp,
        buttonWidth = 0.8f,
        buttonVerticalPadding = 24.dp,
        isSmallScreen = true
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediumOnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    OnboardingScreenImplementation(
        onFinishOnboarding = onFinishOnboarding,
        spacerHeight = 24.dp,
        buttonWidth = 0.7f,
        buttonVerticalPadding = 32.dp,
        isSmallScreen = false
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OnboardingScreenImplementation(
    onFinishOnboarding: () -> Unit,
    spacerHeight: Dp,
    buttonWidth: Float,
    buttonVerticalPadding: Dp,
    isSmallScreen: Boolean
) {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = view.context.findActivity()?.window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.setDecorFitsSystemWindows(false)
        } else {
            window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        WindowCompat.setDecorFitsSystemWindows(
            window ?: return@DisposableEffect onDispose {},
            false
        )

        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window?.setDecorFitsSystemWindows(true)
            } else {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            }
            WindowCompat.setDecorFitsSystemWindows(window ?: return@onDispose, true)
        }
    }

    val pages = listOf(
        OnboardingPage(
            title = "Empower Inclusion, Recognize Filipino Braille",
            subtitle = "Together, we break barriers with effortless Grade 1 and Grade 2 Braille recognition, fostering a more inclusive society.",
            imageRes = R.drawable.onboarding_1,
            contentDescription = "a visually impaired girl walking to school",
            backgroundColor = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPage(
            title = "Easy Braille Recognition",
            subtitle = "Capture or import Filipino Braille and experience detection powered by advanced technology.",
            imageRes = R.drawable.onboarding_2,
            contentDescription = "Father and daughter going to school",
            backgroundColor = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPage(
            title = "See Beyond Touch with Braille-Lens",
            subtitle = "Transform Filipino Braille into text and speech with ease, bridging communication and accessibility effortlessly.",
            imageRes = R.drawable.onboarding_3,
            contentDescription = "Diversity of people",
            backgroundColor = MaterialTheme.colorScheme.secondary
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Animated dot background
            AnimatedDotBackground(
                pages[pagerState.currentPage].backgroundColor,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(spacerHeight))

                // Pager for onboarding pages with page transition animations
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Onboarding pages, swipe left or right to navigate"
                        }
                ) { position ->
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                val pageOffset = (
                                        (pagerState.currentPage - position) + pagerState
                                            .currentPageOffsetFraction
                                        ).absoluteValue

                                alpha = lerp(
                                    start = 0.5f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )

                                scaleX = lerp(
                                    start = 0.85f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                                scaleY = lerp(
                                    start = 0.85f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                            }
                            .fillMaxSize()
                    ) {
                        if (position == 0) {
                            FirstPageLayout(
                                page = pages[position],
                                isSmallScreen = isSmallScreen
                            )
                        } else {
                            OtherPagesLayout(
                                page = pages[position],
                                isSmallScreen = isSmallScreen
                            )
                        }
                    }
                }

                PageIndicator(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.padding(bottom = 16.dp)
                )


                // Get Started Button - Only show on last page
                AnimatedVisibility(
                    visible = pagerState.currentPage == pages.size - 1,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = buttonVerticalPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                setOnboardingComplete(context)
                                onFinishOnboarding()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = pages[pagerState.currentPage].backgroundColor
                            ),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                            modifier = Modifier
                                .fillMaxWidth(buttonWidth)
                                .semantics {
                                    contentDescription = "Get started and go to home screen"
                                }
                        ) {
                            Text(
                                text = "Get Started",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun FirstPageLayout(
    page: OnboardingPage,
    isSmallScreen: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isSmallScreen) 8.dp else 16.dp)
        )

        Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 16.dp))

        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isSmallScreen) 16.dp else 24.dp)
        )

        Spacer(modifier = Modifier.height(if (isSmallScreen) 20.dp else 32.dp))

        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = page.contentDescription,
            modifier = Modifier
                .fillMaxWidth(if (isSmallScreen) 0.70f else 1f)
                .padding(horizontal = if (isSmallScreen) 8.dp else 16.dp),
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
fun OtherPagesLayout(
    page: OnboardingPage,
    isSmallScreen: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = page.contentDescription,
            modifier = Modifier
                .fillMaxWidth(if (isSmallScreen) 0.70f else 1f)
                .padding(horizontal = if (isSmallScreen) 4.dp else 16.dp),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.height(if (isSmallScreen) 20.dp else 32.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color =  MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isSmallScreen) 8.dp else 16.dp)
        )

        Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 16.dp))

        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isSmallScreen) 16.dp else 24.dp)
        )
    }
}

@Composable
fun AnimatedDotBackground(pageColor: Color, modifier: Modifier = Modifier) {
    val density = LocalDensity.current

    class Dot(
        val initialX: Float,
        val initialY: Float,
        val radius: Float,
        val animationOffset: Float
    )

    val dots = remember {
        List(50) {
            Dot(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat(),
                radius = Random.nextFloat() * 5f + 2f,
                animationOffset = Random.nextFloat() * 2f * 3.14f
            )
        }
    }

    val animationProgress = rememberInfiniteTransition(label = "DotAnimationTransition")
    val progress = animationProgress.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DotMovement"
    )

    // Animate color change for dots
    val dotColor by animateColorAsState(
        targetValue = pageColor.copy(alpha = 0.2f),
        animationSpec = tween(500),
        label = "DotColor"
    )

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        dots.forEach { dot ->
            // Calculate moving position with slight oscillation
            val movementRange = 50f
            val xOffset = sin(progress.value * 2 * 3.14f + dot.animationOffset) * movementRange
            val yOffset = cos(progress.value * 2 * 3.14f + dot.animationOffset) * movementRange

            val x = dot.initialX * canvasWidth + xOffset
            val y = dot.initialY * canvasHeight + yOffset

            drawCircle(
                color = dotColor,
                radius = dot.radius.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .semantics {
                contentDescription = "Page ${currentPage + 1} of $pageCount"
            }
    ) {
        repeat(pageCount) { iteration ->
            val color = if (currentPage == iteration) {
                Color(0xFFC14600)
            } else {
                Color(0xFFE5D0AC)
            }

            val width = if (currentPage == iteration) 24.dp else 10.dp
            val height = 10.dp

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .height(height)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}