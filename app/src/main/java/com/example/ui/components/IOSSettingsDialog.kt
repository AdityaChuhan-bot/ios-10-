package com.example.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.R
import com.example.service.IOSNotificationListenerService
import com.example.ui.theme.liquidGlassSurface
import com.example.viewmodel.LauncherViewModel

@Composable
fun IOSSettingsDialog(
    viewModel: LauncherViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val is24Hour by viewModel.statusMonitor.is24Hour.collectAsState()
    val assistiveTouchEnabled by viewModel.assistiveTouchEnabled.collectAsState()
    val showIOSStatusBar by viewModel.showIOSStatusBar.collectAsState()
    val selectedWallpaper by viewModel.selectedWallpaperResId.collectAsState()
    var isNotificationAccessGranted by remember {
        mutableStateOf(IOSNotificationListenerService.isAccessGranted(context))
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isNotificationAccessGranted = IOSNotificationListenerService.isAccessGranted(context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(vertical = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
            .testTag("settings_dialog_scrim"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .liquidGlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color(0xF4F2F2F7),
                    elevation = 16.dp,
                    borderWidth = 1.2.dp
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume */ }
                .padding(18.dp)
                .testTag("settings_dialog_card")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE5E5EA))
                        .clickable { onDismiss() }
                        .testTag("close_settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Group 1: Launcher & Notifications
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                SettingsRowAction(
                    icon = Icons.Default.Home,
                    iconColor = Color(0xFF007AFF),
                    title = "Set as Default Home Launcher",
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            try {
                                val fallback = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(fallback)
                            } catch (_: Exception) {
                            }
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .padding(start = 50.dp)
                        .background(Color(0xFFE5E5EA))
                )

                SettingsRowAction(
                    icon = Icons.Default.Notifications,
                    iconColor = Color(0xFFFF3B30),
                    title = "Notification Center Access",
                    subtitle = if (isNotificationAccessGranted) "Allowed" else "Action Required",
                    onClick = {
                        IOSNotificationListenerService.openSettings(context)
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .padding(start = 50.dp)
                        .background(Color(0xFFE5E5EA))
                )

                SettingsRowAction(
                    icon = Icons.Default.Lock,
                    iconColor = Color(0xFF5856D6),
                    title = "Lock Screen",
                    subtitle = "Preview",
                    onClick = {
                        onDismiss()
                        viewModel.openOverlay(com.example.model.ActiveOverlay.LOCK_SCREEN)
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Settings Group 2: Preferences
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                SettingsRowToggle(
                    icon = Icons.Default.TouchApp,
                    iconColor = Color(0xFF34C759),
                    title = "AssistiveTouch",
                    checked = assistiveTouchEnabled,
                    onCheckedChange = { viewModel.toggleAssistiveTouch() }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .padding(start = 50.dp)
                        .background(Color(0xFFE5E5EA))
                )

                SettingsRowToggle(
                    icon = Icons.Default.Home,
                    iconColor = Color(0xFF007AFF),
                    title = "iOS 10 Status Bar",
                    checked = showIOSStatusBar,
                    onCheckedChange = { viewModel.setShowIOSStatusBar(it) }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .padding(start = 50.dp)
                        .background(Color(0xFFE5E5EA))
                )

                SettingsRowToggle(
                    icon = Icons.Default.Schedule,
                    iconColor = Color(0xFFFF9500),
                    title = "24-Hour Time Format",
                    checked = is24Hour,
                    onCheckedChange = { viewModel.statusMonitor.set24HourFormat(it) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wallpaper options
            Text(
                text = "WALLPAPER",
                color = Color(0xFF8E8E93),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Wave Wallpaper
                WallpaperCard(
                    title = "iOS 10 Wave",
                    isSelected = selectedWallpaper == R.drawable.wallpaper_ios10_wave,
                    color = Color(0xFF2294C6),
                    onClick = { viewModel.setWallpaper(R.drawable.wallpaper_ios10_wave) },
                    modifier = Modifier.weight(1f)
                )

                // Space Wallpaper
                WallpaperCard(
                    title = "iOS 10 Space",
                    isSelected = selectedWallpaper == R.drawable.wallpaper_ios10_space,
                    color = Color(0xFF16192B),
                    onClick = { viewModel.setWallpaper(R.drawable.wallpaper_ios10_space) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // System info footer
            Text(
                text = "iPhone 6 · iOS 10.3.3 Simulator · Android 11 Compatible",
                color = Color(0xFF8E8E93),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun SettingsRowAction(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
            Text(text = title, fontSize = 14.sp, color = Color.Black)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 13.sp, color = Color(0xFF8E8E93))
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFC7C7CC),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SettingsRowToggle(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
            Text(text = title, fontSize = 14.sp, color = Color.Black)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF34C759),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E5EA)
            )
        )
    }
}

@Composable
fun WallpaperCard(
    title: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 55.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Wallpaper,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Text(
                text = "Active",
                color = Color(0xFF5AC8FA),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
