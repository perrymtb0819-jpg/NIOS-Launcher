package com.example.premiumlauncher.lockscreen

import android.app.Vibrator
import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.animation.Animatable
import androidx.animation.spring
import androidx.compose.foundation.WindowInsets
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.Shadow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalVibrator
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fx
import com.example.premiumlauncher.R
import com.example.premiumlauncher.ui.theme.PremiumLauncherTheme

class LockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make the lock screen full screen and secure
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT)
        setContent {
            PremiumLauncherTheme {
                LockScreen()
            }
        }
    }
}

@Composable
fun LockScreen() {
    // State for the lock screen
    var unlocked by remember { mutableStateOf(false) }
    var pinState by remember { mutableStateOf("") }
    val maxPinLength = 4

    // Animation state for unlock (spring physics)
    val unlockProgress = Animatable(0f)
    val vibrator = LocalVibrator.current

    // Liquid glass matrix clock state - we'll simulate a more advanced matrix
    val matrixRows = 7
    val matrixCols = 7
    val dotSize = 6.dp
    val dotSpacing = 8.dp
    val matrixWidth = (matrixCols * dotSize.value + (matrixCols - 1) * dotSpacing.value).dp
    val matrixHeight = (matrixRows * dotSize.value + (matrixRows - 1) * dotSpacing.value).dp

    // Initial dot positions (grid)
    val initialDotPositions = remember {
        mutableStateListOf<Offset>().apply {
            for (row in 0 until matrixRows) {
                for (col in 0 until matrixCols) {
                    val x = col * (dotSize.value + dotSpacing.value)
                    val y = row * (dotSize.value + dotSpacing.value)
                    add(Offset(x, y))
                }
            }
        }
    }
    // Current dot positions (will be perturbed by touch for liquid effect)
    var dotPositions by remember { mutableStateListOf<Offset>(initialDotPositions) }
    // Time display state (for demo, we'll use a fixed time)
    val currentTime = "12:34"

    // Handle touch events on the matrix to create liquid/melting effect
    val pointerInput = rememberUpdatedState {
        PointerInputScope {
            detectDragGestures(
                onDragStart = { centroid, _ ->
                    // Haptic feedback on touch start - water ripple effect
                    vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
                    // Perturb dots near the touch point to create liquid effect
                    perturbDotsLiquid(centroid, dotPositions, ::setDotPositions, strength = 0.5f)
                },
                onDrag = { _, dragAmount ->
                    // Continue liquid effect while dragging
                    val centroid = Offset(dragAmount.x, dragAmount.y) // Simplified
                    perturbDotsLiquid(centroid, dotPositions, ::setDotPositions, strength = 0.3f)
                },
                onDragEnd = { _, _ ->
                    // Reset dots to original positions with spring animation when released
                    // In a full implementation, we'd animate back to original positions
                    dotPositions = initialDotPositions
                    // Check if swipe gesture qualifies for unlock (swipe up)
                    // For simplicity, we'll just check if we dragged up significantly
                    // A real implementation would track the drag trajectory
                }
            )
        }
    }

    // Detect swipe up gesture for unlock
    val swipeUpDetected = remember { mutableStateOf(false) }
    val swipePointerInput = rememberUpdatedState {
        PointerInputScope {
            detectVerticalDragGestures(
                onDragStart = { _ -> },
                onDragEnd = { _, velocityY ->
                    // If swipe up with sufficient velocity, trigger unlock
                    if (velocityY < -1000) { // Negative velocity means upward swipe
                        swipeUpDetected.value = true
                        // Start unlock animation
                        unlockProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = 0.5f // < 0.6 as required for fluid physics
                            )
                        ) { value ->
                            if value >= 1f {
                                unlocked = true
                            }
                        }
                    }
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Deep black background
            .pointerInput(pointerInput)
            .pointerInput(swipePointerInput)
    ) {
        // Background liquid glass effect (animated gradient)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF000000),
                            Color(0xFF1A001A),
                            Color(0xFF000000)
                        )
                    )
                )
                .alpha(0.9f)
        ) {}

        // Liquid glass matrix clock (only show if not unlocked)
        if (!unlocked) {
            LiquidMatrixClock(
                dotPositions = dotPositions,
                dotSize = dotSize,
                timeText = currentTime,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
                    .size(matrixWidth, matrixHeight)
            )
        }

        // PIN input area (at the bottom)
        VStack(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .fillMaxWidth()
                // Advanced frosted glass background with liquid properties
                .background(
                    color = Color(0x66000000), // Very dark semi-transparent
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color(0x66FFFFFF),
                    shape = RoundedCornerShape(24.dp)
                )
                // Add a subtle inner glow effect
                .background(
                    color = Color(0x11FFFFFF),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            // Only show PIN pad if locked
            if (!unlocked) {
                Text(
                    text = "Enter PIN to Unlock",
                    color = Color(0xFFF0F0F0),
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(bottom = 24.dp)
                )
                // PIN dots with liquid effect
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(maxPinLength) { index ->
                        if (index < pinState.length) {
                            LiquidPinDot(filled = true)
                        } else {
                            LiquidPinDot(filled = false)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                // Number pad with liquid touch effects
                NumberPinPadLiquid { number ->
                    if (pinState.length < maxPinLength) {
                        pinState = pinState + number
                        // Haptic feedback for each number press - water droplet effect
                        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                        if (pinState.length == maxPinLength) {
                            // Simple check for demo: PIN is "1234"
                            if (pinState == "1234") {
                                // Start unlock animation
                                unlockProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = 0.4f // Even more fluid
                                    )
                                ) { value ->
                                    if value >= 1f {
                                        unlocked = true
                                    }
                                }
                            } else {
                                // Wrong PIN: haptic feedback and shake
                                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                // In a full implementation, we'd shake the PIN dots
                                pinState = "" // Clear on error
                            }
                        }
                    }
                }
            } else {
                // Unlocked state: show success message and transition to home screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Success text
                    Text(
                        text = "Access Granted",
                        color = Color(0xFF00FFAA),
                        fontSize = 24.sp,
                        modifier = Modifier.align(Alignment.Center)
                            .padding(bottom = 40.dp)
                    )
                    // We'll launch the main activity after a short delay
                    LaunchedEffect(Unit) {
                        // Delay to allow unlock animation to be seen
                        delay(800)
                        // In a real app, we would start the main activity and finish this one
                        // For now, we just rely on the unlocked state
                    }
                }
            }
        }

        // Unlock animation: when unlocked is true, we animate the lock screen away with fluid physics
        if (unlocked) {
            // We'll use the unlockProgress to animate the entire lock screen off-screen with liquid motion
            val unlockOffset by unlockProgress.animateAsState(
                targetValue = 1500f, // Move far off screen
                animationSpec = spring(dampingRatio = 0.4f, stiffnessVal = 50f) // Fluid spring
            )
            val unlockAlpha by unlockProgress.animateAsState(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.4f)
            )
            Box(
                modifier = Modifier
                    .offset(y = unlockOffset)
                    .alpha(unlockAlpha)
                    .fillMaxSize()
            ) {
                // This box will move off screen and fade out when unlocked
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000))
                ) {}
            }
        }
    }
}

@Composable
fun LiquidMatrixClock(
    dotPositions: List<Offset>,
    dotSize: Dp,
    timeText: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val size = size
        val center = Offset(size.width / 2, size.height / 2)
        // Draw each dot with liquid properties
        dotPositions.forEachIndexed { index, dotPos ->
            // Convert from logical position to canvas position (centered)
            val x = dotPos.x + center.x - size.width / 2
            val y = dotPos.y + center.y - size.height / 2

            // Liquid effect: slightly animate dot size based on position
            val liquidFactor = 0.8 + 0.2 * sin((index * 0.1) + System.currentTimeMillis() * 0.001)
            val liquidRadius = (dotSize.value / 2) * liquidFactor

            drawCircle(
                color = Color(0xFF00FFAA), // Neon cyan for liquid glass
                center = Offset(x, y),
                radius = liquidRadius
            )

            // Add inner glow for liquid effect
            drawCircle(
                color = Color(0x3300FFAA),
                center = Offset(x, y),
                radius = liquidRadius * 1.3
            )
        }
    }

    // Draw time text in the center
    drawText(
        text = timeText,
        topLeft = Offset(
            (size.width - textLength(text, fontSize = 48.sp)) / 2,
            (size.height - 48.sp.toPx()) / 2
        ),
        color = Color.White,
        fontSize = 48.sp
    )
}

// Helper to estimate text width (simplified)
private fun textLength(text: String, fontSize: Sp): Float {
    // This is a simplification - in reality we'd use TextLayout or similar
    return text.length * fontSize.value * 0.5f
}

@Composable
fun LiquidPinDot(filled: Boolean = false) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(
                color = if (filled) Color(0xFF00FFAA) else Color(0x33FFFFFF),
                shape = CircleShape
            )
            // Liquid shimmer effect
            .background(
                color = if (filled) Color(0x11FFFFFF) else Color(0x00FFFFFF),
                shape = CircleShape
            )
    )
}

@Composable
fun NumberPinPadLiquid(onNumberSelected: (Int) -> Unit) {
    val numbers = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9),
        listOf(-1, 0, -2) // -1 for delete, -2 for empty
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        numbers.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { number ->
                    when (number) {
                        -1 -> { // Delete button with liquid effect
                            LiquidIconButton(
                                icon = Icons.Default.Backspace,
                                label = "Delete",
                                onClick = { /* TODO: delete last number */ }
                            )
                        }
                        -2 -> { // Spacer for layout
                            Spacer(modifier = Modifier.width(0.dp))
                        }
                        else -> {
                            LiquidNumberButton(number = number) {
                                onNumberSelected(number)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .background(
                color = Color(0x66000000),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = Color(0x66FFFFFF),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF00FFAA)
        )
    }
}

@Composable
fun LiquidNumberButton(number: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .background(
                color = Color(0x66000000),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = Color(0x66FFFFFF),
                shape = CircleShape
            )
            // Liquid touch feedback
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        // Haptic feedback on press
                        val vibrator = LocalVibrator.current
                        vibrator.vibrate(VibrationEffect.createOneShot(8, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                )
            }
    ) {
        Text(
            text = number.toString(),
            color = Color(0xFF00FFAA),
            fontSize = 24.sp
        )
    }
}

/**
 * Perturb the dots near the touch point to create a liquid/melting effect.
 * This creates a more sophisticated liquid displacement.
 */
private fun perturbDotsLiquid(
    centroid: Offset,
    currentPositions: StateList<Offset>,
    setter: (StateList<Offset>) -> Unit,
    strength: Float = 0.3f
) {
    val touchPoint = Offset(centroid.x, centroid.y)
    val influenceRadius = 120.0 // pixels
    val baseStrength = strength

    val newPositions = mutableStateListOf<Offset>()
    currentPositions.forEach { pos ->
        val dx = pos.x - touchPoint.x
        val dy = pos.y - touchPoint.y
        val distance = Math.sqrt(dx * dx + dy * dy)

        if (distance < influenceRadius) {
            // Liquid displacement: move dots away from touch point with fluid dynamics
            val angle = Math.atan2(dy, dx)
            // Distance-based falloff with liquid characteristics
            val normalizedDistance = distance / influenceRadius
            val liquidFalloff = 1.0 - (normalizedDistance * normalizedDistance) // Quadratic falloff for liquid feel
            val force = baseStrength * liquidFalloff * liquidFalloff // Extra liquidity

            // Add some liquid turbulence
            val turbulence = (sin(pos.x * 0.1 + System.currentTimeMillis() * 0.002) * 0.1)
            val finalForce = force + turbulence

            val newX = pos.x + (finalForce * Math.cos(angle)).toFloat()
            val newY = pos.y + (finalForce * Math.sin(angle)).toFloat()

            // Liquid cohesion: dots want to stay together
            val cohesionForce = 0.05
            val avgPos = currentPositions.reduce { acc, p -> Offset(acc.x + p.x, acc.y + p.y) }
            val centerOffset = Offset(avgPos.x / currentPositions.size, avgPos.y / currentPositions.size)
            val toCenter = Offset(centerOffset.x - pos.x, centerOffset.y - pos.y)
            val cohesionX = pos.x + (toCenter.x * cohesionForce)
            val cohesionY = pos.y + (toCenter.y * cohesionForce)

            // Blend between liquid displacement and cohesion
            val blendedX = newX * 0.7 + cohesionX * 0.3
            val blendedY = newY * 0.7 + cohesionY * 0.3

            newPositions.add(Offset(blendedX, blendedY))
        } else {
            // Outside influence radius: apply gentle cohesion to maintain liquid shape
            val avgPos = currentPositions.reduce { acc, p -> Offset(acc.x + p.x, acc.y + p.y) }
            val centerOffset = Offset(avgPos.x / currentPositions.size, avgPos.y / currentPositions.size)
            val toCenter = Offset(centerOffset.x - pos.x, centerOffset.y - pos.y)
            val cohesionForce = 0.02
            val cohesionX = pos.x + (toCenter.x * cohesionForce)
            val cohesionY = pos.y + (toCenter.y * cohesionForce)
            newPositions.add(Offset(cohesionX, cohesionY))
        }
    }
    setter(newPositions)
}