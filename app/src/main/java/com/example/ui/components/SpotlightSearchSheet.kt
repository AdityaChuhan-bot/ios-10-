package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.ui.theme.liquidGlassSurface

@Composable
fun SpotlightSearchSheet(
    query: String,
    onQueryChange: (String) -> Unit,
    allApps: List<AppItem>,
    recentApps: List<AppItem>,
    getIconBitmap: (String) -> ImageBitmap?,
    onAppClick: (AppItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredApps = remember(query, allApps) {
        if (query.isBlank()) {
            emptyList()
        } else {
            allApps.filter {
                it.label.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("spotlight_search_sheet")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Liquid Glass Search Field
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .liquidGlassSurface(
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = Color(0x55FFFFFF),
                            elevation = 4.dp,
                            borderWidth = 1.dp
                        )
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Spotlight Search",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("spotlight_search_input")
                        )
                    }

                    if (query.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.4f))
                                .clickable { onQueryChange("") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.Black,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }

                // Cancel Button
                Text(
                    text = "Cancel",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .testTag("spotlight_cancel_button")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (query.isBlank()) {
                // Siri App Suggestions / Recent Apps
                if (recentApps.isNotEmpty()) {
                    Text(
                        text = "SIRI APP SUGGESTIONS",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassSurface(
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = Color(0xF2FFFFFF),
                                elevation = 8.dp,
                                borderWidth = 1.2.dp
                            )
                            .padding(vertical = 12.dp, horizontal = 8.dp),
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
            } else {
                // Search Results
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassSurface(
                            shape = RoundedCornerShape(18.dp),
                            backgroundColor = Color(0xF2FFFFFF),
                            elevation = 8.dp,
                            borderWidth = 1.2.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(filteredApps) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppClick(app) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .testTag("search_result_${app.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.size(40.dp)) {
                                SystemOrCustomIcon(
                                    type = app.systemIconType,
                                    iconBitmap = getIconBitmap(app.packageName)
                                )
                            }
                            Column {
                                Text(
                                    text = app.label,
                                    color = Color.Black,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = app.packageName,
                                    color = Color(0xFF8E8E93),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
