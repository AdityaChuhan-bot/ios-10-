package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.ui.theme.LiquidGlassSheenEffect
import com.example.ui.theme.liquidGlassSurface

@Composable
fun AppSwitcherSheet(
    recentApps: List<AppItem>,
    getIconBitmap: (String) -> ImageBitmap?,
    onAppClick: (AppItem) -> Unit,
    onDismissApp: (AppItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(top = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
            .testTag("app_switcher_sheet"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Recent Applications",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (recentApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .height(260.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent applications",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 48.dp)
                ) {
                    items(recentApps, key = { it.id }) { app ->
                        AppSwitcherCard(
                            app = app,
                            iconBitmap = getIconBitmap(app.packageName),
                            onClick = { onAppClick(app) },
                            onDismiss = { onDismissApp(app) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppSwitcherCard(
    app: AppItem,
    iconBitmap: ImageBitmap?,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() }
            .testTag("app_switcher_card_${app.id}")
    ) {
        // App Header: Icon + Name + Dismiss 'x'
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(24.dp)) {
                    SystemOrCustomIcon(type = app.systemIconType, iconBitmap = iconBitmap)
                }
                Text(
                    text = app.label,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .clickable { onDismiss() }
                    .testTag("dismiss_recent_${app.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // App Card Body
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(300.dp)
                .liquidGlassSurface(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0xF2FDFDFF),
                    elevation = 12.dp,
                    borderWidth = 1.2.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            // Liquid Glass optical sheen
            LiquidGlassSheenEffect(alpha = 0.08f)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(60.dp)) {
                    SystemOrCustomIcon(type = app.systemIconType, iconBitmap = iconBitmap)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = app.label,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Tap to open",
                    color = Color(0xFF8E8E93),
                    fontSize = 11.sp
                )
            }
        }
    }
}
