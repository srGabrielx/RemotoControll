package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ConnectionState
import com.example.ui.MainViewModel
import com.example.ui.components.SettingsSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchpadScreen(viewModel: MainViewModel) {
    val view = LocalView.current
    val connectionState by viewModel.connectionState.collectAsState()
    val connectedDevice by viewModel.connectedDevice.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var isKeyboardActive by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    fun performHaptic() {
        if (settings.hapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    if (showSettingsSheet) {
        SettingsSheet(viewModel = viewModel, onDismiss = { showSettingsSheet = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- TOP STATUS BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Connection Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when (connectionState) {
                                ConnectionState.CONNECTED -> Color(0xFF4CAF50)
                                ConnectionState.CONNECTING, ConnectionState.PAIRING -> Color(0xFFFFB300)
                                else -> Color(0xFFE53935)
                            }
                        )
                )
                Column {
                    Text(
                        text = if (connectionState == ConnectionState.CONNECTED) {
                            connectedDevice?.name ?: "PC Conectado"
                        } else if (connectionState == ConnectionState.CONNECTING) {
                            "Conectando..."
                        } else {
                            "Desconectado"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (connectionState == ConnectionState.CONNECTED) {
                            connectedDevice?.ipAddress ?: "192.168.x.x"
                        } else {
                            "Toque em 'Devices' para conectar"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        isKeyboardActive = !isKeyboardActive
                        if (isKeyboardActive) {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        } else {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Keyboard,
                        contentDescription = "Teclado",
                        tint = if (isKeyboardActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { showSettingsSheet = true }) {
                    Icon(Icons.Default.Tune, contentDescription = "Ajustes", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // --- KEYBOARD QUICK INPUT BAR (Optional toggle) ---
        AnimatedVisibility(
            visible = isKeyboardActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { newValue ->
                            if (newValue.length > inputText.length) {
                                val added = newValue.substring(inputText.length)
                                viewModel.sendText(added)
                            } else if (newValue.length < inputText.length) {
                                viewModel.sendKey("BACKSPACE")
                            }
                            inputText = newValue
                        },
                        placeholder = { Text("Digitar no PC em tempo real...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                viewModel.sendKey("ENTER")
                                inputText = ""
                                performHaptic()
                            }
                        )
                    )
                    IconButton(
                        onClick = {
                            viewModel.sendKey("ENTER")
                            inputText = ""
                            performHaptic()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar Enter", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // --- MAIN TACTILE TRACKPAD ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF161B22),
                            Color(0xFF0D1117)
                        )
                    )
                )
                .border(1.dp, Color(0xFF30363D), RoundedCornerShape(24.dp))
                .pointerInput(settings) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        var isTap = true
                        var initialTime = System.currentTimeMillis()

                        while (true) {
                            val event = awaitPointerEvent()
                            val pointers = event.changes

                            if (pointers.isEmpty() || pointers.all { !it.pressed }) {
                                break // Fingers lifted
                            }

                            if (pointers.size == 2) {
                                isTap = false
                                val dy = pointers.map { it.positionChange().y }.average().toFloat()
                                val dx = pointers.map { it.positionChange().x }.average().toFloat()
                                if (dy != 0f || dx != 0f) {
                                    viewModel.sendScroll(dy = -dy, dx = dx)
                                }
                            } else if (pointers.size == 1) {
                                val change = pointers.first()
                                val dx = change.positionChange().x
                                val dy = change.positionChange().y

                                if (dx != 0f || dy != 0f) {
                                    isTap = false
                                    viewModel.sendMouseMove(dx, dy)
                                }
                            }

                            pointers.forEach { it.consume() }
                        }

                        if (isTap && settings.tapToClick) {
                            val duration = System.currentTimeMillis() - initialTime
                            if (duration < 350) {
                                viewModel.sendLeftClick()
                                performHaptic()
                            }
                        }
                    }
                }
        ) {
            // Scroll guide indicator on the right edge
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
                    .width(4.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF30363D).copy(alpha = 0.6f))
            )

            // Center subtle trackpad icon & guidance
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = Color(0xFF8B949E).copy(alpha = 0.25f),
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "TRACKPAD",
                    color = Color(0xFF8B949E).copy(alpha = 0.35f),
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1 dedo = Mover & Clique  •  2 dedos = Scroll",
                    color = Color(0xFF8B949E).copy(alpha = 0.2f),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp
                )
            }

            // Bottom Physical Click Bars (Left & Right click pads)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(58.dp)
                    .background(Color(0xFF161B22).copy(alpha = 0.85f))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF30363D),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Click
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(bottomStart = 24.dp))
                        .clickable {
                            viewModel.sendLeftClick()
                            performHaptic()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ESQUERDO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color(0xFF30363D))
                )

                // Middle Click (Scroll wheel click)
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight()
                        .clickable {
                            viewModel.sendMiddleClick()
                            performHaptic()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.UnfoldMore,
                        contentDescription = "Botão do Meio",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color(0xFF30363D))
                )

                // Right Click
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(bottomEnd = 24.dp))
                        .clickable {
                            viewModel.sendRightClick()
                            performHaptic()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DIREITO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- MEDIA CONTROLS & ESSENTIAL PC KEYS ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Media Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = {
                        viewModel.sendSystemCommand("VOLUME_DOWN")
                        performHaptic()
                    }
                ) {
                    Icon(Icons.Default.VolumeDown, contentDescription = "Volume -")
                }

                FilledTonalIconButton(
                    onClick = {
                        viewModel.sendSystemCommand("VOLUME_MUTE")
                        performHaptic()
                    }
                ) {
                    Icon(Icons.Default.VolumeMute, contentDescription = "Mudo")
                }

                FilledTonalIconButton(
                    onClick = {
                        viewModel.sendSystemCommand("VOLUME_UP")
                        performHaptic()
                    }
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Volume +")
                }

                FilledIconButton(
                    onClick = {
                        viewModel.sendSystemCommand("MEDIA_PLAY_PAUSE")
                        performHaptic()
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play/Pause", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }

            // Shortcut / System Keys Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.sendKey("ESCAPE")
                        performHaptic()
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ESC", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.sendKey("TAB")
                        performHaptic()
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("TAB", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.sendKey("WIN")
                        performHaptic()
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("WIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.sendShortcut(listOf("CTRL"), "Z")
                        performHaptic()
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CTRL+Z", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.sendKey("ENTER")
                        performHaptic()
                    },
                    modifier = Modifier.weight(1.2f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ENTER", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
