package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.liquidGlassSurface

@Composable
fun IOSPageIndicator(
    pageCount: Int,
    currentPage: Int,
    hasTodayPage: Boolean = true,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 1) return

    Box(
        modifier = modifier
            .liquidGlassSurface(
                shape = RoundedCornerShape(12.dp),
                backgroundColor = Color(0x30000000),
                elevation = 2.dp,
                borderWidth = 0.8.dp,
                isDark = true
            )
            .padding(horizontal = 10.dp, vertical = 3.dp)
            .testTag("ios_page_indicator"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until pageCount) {
                val isCurrent = i == currentPage
                if (i == 0 && hasTodayPage) {
                    // Today View Search glyph dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clickable { onPageSelected(0) }
                            .testTag("page_dot_today"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Today",
                            tint = if (isCurrent) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(9.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(6.5.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) Color.White else Color.White.copy(alpha = 0.4f))
                            .clickable { onPageSelected(i) }
                            .testTag("page_dot_$i")
                    )
                }
            }
        }
    }
}
