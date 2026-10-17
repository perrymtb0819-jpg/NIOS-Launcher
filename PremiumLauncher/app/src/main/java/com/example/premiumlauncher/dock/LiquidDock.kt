package com.example.premiumlauncher.dock

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalVibrator
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import com.example.premiumlauncher.R
import com.example.premiumlauncher.ui.theme.PremiumLauncherTheme
import com.example.premiumlauncher.iconengine.IconProcessor
import com.example.premiumlauncher.iconengine.GlassAppTile

/**
 * Dynamic Liquid Dock that behaves like a capsule of floating liquid mercury.
 * It flexes, expands, and bends when dragged, with liquid mercury physics.
 */
@Composable
fun LiquidDock(
    context: Context = LocalContext.current,
    apps: List<String> = emptyList(), // List of package names to display
    onAppLaunch: (String) -> Unit = {},
    onDockDrag: (Offset) -> Unit = {}
) {
    val vibrator = LocalVibrator.current
    val dockHeight = 80.dp
    val itemSize = 60.dp
    val spacing = 12.dp
    val margin = 16.dp

    // State for the liquid dock properties
    var dockWidth by remember { mutableStateOf(0.dp) }
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var liquidProgress by remember { mutableStateOf(0f) } // 0 to 1 for liquid expansion
    var bendAmount by remember { mutableStateOf(0f) } // -1 to 1 for bending

    // Calculate dock width based on number of items
    val itemCount = apps.size
    val calculatedWidth = (itemSize * itemCount) + (spacing * (itemCount - 1)) + (margin * 2)

    // Liquid mercury effect: the dock can expand beyond its normal size when dragged
    val effectiveWidth by remember { mutableStateOf(calculatedWidth) }
    var dockWidthState by remember { mutableStateOf(calculatedWidth) }

    // Update dock width based on liquid progress
    LaunchedEffect(liquidProgress) {
        dockWidthState = (calculatedWidth * (1 + liquidProgress * 0.5)).dp // Can expand up to 50%
    }

    // Handle drag gestures for the liquid effect
    val pointerInput = rememberUpdatedState {
        PointerInputScope {
            detectDragGestures(
                onDragStart = { _, dragOffset ->
                    // Start dragging the dock
                    isDragging = true
                    this.dragOffset = dragOffset
                    // Haptic feedback - liquid mercury feel
                    vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                    // Start liquid expansion
                    liquidProgress = 0.3f
                },
                onDrag = { _, dragOffset ->
                    this.dragOffset = dragOffset
                    // Calculate liquid expansion based on drag distance
                    val dragMagnitude = Math.sqrt(
                        dragOffset.x * dragOffset.x + dragOffset.y * dragOffset.y
                    )
                    // Convert pixel drag to liquid progress (0 to 1)
                    liquidProgress = (dragMagnitude / 200).coerceIn(0f, 1f)
                    // Calculate bend amount based on drag direction
                    bendAmount = (dragOffset.x / 300).coerceIn(-1f, 1f) // Negative for left, positive for right
                    // Notify parent of drag
                    onDockDrag(dragOffset)
                },
                onDragEnd = { _, _ ->
                    // End drag: reset to normal state with liquid recoil
                    isDragging = false
                    // Liquid recoil animation
                    liquidProgress = 0f
                    bendAmount = 0f
                    // Haptic feedback for liquid settling
                    vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            )
        }
    }

    // Main dock container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dockHeight)
            .background(
                // Liquid mercury background with reflective properties
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x66000000),
                        Color(0x99001122),
                        Color(0x66000000)
                    )
                )
            )
            .border(
                width = 2.dp,
                color = Color(0x3300FFAA),
                shape = RoundedCornerShape(24.dp)
            )
            // Liquid mercury shape: slightly flexible corners
            .then(modifier = Modifier
                .background(
                    color = Color(0x66000000),
                    shape = RoundedCornerShape(24.dp)
                )
                .alpha(0.7f)
            )
            // Liquid expansion and bending
            .offset(
                x = (bendAmount * 20).toInt(), // Bend based on drag
                y = 0
            )
            .width(effectiveWidth)
            .height(dockHeight)
            .pointerInput(pointerInput)
    ) {
        // Liquid mercury particles effect (visual only)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.2f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw floating particles that simulate liquid mercury
                val particleCount = 8
                val width = size.width
                val height = size.height
                val time = System.currentTimeMillis() * 0.001

                repeat(particleCount) { i ->
                    val angle = (i * 2 * Math.PI / particleCount) + time
                    val radius = min(width, height) * 0.3
                    val x = width / 2 + cos(angle) * radius * 0.5
                    val y = height / 2 + sin(angle) * radius * 0.5
                    val size = 4.0 + sin(time + i) * 2.0

                    drawCircle(
                        center = Offset(x.toFloat(), y.toFloat()),
                        radius = size,
                        color = Color(0x6600FFAA),
                        blur = 2.dp
                    )
                }
            }
        }

        // App items in the dock
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(margin)
                .horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            apps.forEachIndexed { index, packageName ->
                // Apply liquid motion to individual items
                val itemOffset by remember {
                    mutableStateOf(Offset.Zero)
                }
                // Apply a slight wave motion based on liquid progress
                LaunchedEffect(liquidProgress, index) {
                    // Create a waving motion for each item
                    val waveOffset = sin(
                        System.currentTimeMillis() * 0.003 + index * 0.5
                    ) * liquidProgress * 10
                    itemOffset = Offset(waveOffset, 0.0)
                }

                Box(
                    modifier = Modifier
                        .offset(
                            x = itemOffset.x.toInt(),
                            y = itemOffset.y.toInt()
                        )
                        .size(itemSize)
                ) {
                    GlassAppTile(
                        packageName = packageName,
                        label = "App", // In real implementation, get actual label
                        badgeCount = null,
                        modifier = Modifier.size(itemSize),
                        onClick = { onAppLaunch(packageName) }
                    )
                }
            }
        }

        // Liquid mercury meniscus effect at the edges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(margin)
        ) {
            // Left meniscus
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .alpha(liquidProgress)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        topLeft = Offset(0f, 0f),
                        size = Size(16f, 16f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        color = Color(0xFF00FFAA),
                        strokeWidth = 2f
                    )
                }
            }
            Spacer(modifier = Modifier.width())
            // Right meniscus
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .alpha(liquidProgress)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        topLeft = Offset(0f, 0f),
                        size = Size(16f, 16f),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        color = Color(0xFF00FFAA),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}