package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ClickAction
import com.example.domain.model.ConnectionState
import com.example.domain.model.ControlAction
import com.example.domain.model.MouseButton
import com.example.ui.MainViewModel

@Composable
fun ScreenModeScreen(viewModel: MainViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    val connectedDevice by viewModel.connectedDevice.collectAsState()

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedMonitor by remember { mutableIntStateOf(1) }
    var isStreamActive by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- TOP TOOLBAR FOR SCREEN MODE ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Tv, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(
                        text = "Tela Remota (WebRTC)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (connectionState == ConnectionState.CONNECTED) "1080p • 60 FPS • 4ms" else "Aguardando conexão...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Monitor Selector & Reset Zoom
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalButton(
                    onClick = {
                        selectedMonitor = if (selectedMonitor == 1) 2 else 1
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Mon $selectedMonitor", fontSize = 12.sp)
                }

                IconButton(
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    }
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset Zoom", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // --- MAIN REMOTE DISPLAY CANVAS ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF090D12))
                .border(1.dp, Color(0xFF30363D), RoundedCornerShape(20.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        if (scale > 1f) {
                            offset = Offset(offset.x + pan.x, offset.y + pan.y)
                        } else {
                            offset = Offset.Zero
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            // Map local tap to remote screen click
                            viewModel.sendAction(ControlAction.MouseClick(MouseButton.LEFT, ClickAction.CLICK))
                        },
                        onDoubleTap = {
                            viewModel.sendAction(ControlAction.MouseClick(MouseButton.LEFT, ClickAction.DOUBLE_CLICK))
                        },
                        onLongPress = {
                            viewModel.sendAction(ControlAction.MouseClick(MouseButton.RIGHT, ClickAction.CLICK))
                        }
                    )
                }
        ) {
            // Simulated / Streamed Virtual Desktop Canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Desktop Background Mock/Renderer Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A),
                                    Color(0xFF134E4A)
                                )
                            )
                        )
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(12.dp))
                ) {
                    // Desktop Taskbar Simulator
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Window, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(14.dp))
                            Icon(Icons.Default.Folder, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(14.dp))
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }

                    // Centered Status / Remote Stream Info
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (connectionState == ConnectionState.CONNECTED) Color(0xFF4CAF50) else Color(0xFFFFB300))
                        )
                        Text(
                            text = if (connectionState == ConnectionState.CONNECTED) {
                                "${connectedDevice?.name ?: "PC"} • Monitor $selectedMonitor"
                            } else {
                                "Conecte-se ao Desktop para transmissão WebRTC"
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Toque para clicar  •  Pressione longo para Botão Direito  •  Pinça para Zoom",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Floating Zoom Badge
            if (scale > 1f) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "Zoom: %.1fx".format(scale),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- BOTTOM QUICK SCREEN ACTIONS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = { viewModel.sendShortcut(listOf("ALT"), "TAB") },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Alt + Tab", fontSize = 12.sp)
            }

            FilledTonalButton(
                onClick = { viewModel.sendShortcut(listOf("WIN"), "D") },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Desktop (Win+D)", fontSize = 12.sp)
            }

            FilledTonalButton(
                onClick = { viewModel.sendShortcut(listOf("CTRL", "ALT"), "DELETE") },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Ctrl+Alt+Del", fontSize = 12.sp)
            }
        }
    }
}
