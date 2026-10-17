package com.example.premiumlauncher.drawer

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.premiumlauncher.R
import com.example.premiumlauncher.ui.theme.PremiumLauncherTheme
import com.example.premiumlauncher.iconengine.IconProcessor
import com.example.premiumlauncher.iconengine.GlassAppTile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsStateWithLifecycle

// Data class for apps in the drawer
data class DrawerApp(
    val packageName: String,
    val label: String,
    val icon: Int, // In real implementation, this would come from IconProcessor
    val usageScore: Float = 0f // For predictive AI
)

@Composable
fun AppDrawer(
    context: Context = LocalContext.current,
    isDrawerOpen: Boolean,
    onDrawerClose: () -> Unit,
    onAppLaunch: (String) -> Unit,
    onVaultAccessRequested: () -> Unit
) {
    val packageManager: PackageManager = context.packageManager
    val vibrator = LocalVibrator.current

    // State for apps in the drawer
    val drawerApps by remember { mutableStateOf(mutableListOf<DrawerApp>()) }
    val searchQuery by remember { mutableStateOf("") }
    val predictiveApps by remember { mutableStateOf(mutableListOf<DrawerApp>()) }
    val isSearching by remember { mutableStateOf(false) }

    // Load apps from package manager
    LaunchedEffect(Unit) {
        if (drawerApps.isEmpty()) {
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
            intent.addCategory(android.intent.category.LAUNCHER)
            val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
            resolveInfoList.forEach { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(packageManager).toString()
                // In a real implementation, we'd get the actual icon via IconProcessor
                drawerApps.add(DrawerApp(packageName, label, 0)) // Placeholder icon
            }
            // Sort by usage score for predictive AI (mock data)
            drawerApps.sortByDescending { it.usageScore }
            predictiveApps.clear()
            predictiveApps.addAll(drawerApps.take(5)) // Top 5 for predictive dock
        }
    }

    // Filter apps based on search query
    val filteredApps = if (searchQuery.isNotEmpty()) {
        drawerApps.filter {
            it.label.lowercase().contains(searchQuery.lowercase()) ||
                    it.packageName.lowercase().contains(searchQuery.lowercase())
        }
    } else {
        drawerApps
    }

    // Drawer animation
    val drawerOffset by animateDpAsState(
        targetValue = if (isDrawerOpen) 0 else 300.dp, // Closed drawer is offset off-screen
        animationSpec = tween(
            durationMs = 250,
            easing = FastOutSlowInEasing
        )
    )

    // Progressive blur effect for the drawer
    val blurRadius by animateFloatAsState(
        targetValue = if (isDrawerOpen) 0f else 20f,
        animationSpec = tween(
            durationMs = 250,
            easing = FastOutSlowInEasing
        )
    )

    // Main drawer container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = drawerOffset.value)
            .background(
                color = Color(0xFF000000),
                shape = RectangleShape
            )
            .blur(blurRadius.dp)
            // Handle drag to close
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { _, _ -> },
                    onDragEnd = { _, dragAmount ->
                        // If dragged down past threshold, close drawer
                        if (dragAmount.y > 100) {
                            onDrawerClose()
                            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    }
                )
            }
    ) {
        // App Drawer Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Predictive AI App Dock (top row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .spacing(12.dp)
            ) {
                Text(
                    text = "Predictive Dock",
                    color = Color(0xFF00FFAA),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                predictiveApps.forEach { app ->
                    GlassAppTile(
                        packageName = app.packageName,
                        label = app.label,
                        badgeCount = null,
                        modifier = Modifier.size(60.dp),
                        onClick = { onAppLaunch(app.packageName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar with real-time fuzzy search
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = Color(0x66000000),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, Color(0x3300FFAA), RoundedCornerShape(16.dp))
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        isSearching = it.isNotEmpty()
                        // Haptic feedback for each character
                        if (it.length > searchQuery.length) {
                            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    },
                    label = { Text("Search apps...") },
                    placeholder = { Text("Search apps...") },
                    textColor = Color.White,
                    labelColor = Color(0x88FFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                // Search icon
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF00FFAA),
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // A-Z FastScroll Matrix Tracker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .verticalAlign(Alignment.CenterVertically)
            ) {
                // A-Z indicator
                val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                alphabet.forEach { letter ->
                    val isActive = searchQuery.isNotEmpty() &&
                            searchQuery.first().uppercase() == letter
                    Text(
                        text = letter.toString(),
                        color = if (isActive) Color(0xFF00FFAA) else Color(0x55FFFFFF),
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )
                    if (letter != 'Z') Spacer(modifier = Modifier.width(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.dp)
                    .weight(1f)
            ) {
                if (filteredApps.isEmpty() && isSearching) {
                    // No results found
                    Text(
                        text = "No apps found",
                        color = Color(0x88FFFFFF),
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredApps.size) { index ->
                            val app = filteredApps[index]
                            Column(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        color = Color(0x22000000),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color(0x33FFFFFF),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onAppLaunch(app.packageName)
                                        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                                    }
                                    .indication(
                                        remember { MutableInteractionSource(
                                                PressInteraction.Press,
                                                PressInteraction.Release
                                        ) }
                                    )
                            ) {
                                GlassAppTile(
                                    packageName = app.packageName,
                                    label = app.label,
                                    badgeCount = null,
                                    modifier = Modifier.size(56.dp),
                                    onClick = { /* handled by parent */ }
                                )
                                Text(
                                    text = app.label,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                    overflow = TextOverflow.Ellipsis
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                        .padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hidden Stealth Vault Access
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = "Swipe right for vault",
                    color = Color(0x88FFFFFF),
                    fontSize = 12.sp
                )
                IconButton(
                    onClick = onVaultAccessRequested
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Access Vault",
                        tint = Color(0xFF00FFAA)
                    )
                }
            }
        }
    }
}