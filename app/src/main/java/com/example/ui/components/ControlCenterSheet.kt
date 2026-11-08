package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.view.KeyEvent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.SystemControlsManager
import com.example.ui.theme.LiquidGlassControlCenter
import com.example.ui.theme.LiquidGlassSheenEffect
import com.example.ui.theme.liquidGlassSurface
import kotlinx.coroutines.launch

@Composable
fun ControlCenterSheet(
    controlsManager: SystemControlsManager,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFlashlightOn by controlsManager.isFlashlightOn.collectAsState()
    val context = LocalContext.current
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) controlsManager.toggleFlashlight()
    }
    val volumeLevel by controlsManager.volumeLevel.collectAsState()
    val brightnessLevel by controlsManager.brightnessLevel.collectAsState()
    val isWifiOn by controlsManager.isWifiOn.collectAsState()
    val isBluetoothOn by controlsManager.isBluetoothOn.collectAsState()
    val isAirplaneMode by controlsManager.isAirplaneMode.collectAsState()
    val isRotationLocked by controlsManager.isRotationLocked.collectAsState()
    val isDndActive by controlsManager.isDndActive.collectAsState()
    val isPlayingMusic by controlsManager.isPlayingMusic.collectAsState()


    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
            .testTag("control_center_scrim"),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassSurface(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    backgroundColor = LiquidGlassControlCenter,
                    elevation = 20.dp,
                    borderWidth = 1.3.dp
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume taps */ }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
                .testTag("control_center_card"),
            contentAlignment = Alignment.Center
        ) {
            // Liquid Glass animated caustics sheen across top curved rim
            LiquidGlassSheenEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                alpha = 0.09f
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle pill
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFB0B0B5), Color(0xFFC7C7CC))
                            )
                        )
                        .clickable { onDismiss() }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Two-page Horizontal Pager (Page 0: System Controls, Page 1: Now Playing Music)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(345.dp)
                ) { page ->
                    if (page == 0) {
                        // Page 1: System Controls
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Row 1: Top 5 circular toggles (Airplane, Wi-Fi, Bluetooth, DND, Rotation)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                ControlCircleToggle(
                                    icon = Icons.Default.AirplanemodeActive,
                                    isActive = isAirplaneMode,
                                    activeColor = Color(0xFFFF9500),
                                    onClick = { controlsManager.toggleAirplaneMode() },
                                    tag = "cc_airplane"
                                )
                                ControlCircleToggle(
                                    icon = Icons.Default.Wifi,
                                    isActive = isWifiOn,
                                    activeColor = Color(0xFF007AFF),
                                    onClick = { controlsManager.toggleWifi() },
                                    tag = "cc_wifi"
                                )
                                ControlCircleToggle(
                                    icon = Icons.Default.Bluetooth,
                                    isActive = isBluetoothOn,
                                    activeColor = Color(0xFF007AFF),
                                    onClick = { controlsManager.toggleBluetooth() },
                                    tag = "cc_bluetooth"
                                )
                                ControlCircleToggle(
                                    icon = Icons.Default.DoNotDisturbOn,
                                    isActive = isDndActive,
                                    activeColor = Color(0xFF5856D6),
                                    onClick = { controlsManager.toggleDnd() },
                                    tag = "cc_dnd"
                                )
                                ControlCircleToggle(
                                    icon = Icons.Default.ScreenRotation,
                                    isActive = isRotationLocked,
                                    activeColor = Color(0xFFFF3B30),
                                    onClick = { controlsManager.toggleRotationLock() },
                                    tag = "cc_rotation"
                                )
                            }

                            // Row 2: Brightness slider with Liquid Glass trough
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlassSurface(
                                        shape = RoundedCornerShape(16.dp),
                                        backgroundColor = Color(0xE6FFFFFF),
                                        elevation = 5.dp,
                                        borderWidth = 1.1.dp
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BrightnessLow,
                                    contentDescription = "Low Brightness",
                                    tint = Color(0xFF8E8E93),
                                    modifier = Modifier.size(18.dp)
                                )
                                Slider(
                                    value = brightnessLevel,
                                    onValueChange = { controlsManager.setBrightness(it) },
                                    valueRange = 0.05f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color(0xFF007AFF),
                                        inactiveTrackColor = Color(0xFFE5E5EA)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 10.dp)
                                        .testTag("cc_brightness_slider")
                                )
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = "High Brightness",
                                    tint = Color(0xFF8E8E93),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Row 3: Android equivalents for screen casting and night light.
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .liquidGlassSurface(
                                            shape = RoundedCornerShape(16.dp),
                                            backgroundColor = Color(0xE8FFFFFF),
                                            elevation = 5.dp,
                                            borderWidth = 1.1.dp
                                        )
                                        .clickable { controlsManager.openCastSettings() }
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ScreenShare,
                                        contentDescription = null,
                                        tint = Color(0xFF007AFF),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Screen Mirroring",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "Open Cast settings",
                                            fontSize = 10.sp,
                                            color = Color(0xFF8E8E93)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .liquidGlassSurface(
                                            shape = RoundedCornerShape(16.dp),
                                            backgroundColor = Color(0xE8FFFFFF),
                                            elevation = 5.dp,
                                            borderWidth = 1.1.dp
                                        )
                                        .clickable { controlsManager.openNightLightSettings() }
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Nightlight,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9500),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Night Light",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "Display settings",
                                            fontSize = 10.sp,
                                            color = Color(0xFF8E8E93)
                                        )
                                    }
                                }
                            }

                            // Row 4: Bottom 4 quick tool buttons (Flashlight, Timer, Calculator, Camera)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                ControlCircleToggle(
                                    icon = Icons.Default.FlashlightOn,
                                    isActive = isFlashlightOn,
                                    activeColor = Color.White,
                                    onClick = {
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.CAMERA
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            controlsManager.toggleFlashlight()
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    tag = "cc_flashlight"
                                )
                                ControlCircleToggle(
                                    icon = Icons.Default.Timer,
                                    isActive = false,
                                    activeColor = Color(0xFFFF9500),
                                    onClick = { controlsManager.openTimer() },
                                    tag = "cc_timer"
                                )
                                ControlCircleToggle(
                                    icon = Icons.Default.Calculate,
                                    isActive = false,
                                    activeColor = Color(0xFFFF9500),
                                    onClick = { controlsManager.openCalculator() },
                                    tag = "cc_calculator"
                                )
                                ControlCircleToggle(
                                    icon = Icons.Default.CameraAlt,
                                    isActive = false,
                                    activeColor = Color(0xFF007AFF),
                                    onClick = { controlsManager.openCamera() },
                                    tag = "cc_camera"
                                )
                            }
                        }
                    } else {
                        // Page 2: iOS 10 Now Playing Card in Liquid Glass
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .liquidGlassSurface(
                                    shape = RoundedCornerShape(22.dp),
                                    backgroundColor = Color(0xF2FFFFFF),
                                    elevation = 8.dp,
                                    borderWidth = 1.3.dp
                                )
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Album Artwork & Track Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .liquidGlassSurface(
                                            shape = RoundedCornerShape(10.dp),
                                            backgroundColor = Color(0xFFE5E5EA),
                                            elevation = 3.dp,
                                            borderWidth = 1.dp
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = Color(0xFFFF2D55),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isPlayingMusic) "Now Playing" else "Music",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "iPhone Audio Stream",
                                        fontSize = 13.sp,
                                        color = Color(0xFF8E8E93)
                                    )
                                }
                            }

                            // Playback Scrubber (Timeline)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Slider(
                                    value = 0.42f,
                                    onValueChange = {},
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.Black,
                                        activeTrackColor = Color.Black,
                                        inactiveTrackColor = Color(0xFFD1D1D6)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "1:24", fontSize = 11.sp, color = Color(0xFF8E8E93))
                                    Text(text = "-2:16", fontSize = 11.sp, color = Color(0xFF8E8E93))
                                }
                            }

                            // Transport Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable { controlsManager.sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS) }
                                )
                                Icon(
                                    imageVector = if (isPlayingMusic) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clickable { controlsManager.sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) }
                                        .testTag("cc_media_play_pause")
                                )
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable { controlsManager.sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT) }
                                )
                            }

                            // Volume Slider with Liquid Glass Trough
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlassSurface(
                                        shape = RoundedCornerShape(14.dp),
                                        backgroundColor = Color(0xE8F5F5FA),
                                        elevation = 3.dp,
                                        borderWidth = 1.dp
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeDown,
                                    contentDescription = "Volume Down",
                                    tint = Color(0xFF8E8E93),
                                    modifier = Modifier.size(18.dp)
                                )
                                Slider(
                                    value = volumeLevel,
                                    onValueChange = { controlsManager.setVolume(it) },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color(0xFF007AFF),
                                        inactiveTrackColor = Color(0xFFE5E5EA)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                        .testTag("cc_volume_slider")
                                )
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Volume Up",
                                    tint = Color(0xFF8E8E93),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Audio Output Selector (iPhone Speaker) with Liquid Glass Pill
                            Row(
                                modifier = Modifier
                                    .liquidGlassSurface(
                                        shape = RoundedCornerShape(10.dp),
                                        backgroundColor = Color(0xFFEAEAF0),
                                        elevation = 2.dp,
                                        borderWidth = 1.dp
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speaker,
                                    contentDescription = null,
                                    tint = Color(0xFF007AFF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "iPhone Speaker",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF007AFF)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Page Indicator Dots for Control Center (Card 1 & Card 2)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (pagerState.currentPage == 0) Color.Black else Color(0xFFC7C7CC))
                            .clickable { scope.launch { pagerState.animateScrollToPage(0) } }
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (pagerState.currentPage == 1) Color.Black else Color(0xFFC7C7CC))
                            .clickable { scope.launch { pagerState.animateScrollToPage(1) } }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Authentic iOS 10 Liquid Glass circular toggle button with optical bevel,
 * top specular reflection, and vibrant radial glow when active.
 */
@Composable
fun ControlCircleToggle(
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    val transition = rememberInfiniteTransition(label = "activePulse")
    val glowAlpha by if (isActive && activeColor != Color.White) {
        transition.animateFloat(
            initialValue = 0.45f,
            targetValue = 0.75f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )
    } else {
        remember { mutableFloatStateOf(0.4f) }
    }

    Box(
        modifier = Modifier
            .size(54.dp)
            .shadow(
                elevation = if (isActive) 8.dp else 2.dp,
                shape = CircleShape,
                ambientColor = if (isActive) activeColor.copy(alpha = glowAlpha) else Color(0x1A000000),
                spotColor = if (isActive) activeColor else Color(0x33000000)
            )
            .clip(CircleShape)
            .background(
                if (isActive) {
                    if (activeColor == Color.White) {
                        // Flashlight bright spotlight white
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFFFFBEA),
                                Color(0xFFFFFAEB)
                            )
                        )
                    } else {
                        // Active colored liquid gradient
                        Brush.verticalGradient(
                            colors = listOf(
                                activeColor.copy(alpha = 0.95f),
                                activeColor,
                                activeColor.copy(alpha = 0.85f)
                            )
                        )
                    }
                } else {
                    // Translucent frosted Liquid Glass inactive circle
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF0FFFFFF),
                            Color(0xDCF0F0F5),
                            Color(0xC8E5E5EA)
                        )
                    )
                }
            )
            .border(
                width = 1.3.dp,
                brush = Brush.verticalGradient(
                    colors = if (isActive) {
                        listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    } else {
                        listOf(
                            Color(0xE6FFFFFF),
                            Color(0x80FFFFFF),
                            Color(0x24000000)
                        )
                    }
                ),
                shape = CircleShape
            )
            .clickable { onClick() }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        // Specular optical highlight crescent along the upper curve of the glass sphere
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color.White.copy(alpha = if (isActive) 0.55f else 0.80f),
                startAngle = 205f,
                sweepAngle = 130f,
                useCenter = false,
                style = Stroke(width = 1.6.dp.toPx()),
                topLeft = Offset(2.2.dp.toPx(), 2.2.dp.toPx()),
                size = Size(size.width - 4.4.dp.toPx(), size.height - 4.4.dp.toPx())
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) {
                if (activeColor == Color.White) Color.Black else Color.White
            } else {
                Color.Black.copy(alpha = 0.85f)
            },
            modifier = Modifier.size(24.dp)
        )
    }
}
