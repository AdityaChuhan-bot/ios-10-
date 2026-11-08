package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.system.SystemStatusMonitor
import com.example.ui.theme.liquidGlassSurface

@Composable
fun IOSTodayWidgetsView(
    statusMonitor: SystemStatusMonitor,
    recentApps: List<AppItem>,
    getIconBitmap: (String) -> ImageBitmap?,
    onAppClick: (AppItem) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDate by statusMonitor.currentDate.collectAsState()
    val batteryLevel by statusMonitor.batteryLevel.collectAsState()
    val isCharging by statusMonitor.isCharging.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("today_widgets_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Spotlight Search Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .liquidGlassSurface(
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = Color(0x66FFFFFF),
                    elevation = 4.dp,
                    borderWidth = 1.dp
                )
                .clickable { onSearchClick() }
                .padding(horizontal = 12.dp)
                .testTag("today_search_bar"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Search",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }

        // Widget 1: Calendar / Up Next
        TodayWidgetCard(title = "UP NEXT", icon = Icons.Default.CalendarToday, iconColor = Color(0xFFFF3B30)) {
            Column {
                Text(
                    text = "No more events today",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = currentDate,
                    fontSize = 13.sp,
                    color = Color(0xFF8E8E93)
                )
            }
        }

        // Widget 2: Weather
        TodayWidgetCard(title = "WEATHER", icon = Icons.Default.WbSunny, iconColor = Color(0xFFFF9500)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Location",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = "Mostly Sunny",
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93)
                    )
                    Text(
                        text = "H: 75°  L: 56°",
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                }

                Text(
                    text = "72°",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.Black
                )
            }
        }

        // Widget 3: Batteries
        TodayWidgetCard(title = "BATTERIES", icon = Icons.Default.PhoneIphone, iconColor = Color(0xFF4CD964)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // iPhone battery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneIphone,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "iPhone",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$batteryLevel%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCharging) Color(0xFF34C759) else Color.Black
                        )
                        Box(modifier = Modifier.size(width = 30.dp, height = 14.dp)) {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                drawRoundRect(
                                    color = Color(0xFFC7C7CC),
                                    size = Size(size.width, size.height),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                )
                                val fillWidth = (size.width * (batteryLevel / 100f)).coerceAtLeast(2f)
                                drawRoundRect(
                                    color = if (isCharging) Color(0xFF34C759) else Color(0xFF007AFF),
                                    size = Size(fillWidth, size.height),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                )
                            }
                        }
                    }
                }

                // Apple Watch / Bluetooth accessory
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Watch,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Apple Watch",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "85%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Box(modifier = Modifier.size(width = 30.dp, height = 14.dp)) {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                drawRoundRect(
                                    color = Color(0xFFC7C7CC),
                                    size = Size(size.width, size.height),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                )
                                drawRoundRect(
                                    color = Color(0xFF34C759),
                                    size = Size(size.width * 0.85f, size.height),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }
        }

        // Widget 4: Siri App Suggestions
        if (recentApps.isNotEmpty()) {
            TodayWidgetCard(title = "SIRI APP SUGGESTIONS", icon = Icons.Default.Search, iconColor = Color(0xFF5856D6)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (app in recentApps.take(4)) {
                        IOSAppIcon(
                            app = app,
                            iconBitmap = getIconBitmap(app.packageName),
                            isEditMode = false,
                            onClick = { onAppClick(app) },
                            onLongClick = {}
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "Edit" pill button at bottom
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.35f))
                .clickable { onSearchClick() }
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("today_edit_widgets_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Edit",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TodayWidgetCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassSurface(
                shape = RoundedCornerShape(18.dp),
                backgroundColor = Color(0xF0FFFFFF),
                elevation = 8.dp,
                borderWidth = 1.2.dp
            )
            .padding(14.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF636366),
                letterSpacing = 0.4.sp
            )
        }

        content()
    }
}
