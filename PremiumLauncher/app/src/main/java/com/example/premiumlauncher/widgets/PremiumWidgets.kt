package com.example.premiumlauncher.widgets

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.Background
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.fx
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.telephony.TelephonyManager
import android.os.Build
import android.provider.Settings
import android.telephony.CellInfo
import android.telephony.CellSignalStrength
import android.telephony.SignalStrength
import android.os.BatteryManager
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.content.Context
import android.content.BroadcastReceiver
import androidx.core.content.ContextCompat
import java.util.*

// ===== DOT-MATRIX KINETIC CLOCK =====
@Composable
fun DotMatrixKineticClock(
    modifier: Modifier = Modifier.size(160.dp) // 2x2 tile size
) {
    val context = LocalContext.current
    val currentTime by remember {
        // In a real implementation, this would be updated every minute
        // For demo, we'll use a fixed time that updates occasionally
        val time = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            .format(java.time.ZonedDateTime.now())
        time
    }

    val hours = currentTime.substring(0, 2)
    val minutes = currentTime.substring(3, 5)

    // Dot matrix representation for each digit
    val digitMap = mapOf(
        '0' to listOf(
            listOf(true, true, true, true, true),
            listOf(true, false, false, false, true),
            listOf(true, false, false, false, true),
            listOf(true, false, false, false, true),
            listOf(true, true, true, true, true)
        ),
        '1' to listOf(
            listOf(false, false, true, false, false),
            listOf(false, true, true, false, false),
            listOf(false, false, true, false, false),
            listOf(false, false, true, false, false),
            listOf(false, true, true, true, true)
        ),
        '2' to listOf(
            listOf(true, true, true, true, true),
            listOf(false, false, false, false, true),
            listOf(true, true, true, true, true),
            listOf(true, false, false, false, false),
            listOf(true, true, true, true, true)
        ),
        '3' to listOf(
            listOf(true, true, true, true, true),
            listOf(false, false, false, false, true),
            listOf(true, true, true, true, true),
            listOf(false, false, false, false, true),
            listOf(true, true, true, true, true)
        ),
        '4' to listOf(
            listOf(true, false, false, false, true),
            listOf(true, false, false, false, true),
            listOf(true, true, true, true, true),
            listOf(false, false, false, false, true),
            listOf(false, false, false, false, true)
        ),
        '5' to listOf(
            listOf(true, true, true, true, true),
            listOf(true, false, false, false, false),
            listOf(true, true, true, true, true),
            listOf(false, false, false, false, true),
            listOf(true, true, true, true, true)
        ),
        '6' to listOf(
            listOf(true, true, true, true, true),
            listOf(true, false, false, false, false),
            listOf(true, true, true, true, true),
            listOf(true, false, false, false, true),
            listOf(true, true, true, true, true)
        ),
        '7' to listOf(
            listOf(true, true, true, true, true),
            listOf(false, false, false, false, true),
            listOf(false, false, false, false, true),
            listOf(false, false, false, false, true),
            listOf(false, false, false, false, true)
        ),
        '8' to listOf(
            listOf(true, true, true, true, true),
            listOf(true, false, false, false, true),
            listOf(true, true, true, true, true),
            listOf(true, false, false, false, true),
            listOf(true, true, true, true, true)
        ),
        '9' to listOf(
            listOf(true, true, true, true, true),
            listOf(true, false, false, false, true),
            listOf(true, true, true, true, true),
            listOf(false, false, false, false, true),
            listOf(true, true, true, true, true)
        )
    )

    Box(
        modifier = modifier
            .background(
                color = Color(0x66000000),
                shape = RoundedCornerShape(20.dp)
            )
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
    ) {
        // Draw the time in dot matrix style
        val allDigits = hours + minutes
        val digitWidth = 20.dp
        val digitHeight = 24.dp
        val dotSize = 4.dp
        val dotSpacing = 2.dp
        val padding = 16.dp

        allDigits.forEachIndexed { index, digit ->
            val digitPatterns = digitMap[digit] ?: digitMap['0'] ?: emptyList()
            val xOffset = padding + index * (digitWidth + 8.dp)

            digitPatterns.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { colIndex, isOn ->
                    val x = xOffset + colIndex * (dotSize + dotSpacing)
                    val y = padding + rowIndex * (dotSize + dotSpacing) +
                        (if (index >= 2) digitHeight else 0.dp) // Offset for minutes

                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .offset(x.intValue(), y.intValue())
                            .background(
                                color = if (isOn) Color(0xFF00FFAA) else Color(0x33FFFFFF),
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        // Add kinetic effect - subtle movement
        val timeMillis = System.currentTimeMillis()
        val waveOffset = sin(timeMillis * 0.001) * 2.0
        // In a full implementation, we'd animate individual dots
    }
}

// ===== HYPER-LOCAL METEOROLOGICAL GLASS WIDGET =====
@Composable
fun HyperLocalMeteorologicalGlass(
    modifier: Modifier = Modifier.size(160.dp) // 2x2 tile size
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x66000000),
                        Color(0x99001122)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(1.dp, Color(0x3300FFAA), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .horizontalAlign(Alignment.CenterHorizontally)
        ) {
            // Weather icon (simplified)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00FFAA),
                                Color(0xFF00AAFF)
                            )
                        ),
                        shape = CircleShape
                    )
                    .alpha(0.8f)
            ) {
                // Sun/cloud/rain icon would go here
                Icon(
                    imageVector = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = androidx.compose.ui.R.drawable.ic_wb_sunny),
                    contentDescription = "Weather",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Temperature
            Text(
                text = "22°",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Location and conditions
            Text(
                text = "Partly Cloudy",
                color = Color(0xFF00FFAA),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Additional weather details in a subtle glass panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .justify(Arrangement.SpaceBetween)
            ) {
                Column {
                    Text("Humidity", color = Color(0x88FFFFFF), fontSize = 10.sp)
                    Text("65%", color = Color.White, fontSize = 12.sp)
                }
                Column {
                    Text("Wind", color = Color(0x88FFFFFF), fontSize = 10.sp)
                    Text("12 km/h", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

// ===== FLUID AUDIO WAVE MUSIC WIDGET =====
@Composable
fun FluidAudioWaveMusicWidget(
    modifier: Modifier = Modifier.size(160.dp) // 2x2 tile size
) {
    val audioFocus by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(
                color = Color(0x66000000),
                shape = RoundedCornerShape(20.dp)
            )
            .border(1.dp, Color(0x33FF00AA), RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { /* Handle play/pause */ },
                    onDoubleTap = { /* Handle next track */ }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .horizontalAlign(Alignment.CenterHorizontally)
                .verticalArrangement = Arrangement.Center
        ) {
            // Music icon with fluid effect
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.sweepGradient(
                            angles = listOf(0f, 360f),
                            colors = listOf(
                                Color(0xFFFF00AA),
                                Color(0xFFFF00FF),
                                Color(0xFFFF00AA)
                            ),
                            tileMode = android.graphics.Shader.TileMode.Clamp
                        ),
                        shape = CircleShape
                    )
                    .alpha(0.9f)
            ) {
                Icon(
                    imageVector = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = androidx.compose.ui.R.drawable.ic_music_note),
                    contentDescription = "Music",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Now playing info
            Text(
                text = "Midnight City",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "M83",
                color = Color(0xFF00FFAA),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fluid audio waveform
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(
                        color = Color(0x33000000),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val waveCount = 5

                    repeat(waveCount) { i ->
                        val x = (width / waveCount) * i + (width / (waveCount * 2))
                        val waveHeight = height * 0.6
                        val offset = sin(System.currentTimeMillis() * 0.005 + i) * waveHeight * 0.4

                        drawLine(
                            from = Offset(x, height/2),
                            to = Offset(x, height/2 + offset),
                            color = Color(0xFF00FFAA),
                            strokeWidth = 2f
                        )

                        // Add fluid glow effect
                        drawLine(
                            from = Offset(x, height/2),
                            to = Offset(x, height/2 + offset),
                            color = Color(0x3300FFAA),
                            strokeWidth = 6f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .justify(Arrangement.SpaceEvenly)
            ) {
                IconButton(
                    onClick = { /* Previous */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = { /* Play/Pause */ }
                ) {
                    Icon(
                        imageVector = if (audioFocus) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (audioFocus) "Pause" else "Play",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = { /* Next */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// ===== SYSTEM TELEMETRY MATRIX =====
@Composable
fun SystemTelemetryMatrix(
    modifier: Modifier = Modifier.size(160.dp) // 2x2 tile size
) {
    // In a real implementation, we'd collect actual system metrics
    val cpuUsage by remember { mutableStateOf(45) }
    val ramUsage by remember { mutableStateOf(62) }
    val batteryLevel by remember { mutableStateOf(78) }
    val temperature by remember { mutableStateOf(38) }

    Box(
        modifier = modifier
            .background(
                color = Color(0x66000000),
                shape = RoundedCornerShape(20.dp)
            )
            .border(1.dp, Color(0x3300AAFF), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .horizontalAlign(Alignment.CenterHorizontally)
                .verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Text(
                text = "SYSTEM",
                color = Color(0xFF00FFAA),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            // Telemetry grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(4) { index ->
                    when (index) {
                        0 -> TelemetryItem(
                            label = "CPU",
                            value = "$cpuUsage%",
                            icon = Icons.Default.Speed,
                            color = Color(0xFFFF00AA)
                        )
                        1 -> TelemetryItem(
                            label = "RAM",
                            value = "$ramUsage%",
                            icon = Icons.Default.Memory,
                            color = Color(0xFF00FFAA)
                        )
                        2 -> TelemetryItem(
                            label = "BATT",
                            value = "$batteryLevel%",
                            icon = Icons.Default.BatteryStd,
                            color = if (batteryLevel > 20) Color(0xFF00FF00) else Color(0xFFFF0000)
                        )
                        3 -> TelemetryItem(
                            label = "TEMP",
                            value = "$temperature°C",
                            icon = Icons.Default.Thermostat,
                            color = Color(0xFFFF8800)
                        )
                    }
                }
            }

            // Subtle animated background effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0x0000FFAA),
                                Color(0x6600FFAA),
                                Color(0x0000FFAA)
                            )
                        )
                    )
                    .alpha(0.1f)
            )
        }
    }
}

@Composable
private fun TelemetryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalAlign(Alignment.CenterHorizontally)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = color,
            fontSize = 10.sp
        )
    }
}

// ===== INTERACTIVE GLASS QUICK-NOTES WIDGET =====
@Composable
fun InteractiveGlassQuickNotes(
    modifier: Modifier = Modifier.size(160.dp) // 2x2 tile size
) {
    val notes by remember { mutableStateOf("Tap to add note...") }
    val isEditing by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(
                color = Color(0x66000000),
                shape = RoundedCornerShape(20.dp)
            )
            .border(1.dp, Color(0x33FFAA00), RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isEditing = true
                        // In a real implementation, we'd show a text input
                    }
                )
            }
    ) {
        if (isEditing) {
            // Show input field (simplified)
            TextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Quick Note") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        color = Color(0x66FFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp)
            )
        } else {
            // Display notes
            Text(
                text = notes,
                color = if (notes == "Tap to add note...") Color(0x88FFFFFF) else Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )

            // Add button
            IconButton(
                onClick = { /* Add new note */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note",
                    tint = Color(0xFFFFAA00),
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}