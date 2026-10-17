package com.example.premiumlauncher.home

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.graphics.layer.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Blur
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalVibrator
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fx
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.*
import com.google.accompanist.pager.indicators.*
import com.example.premiumlauncher.R
import com.example.premiumlauncher.ui.theme.PremiumLauncherTheme
import com.example.premiumlauncher.iconengine.IconProcessor
import com.example.premiumlauncher.iconengine.GlassAppTile
import com.example.premiumlauncher.drawer.AppDrawer
import com.example.premiumlauncher.dock.LiquidDock

// Data classes for home screen items
sealed interface HomeScreenItem {
    val id: String
    val label: String
}

data class AppItem(
    override val id: String, // package name
    override val label: String,
    var pageIndex: Int = 0,
    var x: Int = 0, // position within the page grid (column)
    var y: Int = 0  // position within the page grid (row)
) : HomeScreenItem

data class FolderItem(
    override val id: String,
    override val label: String,
    val contents: List<HomeScreenItem>, // can contain apps or other folders
    var pageIndex: Int = 0,
    var x: Int = 0,
    var y: Int = 0,
    var isExpanded: Boolean = false,
    var expansionProgress: Float = 0f
) : HomeScreenItem

@Composable
fun HomeScreen(
    vibrator: Vibrator = LocalVibrator.current,
    onAppClick: (packageName: String) -> Unit = {},
    onFolderClick: (folderId: String) -> Unit = {},
    onFolderExpand: (folderId: String) -> Unit = {}
) {
    val context = LocalContext.current
    val packageManager: PackageManager = context.packageManager

    // State for the items on the home screen
    val items = remember { mutableStateListOf<HomeScreenItem>() }
    // State for whether we have loaded the apps
    val appsLoaded by remember { mutableStateOf(false) }

    // State for the app drawer
    val drawerOpen by remember { mutableStateOf(false) }

    // State for the liquid dock
    val dockApps by remember { mutableStateOf(mutableListOf<String>()) }
    var dockDragOffset by remember { mutableStateOf(Offset.Zero) }

    // Load the list of apps from the package manager
    LaunchedEffect(Unit) {
        if (!appsLoaded) {
            // Get all launchable apps
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
            intent.addCategory(android.intent.category.LAUNCHER)
            val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
            // Clear the items list and add apps
            items.clear()
            resolveInfoList.forEach { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(packageManager).toString()
                items.add(AppItem(packageName, label))
            }
            // Distribute them across pages (we'll assign positions later in a layout pass)
            appsLoaded = true

            // Initialize dock with some favorite apps (first 4 apps)
            if (items.size >= 4) {
                dockApps.clear()
                dockApps.addAll(items.take(4).map { (it as AppItem).id })
            } else if (!items.isEmpty()) {
                dockApps.clear()
                dockApps.addAll(items.map { (it as AppItem).id })
            }
        }
    }

    // Wait until apps are loaded
    if (!appsLoaded) {
        // Show a loading indicator with liquid effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00FFAA),
                                Color(0xFF00AAFF),
                                Color(0xFF00FFAA)
                            )
                        )
                    )
                    .alpha(0.8f)
                    .blur(8.dp)
                    .align(Alignment.Center)
            )
        }
        return@HomeScreen
    }

    // Organize items by page
    val itemsPerPage = 6
    val pages = items
        .chunked(itemsPerPage)
        .map { it.toList() }
        .toList()

    // State for drag and drop
    var draggedItem by remember { mutableStateOf<HomeScreenItem?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragState by remember { mutableStateOf<DragState>(DragState.Normal) }
    var dragOverItem by remember { mutableStateOf<HomeScreenItem?>(null) } // Item being dragged over

    enum class DragState {
        Normal,
        Dragging,
        DraggingOver
    }

    // Pager state
    val pagerState = rememberPagerState()

    // Detect swipe up gesture to open app drawer
    val swipeUpDetected = remember { mutableStateOf(false) }
    val swipePointerInput = rememberUpdatedState {
        PointerInputScope {
            detectVerticalDragGestures(
                onDragStart = { _ -> },
                onDragEnd = { _, velocityY ->
                    // If swipe up with sufficient velocity, open drawer
                    if (velocityY < -1500) { // Strong upward swipe
                        drawerOpen.value = true
                        vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                    // If swipe down strongly, close drawer if open
                    else if (velocityY > 1500 && drawerOpen.value) {
                        drawerOpen.value = false
                        vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
            )
        }
    }

    // Main container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Deep black background
            .pointerInput(swipePointerInput)
    ) {
        // Horizontal pager for pages
        HorizontalPager(
            pageCount = pages.size,
            state = pagerState,
            contentPadding = PaddingValues(16.dp)
        ) { pageIndex ->
            // Each page is a vertical grid
            val pageItems = pages[pageIndex] // items for this page
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3), // 3 columns
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(pageItems.size) { index ->
                        val item = pageItems[index]
                        val col = index % 3
                        val row = index / 3
                        // Update the item's position in the master list
                        LaunchedEffect(item, pageIndex, col, row) {
                            items.first { it.id == item.id }.apply {
                                this@apply.pageIndex = pageIndex
                                this@apply.x = col
                                this@apply.y = row
                            }
                        }

                        // Determine if this item is being dragged over
                        val isBeingDraggedOver = dragOverItem?.id == item.id && draggedItem?.id != item.id

                        // Container for the item
                        val containerModifier = Modifier
                            .size(88.dp)
                            .background(
                                color = Color(0x22000000), // Very dark semi-transparent
                                shape = RoundedCornerShape(18.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = Color(0x33FFFFFF),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .offset(
                                // If this item is being dragged, apply the drag offset
                                if (draggedItem?.id == item.id) {
                                    IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt())
                                } else {
                                    IntOffset.Zero
                                }
                            )
                            .pointerInput(Unit) {
                                // Detect drag gestures for this item
                                detectDragGestures(
                                    onDragStart = { _, dragOffset ->
                                        // Start dragging this item
                                        draggedItem = item
                                        this.dragOffset = dragOffset
                                        dragState = DragState.Dragging
                                        // Haptic feedback
                                        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                                    },
                                    onDragCancel = {
                                        // Cancel drag
                                        draggedItem = null
                                        dragState = DragState.Normal
                                        dragOverItem = null
                                    },
                                    onDragEnd = { _, dragOffset ->
                                        // End drag: handle drop
                                        draggedItem = null
                                        dragState = DragState.Normal
                                        // Process the drop
                                        processDrop(index, pageItems, dragOffset)
                                        dragOverItem = null
                                    },
                                    onDrag = { _, dragOffset ->
                                        this.dragOffset = dragOffset
                                        // Check if we are dragging over another item
                                        val hoveredItem = findHoveredItem(index, pageItems, dragOffset)
                                        dragOverItem = hoveredItem
                                        dragState = if (hoveredItem != null && hoveredItem.id != item.id) {
                                            DragState.DraggingOver
                                        } else {
                                            DragState.Dragging
                                        }
                                    }
                                )
                            }
                            // Liquid visual effects when dragging over
                            .scaleY(
                                if (isBeingDraggedOver) {
                                    0.85f // Scale down to 85% (15% reduction)
                                } else 1f
                            )
                            .alpha(
                                if (isBeingDraggedOver) {
                                    0.7f // Slightly transparent to simulate blur
                                } else 1f
                            )
                            .blur(
                                if (isBeingDraggedOver) {
                                    4.dp
                                } else 0.dp
                            )
                            // Handle tap to launch the app or folder
                            .combinedClickable(
                                onClick = {
                                    if (draggedItem == null) {
                                        // Not dragging, so perform click
                                        when (item) {
                                            is AppItem -> onAppClick(item.id)
                                            is FolderItem -> {
                                                if (item.isExpanded) {
                                                    // Collapse folder
                                                    items.first { it.id == item.id }.isExpanded = false
                                                } else {
                                                    // Expand folder
                                                    items.first { it.id == item.id }.isExpanded = true
                                                    onFolderExpand(item.id)
                                                }
                                            }
                                        }
                                    }
                                ,
                                onLongClick = {
                                    // Long press: enter edit mode or show options
                                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                    true
                                }
                            )
                        }

                        containerModifier {
                            when (item) {
                                is AppItem -> {
                                    // Use the GlassAppTile to display the app icon
                                    GlassAppTile(
                                        packageName = item.id,
                                        label = item.label,
                                        badgeCount = null, // TODO: Implement notification badge
                                        onClick = { /* handled by container */ },
                                        onLongClick = { /* handled by container */ }
                                    )
                                }
                                is FolderItem -> {
                                    // Folder display with liquid expansion
                                    FolderDisplay(
                                        folder = item,
                                        onFolderClick = { folderId ->
                                            onFolderClick(folderId)
                                            // Toggle expansion
                                            items.first { it.id == folderId }.isExpanded = !it.isExpanded
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Page indicator
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )

        // Liquid Dock at the bottom
        LiquidDock(
            apps = dockApps,
            onAppLaunch = { packageName ->
                onAppClick(packageName)
                // Optionally, we could make launched apps appear in the dock
            },
            onDockDrag = { offset ->
                dockDragOffset = offset
                // In a full implementation, we'd allow rearranging dock items based on drag
            }
        )

        // App Drawer (slides up from bottom)
        if (drawerOpen.value) {
            AppDrawer(
                isDrawerOpen = drawerOpen.value,
                onDrawerClose = { drawerOpen.value = false },
                onAppLaunch = { packageName ->
                    drawerOpen.value = false
                    onAppClick(packageName)
                },
                onVaultAccessRequested = {
                    // TODO: Implement stealth vault access
                    drawerOpen.value = false
                    // In a full implementation, we'd navigate to the vault
                }
            )
        }
    }
}

/**
 * Process the drop action when dragging ends
 */
private fun processDrop(
    dropIndex: Int,
    pageItems: List<HomeScreenItem>,
    dragOffset: Offset
) {
    val dragged = draggedItem ?: return
    val dropCol = dropIndex % 3
    val dropRow = dropIndex / 3

    // If we're dragging over another item, create a folder or reorder
    val hoveredItem = findHoveredItem(dropIndex, pageItems, dragOffset)
    if (hoveredItem != null && hoveredItem.id != dragged.id) {
        // Create folder from the two items
        createFolderFromItems(dragged, hoveredItem)
    } else {
        // Just reorder to the drop position
        reorderItem(dragged, dropIndex, pageItems)
    }
}

/**
 * Find which item is being hovered over during drag
 */
private fun findHoveredItem(
    currentIndex: Int,
    pageItems: List<HomeScreenItem>,
    dragOffset: Offset
): HomeScreenItem? {
    val itemSize = 88.dp.toPx() // Convert to pixels
    val spacing = 16.dp.toPx()
    val currentCol = currentIndex % 3
    val currentRow = currentIndex / 3

    // The drag offset is relative to the item's original position
    val dragX = dragOffset.x
    val dragY = dragOffset.y

    // Check each other item
    return pageItems.firstOrNull { index ->
        if (index == currentIndex) return@firstOrNull false // Skip the dragged item itself
        val col = index % 3
        val row = index / 3
        // Calculate the center of the other item in the grid
        val itemCenterX = (col * (itemSize + spacing) + itemSize / 2).toFloat()
        val itemCenterY = (row * (itemSize + spacing) + itemSize / 2).toFloat()
        // Check if the drag offset is within a threshold distance of the item center
        val dx = dragX - itemCenterX
        val dy = dragY - itemCenterY
        val distance = Math.sqrt(dx * dx + dy * dy)
        distance < itemSize * 0.8 // Within 80% of item size
    }
}

/**
 * Create a folder from two items
 */
private fun createFolderFromItems(item1: HomeScreenItem, item2: HomeScreenItem) {
    // In a full implementation, we would:
    // 1. Remove both items from their current positions
    // 2. Create a new FolderItem containing both
    // 3. Add the folder at one of the positions
    // For now, we'll just note that this needs implementation
    // This is a complex operation that would require modifying the items list
}

/**
 * Reorder an item to a new position
 */
private fun reorderItem(
    item: HomeScreenItem,
    newIndex: Int,
    pageItems: List<HomeScreenItem>
) {
    // In a full implementation, we would move the item in the items list
    // and update its pageIndex, x, y accordingly
    // This is complex due to the paging system
}

/**
 * Folder display component with liquid expansion
 */
@Composable
fun FolderDisplay(
    folder: FolderItem,
    onFolderClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .background(
                color = Color(0x22000000),
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.5.dp,
                color = if (folder.isExpanded) Color(0xFF00FFAA) else Color(0x33FFFFFF),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onFolderClick(folder.id) }
    ) {
        if (folder.isExpanded) {
            // Expanded folder view - full screen liquid expansion
            ExpandedFolderView(
                folder = folder,
                onClose = { /* TODO: close folder */ }
            )
        } else {
            // Collapsed folder view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalAlign(Alignment.CenterHorizontally)
                    .verticalAlign(Alignment.CenterVertically)
            ) {
                // Folder icon (stack of dots)
                Row(
                    modifier = Modifier
                        .size(48.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (index == 1) Color(0xFF00FFAA) else Color(0xFFFFFF),
                                    shape = CircleShape
                                )
                        )
                    }
                }
                // Folder label
                Text(
                    text = folder.label,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Expanded folder view that takes full screen with liquid expansion
 */
@Composable
fun ExpandedFolderView(
    folder: FolderItem,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF00FFAA)
            )
        }

        // Folder contents in a grid
        if (folder.contents.isNotEmpty()) {
            val columns = 4
            val rows = ((folder.contents.size - 1) / columns) + 1
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(24.dp)
            ) {
                items(folder.contents.size) { index =>
                    val item = folder.contents[index]
                    when (item) {
                        is AppItem -> {
                            GlassAppTile(
                                packageName = item.id,
                                label = item.label,
                                badgeCount = null,
                                modifier = Modifier
                                    .size(80.dp)
                            )
                        }
                        is FolderItem -> {
                            // Nested folder (simplified)
                            Box(
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
                            ) {
                                Text(
                                    text = item.label,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}