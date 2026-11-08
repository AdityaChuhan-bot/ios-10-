package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.liquidGlassSurface
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun AssistiveTouchView(
    onHomeClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onControlCenterClick: () -> Unit,
    onRecentsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLockClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(20f) }
    var offsetY by remember { mutableFloatStateOf(450f) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    Box(modifier = modifier.fillMaxSize()) {
        // Expanded Radial Menu Modal
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isExpanded = false }
                    .testTag("assistive_touch_menu_scrim"),
                contentAlignment = Alignment.Center
            ) {
                // Dark liquid glass rounded container
                Box(
                    modifier = Modifier
                        .size(264.dp)
                        .liquidGlassSurface(
                            shape = RoundedCornerShape(34.dp),
                            elevation = 16.dp,
                            borderWidth = 1.2.dp,
                            isDark = true
                        )
                        .padding(16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Consume */ }
                        .testTag("assistive_touch_menu_card")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top row: Notifications
                        AssistiveMenuItem(
                            icon = Icons.Default.Notifications,
                            label = "Notifications",
                            onClick = {
                                isExpanded = false
                                onNotificationsClick()
                            }
                        )

                        // Middle row: Device, Control Center, Recents
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistiveMenuItem(
                                icon = Icons.Default.PhoneIphone,
                                label = "Device",
                                onClick = {
                                    isExpanded = false
                                    onControlCenterClick()
                                }
                            )

                            AssistiveMenuItem(
                                icon = Icons.Default.Tune,
                                label = "Control Center",
                                onClick = {
                                    isExpanded = false
                                    onControlCenterClick()
                                }
                            )

                            AssistiveMenuItem(
                                icon = Icons.Default.ViewCarousel,
                                label = "Recent Apps",
                                onClick = {
                                    isExpanded = false
                                    onRecentsClick()
                                }
                            )
                        }

                        // Bottom row: Home, Lock, Settings
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistiveMenuItem(
                                icon = Icons.Default.Lock,
                                label = "Lock",
                                onClick = {
                                    isExpanded = false
                                    onLockClick()
                                }
                            )
                            AssistiveMenuItem(
                                icon = Icons.Default.Home,
                                label = "Home",
                                onClick = {
                                    isExpanded = false
                                    onHomeClick()
                                }
                            )
                            AssistiveMenuItem(
                                icon = Icons.Default.Settings,
                                label = "Settings",
                                onClick = {
                                    isExpanded = false
                                    onSettingsClick()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Draggable Button
        if (!isExpanded) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(56.dp)
                    .liquidGlassSurface(
                        shape = RoundedCornerShape(16.dp),
                        elevation = 8.dp,
                        borderWidth = 1.2.dp,
                        isDark = true
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                // Snap to nearest edge (left or right)
                                val snapMargin = 20f
                                offsetX = if (offsetX < screenWidthPx / 2) {
                                    snapMargin
                                } else {
                                    screenWidthPx - 54.dp.toPx() - snapMargin
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount.x).coerceIn(10f, screenWidthPx - 64.dp.toPx())
                            offsetY = (offsetY + dragAmount.y).coerceIn(60f, screenHeightPx - 100.dp.toPx())
                        }
                    }
                    .clickable { isExpanded = true }
                    .testTag("assistive_touch_floating_button"),
                contentAlignment = Alignment.Center
            ) {
                // Classic iOS AssistiveTouch Concentric Ring Icon
                Canvas(modifier = Modifier.size(34.dp)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    // Outer ring
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = size.width / 2 - 1.dp.toPx(),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    // Middle ring
                    drawCircle(
                        color = Color.White.copy(alpha = 0.7f),
                        radius = size.width / 3,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // Center solid dot
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx()
                    )
                }
            }
        }
    }
}

@Composable
fun AssistiveMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("assistive_item_$label")
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
