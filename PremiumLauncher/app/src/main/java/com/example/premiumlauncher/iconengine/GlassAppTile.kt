package com.example.premiumlauncher.iconengine

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawScope.drawWithContent
import androidx.compose.ui.graphics.drawScope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fx
import androidx.compose.ui.unit.sp
import com.example.premiumlauncher.R

/**
 * A composable that displays an app icon inside a liquid glass tile.
 *
 * @param packageName The package name of the app.
 * @param sizeInDp The size of the tile (width and height for square tiles).
 * @param label The app label to display below the icon.
 * @param badgeCount Optional unread notification count to display as a badge.
 * @param onClick Callback when the tile is clicked.
 * @param onLongClick Callback when the tile is long-pressed.
 */
@Composable
fun GlassAppTile(
    packageName: String,
    sizeInDp: Dp = 80.dp,
    label: String,
    badgeCount: Int? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val iconProcessor = remember { IconProcessor(context) }
    val processedIcon = remember(packageName) { iconProcessor.getProcessedIcon(packageName) }

    // If we don't have an icon yet, show a placeholder
    val iconToShow = processedIcon ?: androidx.compose.ui.res.painterResource(id = R.drawable.ic_placeholder)

    // Tile background: frosted glass with reflective properties
    val tileModifier = Modifier
        .size(sizeInDp)
        .background(
            color = Color(0x66FFFFFF), // Semi-transparent white for frosted glass
            shape = RoundedCornerShape(16.dp)
        )
        .border(
            width = 1.dp,
            color = Color(0xFFFFFFFF),
            shape = RoundedCornerShape(16.dp)
        )
        // Add a slight drop shadow to simulate depth
        .shadow(2.dp, Color(0x33000000), RoundedCornerShape(16.dp))

    // Content of the tile: icon and label
    Box(
        modifier = tileModifier
            .clickable { onClick() }
            .longClickable { onLongClick() }
    ) {
        // Icon with a slight reflection effect (we'll just center it)
        Image(
            bitmap = iconToShow.asImageBitmap(),
            contentDescription = label,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )

        // App label
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )

        // Notification badge (if any)
        badgeCount?.let { count ->
            if (count > 0) {
                val badgeText = if (count > 99) "99+" else count.toString()
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            color = Color.Red,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * A composable that displays a 2x2 macro tile (Nothing OS style).
 *
 * @param packageName The package name of the app.
 * @param label The app label.
 * @param badgeCount Optional unread notification count.
 * @param onClick Callback when the tile is clicked.
 * @param onLongClick Callback when the tile is long-pressed.
 */
@Composable
fun MacroGlassAppTile(
    packageName: String,
    label: String,
    badgeCount: Int? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    GlassAppTile(
        packageName = packageName,
        sizeInDp = 160.dp, // 2x2 tile (assuming 80dp is 1x1)
        label = label,
        badgeCount = badgeCount,
        onClick = onClick,
        onLongClick = onLongClick
    )
}

/**
 * A composable that displays a 4x1 banner tile.
 *
 * @param packageName The package name of the app.
 * @param label The app label.
 * @param badgeCount Optional unread notification count.
 * @param onClick Callback when the tile is clicked.
 * @param onLongClick Callback when the tile is long-pressed.
 */
@Composer
fun BannerGlassAppTile(
    packageName: String,
    label: String,
    badgeCount: Int? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    GlassAppTile(
        packageName = packageName,
        sizeInDp = 320.dp, // 4x1 tile (width 4*80, height 80)
        label = label,
        badgeCount = badgeCount,
        onClick = onClick,
        onLongClick = onLongClick
    )
}