package com.example.premiumlauncher.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.sliderslider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.premiumlauncher.R
import com.example.premiumlauncher.ui.theme.PremiumLauncherTheme
import com.example.premiumlauncher.ui.theme.SettingsTheme
import com.example.premiumlauncher.widgets.PremiumWidgets
import com.example.premiumlauncher.home.HomeScreen
import com.example.premiumlauncher.lockscreen.LockScreenActivity
import com.example.premiumlauncher.widgethost.ThirdPartyWidgetWrapper
import com.example.premiumlauncher.drawer.AppDrawer

class SettingsActivity : ComponentActivity() {
    private val PREFS_NAME = "PremiumLauncherPrefs"
    private val GRID_COLS_KEY = "grid_cols"
    private val GRID_ROWS_KEY = "grid_rows"
    private val GLASS_TINT_KEY = "glass_tint"
    private val BLUR_RADIUS_KEY = "blur_radius"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsTheme {
                SettingsScreen(this)
            }
        }
    }
}

@Composable
fun SettingsScreen(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Settings state
    val gridCols by remember { mutableStateOf(
        prefs.getInt(GRID_COLS_KEY, 3)
    ) }
    val gridRows by remember { mutableStateOf(
        prefs.getInt(GRID_ROWS_KEY, 3)
    ) }
    val glassTint by remember { mutableStateOf(
        prefs.getInt(GLASS_TINT_KEY, 0xFF00FFAA)
    ) }
    val blurRadius by remember { mutableStateOf(
        prefs.getInt(BLUR_RADIUS_KEY, 20)
    ) }

    // Gesture mapping state (simplified)
    val singleTapAction by remember { mutableStateOf("Launch App") }
    val doubleTapAction by remember { mutableStateOf("Open Widgets") }
    val swipeDownAction by remember { mutableStateOf("Open App Drawer") }
    val twoFingerRotateAction by remember { mutableStateOf("Open Settings") }

    // Save settings when changed
    LaunchedEffect(gridCols, gridRows, glassTint, blurRadius) {
        prefs.edit()
            .putInt(GRID_COLS_KEY, gridCols)
            .putInt(GRID_ROWS_KEY, gridRows)
            .putInt(GLASS_TINT_KEY, glassTint)
            .putInt(BLUR_RADIUS_KEY, blurRadius)
            .apply()
    }

    // Main settings container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Dark background for settings
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll()
        ) {
            // Header
            Text(
                text = "Settings",
                color = Color(0xFF00FFAA),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Granular Grid Configurator
            SettingsSection(
                title = "Grid Configuration",
                icon = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = androidx.compose.ui.R.drawable.ic_grid_on)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .spacing(12.dp)
                ) {
                    Text(
                        text = "Columns: $gridCols",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Slider(
                        value = gridCols,
                        onValueChange = { gridCols = it.roundToInt() },
                        valueRange = 2..8,
                        steps = 6,
                        color = Color(0xFF00FFAA),
                        thumbColor = Color.White
                    )

                    Text(
                        text = "Rows: $gridRows",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Slider(
                        value = gridRows,
                        onValueChange = { gridRows = it.roundToInt() },
                        valueRange = 2..8,
                        steps = 6,
                        color = Color(0xFF00FFAA),
                        thumbColor = Color.White
                    )

                    Text(
                        text = "Changes will apply to home screen layout",
                        color = Color(0x88FFFFFF),
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            // Glass Tint & Blur Density Sliders
            SettingsSection(
                title = "Glass Appearance",
                icon = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = androidx.compose.ui.R.drawable.ic_wb_iridescent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .spacing(12.dp)
                ) {
                    Text(
                        text = "Glass Tint",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    // Color picker simplified to hue slider
                    Slider(
                        value = (glassTint and 0xFFFFFF) shr 0, // Extract hue component
                        onValueChange = {
                            val newTint = (glassTint and 0xFF000000) or (it shl 0 and 0xFFFFFF)
                            glassTint = newTint
                        },
                        valueRange = 0..360,
                        color = Color(0xFF00FFAA),
                        thumbColor = Color.White
                    )

                    Text(
                        text = "Blur Radius",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Slider(
                        value = blurRadius,
                        onValueChange = { blurRadius = it.roundToInt() },
                        valueRange = 0..40,
                        steps = 8,
                        color = Color(0xFF00FFAA),
                        thumbColor = Color.White
                    )

                    PreviewGlassEffect(
                        tint = glassTint,
                        blurRadius = blurRadius
                    )
                }
            }

            // Gesture Mapping Control Center
            SettingsSection(
                title = "Gesture Mapping",
                icon = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = androidx.compose.ui.R.drawable.ic_touch_app)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .spacing(12.dp)
                ) {
                    GestureOption(
                        title = "Single Tap",
                        currentAction = singleTapAction,
                        onChange = { singleTapAction = it },
                        options = listOf(
                            "Launch App", "Open App Drawer", "Open Widgets",
                            "Open Settings", "Quick Note", "Voice Assistant"
                        )
                    )
                    GestureOption(
                        title = "Double Tap",
                        currentAction = doubleTapAction,
                        onChange = { doubleTapAction = it },
                        options = listOf(
                            "Launch App", "Open App Drawer", "Open Widgets",
                            "Open Settings", "Quick Note", "Voice Assistant"
                        )
                    )
                    GestureOption(
                        title = "Swipe Down",
                        currentAction = swipeDownAction,
                        onChange = { swipeDownAction = it },
                        options = listOf(
                            "Open App Drawer", "Open Notification Panel",
                            "Quick Settings", "None"
                        )
                    )
                    GestureOption(
                        title = "Two Finger Rotate",
                        currentAction = twoFingerRotateAction,
                        onChange = { twoFingerRotateAction = it },
                        options = listOf(
                            "Open Settings", "Rotate Widget", "None", "Lock Screen"
                        )
                    )
                }
            }

            // Dynamic Wallpaper Engine Binding
            SettingsSection(
                title = "Wallpaper & Effects",
                icon = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = androidx.compose.ui.R.drawable.ic_wallpaper)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .spacing(12.dp)
                ) {
                    ToggleSetting(
                        title = "Live Wallpaper Support",
                        description = "Enable interactive wallpapers that respond to touch",
                        isOn = true, // In real implementation, read from prefs
                        onToggle = { /* TODO: implement */ }
                    )
                    ToggleSetting(
                        title = "Motion Effects",
                        description = "Enable parallax and motion effects on home screen",
                        isOn = true,
                        onToggle = { /* TODO: implement */ }
                    )
                    ToggleSetting(
                        title = "Blur Behind Windows",
                        description = "Apply blur effect to popup windows",
                        isOn = true,
                        onToggle = { /* TODO: implement */ }
                    )
                }
            }

            // Apply and Reset buttons
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .justify(Arrangement.SpaceEvenly)
            ) {
                Button(
                    onClick = {
                        // In a real implementation, we'd apply settings to running launcher
                        // For now, just show confirmation
                    }
                ) {
                    Text("Apply Changes")
                }
                Button(
                    onClick = {
                        // Reset to defaults
                        gridCols.value = 3
                        gridRows.value = 3
                        glassTint.value = 0xFF00FFAA
                        blurRadius.value = 20
                    }
                ) {
                    Text("Reset to Default")
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = Color(0x66000000),
                shape = RoundedCornerShape(16.dp)
            )
            .border(1.dp, Color(0x3300FFAA), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .spacing(12.dp)
                .align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF00FFAA),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
fun GestureOption(
    title: String,
    currentAction: String,
    onChange: (String) -> Unit,
    options: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(
                color = Color(0x22000000),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Text(
            text = title,
            color = Color(0xFF00FFAA),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Current: $currentAction",
            color = Color.White,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap to change",
            color = Color(0x88FFFFFF),
            fontSize = 12.sp
        )
        // In a full implementation, this would open a selection dialog
        // For now, we'll just show it's tappable
    }
    .clickable {
        // Cycle to next option
        val currentIndex = options.indexOfFirst { it == currentAction }
        val nextIndex = (currentIndex + 1) % options.size
        onChange(options[nextIndex])
    }
}

@Composable
fun ToggleSetting(
    title: String,
    description: String,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = Color(0x22000000),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = description,
                color = Color(0x88FFFFFF),
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(
            onClick = { onToggle(!isOn) }
        ) {
            Icon(
                imageVector = if (isOn) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                contentDescription = if (isOn) "Enabled" else "Disabled",
                tint = if (isOn) Color(0xFF00FFAA) else Color(0x88FFFFFF)
            )
        }
    }
}

@Composable
fun PreviewGlassEffect(
    tint: Int,
    blurRadius: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                color = Color(0x66000000),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color(tint),
                shape = RoundedCornerShape(16.dp)
            )
            .blur(blurRadius.dp)
            .padding(4.dp)
    ) {
        Text(
            text = "Glass Effect Preview",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}