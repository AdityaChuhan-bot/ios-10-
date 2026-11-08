package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AppItem
import com.example.ui.theme.LiquidGlassSheenEffect
import com.example.ui.theme.liquidGlassSurface

@Composable
fun IOSDock(
    dockApps: List<AppItem>,
    isEditMode: Boolean,
    getIconBitmap: (String) -> ImageBitmap?,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .navigationBarsPadding()
            .height(88.dp)
            .liquidGlassSurface(
                shape = RoundedCornerShape(26.dp),
                backgroundColor = Color(0xDDF2F4F8),
                elevation = 14.dp,
                borderWidth = 1.2.dp
            )
            .testTag("ios_dock"),
        contentAlignment = Alignment.Center
    ) {
        // Fluid caustics sheen
        LiquidGlassSheenEffect(alpha = 0.12f)

        // Top specular reflection highlight along the curve
        Canvas(modifier = Modifier.matchParentSize()) {
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(24.dp.toPx(), 1.5.dp.toPx()),
                end = Offset(size.width - 24.dp.toPx(), 1.5.dp.toPx()),
                strokeWidth = 1.2.dp.toPx()
            )
        }

        // 4 Dock apps
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (app in dockApps.take(4)) {
                IOSAppIcon(
                    app = app,
                    iconBitmap = getIconBitmap(app.packageName),
                    isEditMode = isEditMode,
                    onClick = { onAppClick(app) },
                    onLongClick = onAppLongClick
                )
            }
        }
    }
}
