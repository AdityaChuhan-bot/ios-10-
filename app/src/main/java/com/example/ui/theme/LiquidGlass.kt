package com.example.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Core Liquid Glass tokens with optical refraction values
val LiquidGlassHighlight = Color(0xB3FFFFFF)
val LiquidGlassMid = Color(0x40FFFFFF)
val LiquidGlassShadow = Color(0x1A000000)

val LiquidGlassDockLight = Color(0xD0FAFAFD)
val LiquidGlassCardLight = Color(0xE8FFFFFF)
val LiquidGlassControlCenter = Color(0xEBF0F2F6)

/**
 * Applies authentic Liquid Glass surface optics:
 * 1. Deep ambient and contact shadow.
 * 2. Specular top-down refraction gradient simulating light passing through curved liquid glass.
 * 3. Prismatic specular bevel rim highlight along the edges.
 */
fun Modifier.liquidGlassSurface(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = LiquidGlassCardLight,
    elevation: Dp = 12.dp,
    borderWidth: Dp = 1.2.dp,
    isDark: Boolean = false
): Modifier {
    val topBorderColor = if (isDark) Color(0x66FFFFFF) else Color(0xB8FFFFFF)
    val midBorderColor = if (isDark) Color(0x1AFFFFFF) else Color(0x40FFFFFF)
    val bottomBorderColor = if (isDark) Color(0x0AFFFFFF) else Color(0x1F000000)

    val topBg = if (isDark) Color(0xE62C2C2E) else backgroundColor.copy(alpha = (backgroundColor.alpha * 1.05f).coerceAtMost(1f))
    val midBg = if (isDark) Color(0xD91C1C1E) else backgroundColor
    val btmBg = if (isDark) Color(0xCC141416) else backgroundColor.copy(alpha = (backgroundColor.alpha * 0.90f))

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color(0x33000000),
            spotColor = Color(0x4D000000)
        )
        .clip(shape)
        .background(
            Brush.verticalGradient(
                colors = listOf(topBg, midBg, btmBg)
            )
        )
        .border(
            width = borderWidth,
            brush = Brush.verticalGradient(
                colors = listOf(topBorderColor, midBorderColor, bottomBorderColor)
            ),
            shape = shape
        )
}

/**
 * Renders an animated fluid caustics / liquid sheen effect across the glass container
 */
@Composable
fun LiquidGlassSheenEffect(
    modifier: Modifier = Modifier,
    alpha: Float = 0.18f
) {
    val transition = rememberInfiniteTransition(label = "liquidSheen")
    val sheenProgress by transition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheenProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val startX = width * sheenProgress
        val startY = 0f
        val endX = startX + width * 0.4f
        val endY = height

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = alpha),
                    Color.White.copy(alpha = alpha * 1.4f),
                    Color.White.copy(alpha = alpha),
                    Color.Transparent
                ),
                start = Offset(startX, startY),
                end = Offset(endX, endY)
            )
        )
    }
}
