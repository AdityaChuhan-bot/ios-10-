package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.SystemStatusMonitor

@Composable
fun IOS10StatusBar(
    statusMonitor: SystemStatusMonitor,
    modifier: Modifier = Modifier,
    onTapNotificationCenter: () -> Unit = {},
    onTapControlCenter: () -> Unit = {},
    onTapStatusBar: () -> Unit = onTapNotificationCenter
) {
    val currentTime by statusMonitor.currentTime.collectAsState()
    val carrierName by statusMonitor.carrierName.collectAsState()
    val cellularDots by statusMonitor.cellularDots.collectAsState()
    val wifiConnected by statusMonitor.wifiConnected.collectAsState()
    val wifiLevel by statusMonitor.wifiLevel.collectAsState()
    val batteryLevel by statusMonitor.batteryLevel.collectAsState()
    val isCharging by statusMonitor.isCharging.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp)
            .height(20.dp)
            .padding(horizontal = 8.dp)
            .testTag("ios_status_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: 5 signal dots, carrier label, wifi icon (tap opens Notification Center)
        Row(
            modifier = Modifier.clickable { onTapNotificationCenter() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Iconic iOS 10 5-dot signal indicator
            Canvas(modifier = Modifier.size(width = 30.dp, height = 8.dp)) {
                val dotRadius = 2.2.dp.toPx()
                val spacing = 6.dp.toPx()
                for (i in 0 until 5) {
                    val cx = i * spacing + dotRadius
                    val cy = size.height / 2
                    val isFilled = i < cellularDots
                    if (isFilled) {
                        drawCircle(
                            color = Color.White,
                            radius = dotRadius,
                            center = Offset(cx, cy)
                        )
                    } else {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f),
                            radius = dotRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }

            Text(
                text = carrierName,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )

            if (wifiConnected) {
                // iOS Wi-Fi arc icon
                Canvas(modifier = Modifier.size(13.dp)) {
                    val stroke = Stroke(width = 1.6.dp.toPx())
                    // 3 curved arcs
                    val alpha3 = if (wifiLevel >= 3) 1f else 0.35f
                    val alpha2 = if (wifiLevel >= 2) 1f else 0.35f
                    val alpha1 = if (wifiLevel >= 1) 1f else 0.35f

                    drawArc(
                        color = Color.White.copy(alpha = alpha3),
                        startAngle = 225f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = stroke,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height * 2)
                    )
                    drawArc(
                        color = Color.White.copy(alpha = alpha2),
                        startAngle = 225f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = stroke,
                        topLeft = Offset(size.width * 0.2f, size.height * 0.35f),
                        size = Size(size.width * 0.6f, size.height * 1.3f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha1),
                        radius = 1.4.dp.toPx(),
                        center = Offset(size.width / 2, size.height - 1.5.dp.toPx())
                    )
                }
            }
        }

        // Center: Time (tap opens Notification Center)
        Text(
            text = currentTime,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable { onTapNotificationCenter() }
                .testTag("status_bar_time")
        )

        // Right side: Bluetooth, Battery percentage & Battery icon (tap opens Control Center)
        Row(
            modifier = Modifier
                .clickable { onTapControlCenter() }
                .testTag("status_bar_control_center_trigger"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = "Bluetooth",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(12.dp)
            )

            Text(
                text = "$batteryLevel%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.testTag("status_bar_battery_percentage")
            )

            // iOS 10 Battery icon shell and fill
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(width = 22.dp, height = 11.dp)
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val stroke = Stroke(width = 1.dp.toPx())
                        // Outer rounded rect
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.6f),
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
                            style = stroke
                        )

                        // Inner level fill
                        val fillWidth = ((size.width - 4.dp.toPx()) * (batteryLevel / 100f)).coerceAtLeast(1f)
                        val fillColor = when {
                            batteryLevel <= 20 -> Color(0xFFFF3B30)
                            isCharging -> Color(0xFF4CD964)
                            else -> Color.White
                        }
                        drawRoundRect(
                            color = fillColor,
                            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                            size = Size(fillWidth, size.height - 4.dp.toPx()),
                            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                        )
                    }

                    if (isCharging) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Charging",
                            tint = Color.Black,
                            modifier = Modifier
                                .size(9.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                // Battery positive terminal nub
                Canvas(modifier = Modifier.size(width = 2.dp, height = 4.dp)) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.6f),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                    )
                }
            }
        }
    }
}
