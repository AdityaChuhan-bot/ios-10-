package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.model.SystemIconType
import com.example.ui.theme.liquidGlassSurface
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IOSAppIcon(
    app: AppItem,
    iconBitmap: ImageBitmap?,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
    secondAngle: Float = 0f,
    minuteAngle: Float = 0f,
    hourAngle: Float = 0f,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    onPositioned: (androidx.compose.ui.geometry.Rect) -> Unit = {},
    onDragStart: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {}
) {
    // Jiggle animation with phase variation based on hashcode
    val infiniteTransition = rememberInfiniteTransition(label = "jiggle")
    val rotation by if (isEditMode) {
        val hashMod = (app.id.hashCode() % 50).toFloat()
        infiniteTransition.animateFloat(
            initialValue = -2.2f,
            targetValue = 2.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 140 + hashMod.toInt(), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "jiggleRotation"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Column(
        modifier = modifier
            .width(76.dp)
            .rotate(rotation)
            .onGloballyPositioned { onPositioned(it.boundsInRoot()) }
            .then(
                if (isEditMode) {
                    Modifier.pointerInput(app.id, isEditMode) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart() },
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragCancel,
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount)
                            }
                        )
                    }
                } else Modifier
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("app_icon_${app.packageName.ifEmpty { app.id }}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            // Squircle Icon Container with Liquid Glass optics
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(
                        elevation = 3.5.dp,
                        shape = RoundedCornerShape(13.5.dp),
                        ambientColor = Color(0x33000000),
                        spotColor = Color(0x4D000000)
                    )
                    .clip(RoundedCornerShape(13.5.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (app.isFolder) {
                    FolderIconPreview(apps = app.folderApps)
                } else {
                    SystemOrCustomIcon(
                        type = app.systemIconType,
                        iconBitmap = iconBitmap,
                        secondAngle = secondAngle,
                        minuteAngle = minuteAngle,
                        hourAngle = hourAngle
                    )
                }

                // Liquid Glass optical gloss overlay: curved specular lens reflection
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val glossPath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width, size.height * 0.44f)
                        quadraticTo(
                            size.width * 0.5f,
                            size.height * 0.54f,
                            0f,
                            size.height * 0.44f
                        )
                        close()
                    }
                    drawPath(
                        path = glossPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.38f),
                                Color.White.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
                }

                // Liquid Glass specular bevel rim border
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 0.9.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.65f),
                                    Color.White.copy(alpha = 0.20f),
                                    Color.Black.copy(alpha = 0.15f)
                                )
                            ),
                            shape = RoundedCornerShape(13.5.dp)
                        )
                )
            }

            // Top-right notification badge (e.g. 1, 3, etc.)
            if (!isEditMode && app.badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-4).dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                        .testTag("app_badge_${app.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (app.badgeCount > 99) "99+" else "${app.badgeCount}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Top-left remove badge in edit mode
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.TopStart)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8E8E93))
                        .combinedClickable(onClick = onDeleteClick)
                        .testTag("delete_app_badge_${app.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // App Label beneath with text shadow
        Text(
            text = app.label,
            style = TextStyle(
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.85f),
                    offset = Offset(0f, 2f),
                    blurRadius = 3f
                )
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(72.dp)
                .testTag("app_label_${app.label}")
        )
    }
}

@Composable
fun SystemOrCustomIcon(
    type: SystemIconType,
    iconBitmap: ImageBitmap?,
    secondAngle: Float = 0f,
    minuteAngle: Float = 0f,
    hourAngle: Float = 0f
) {
    when (type) {
        SystemIconType.PHONE -> {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF5CE777), Color(0xFF38C154))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        SystemIconType.MESSAGES -> {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF5CE777), Color(0xFF38C154))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sms,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        SystemIconType.SAFARI -> {
            // Authentic iOS 10 Safari Compass
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(50.dp)) {
                    val radius = size.width / 2
                    val center = Offset(radius, radius)

                    // Outer dial tick marks
                    for (i in 0 until 72) {
                        val angle = Math.toRadians(i * 5.0)
                        val isMajor = i % 6 == 0
                        val tickLen = if (isMajor) 4.dp.toPx() else 2.dp.toPx()
                        val color = if (isMajor) Color(0xFF007AFF) else Color(0xFF8E8E93).copy(alpha = 0.5f)
                        val start = Offset(
                            (center.x + (radius - tickLen) * Math.cos(angle)).toFloat(),
                            (center.y + (radius - tickLen) * Math.sin(angle)).toFloat()
                        )
                        val end = Offset(
                            (center.x + (radius - 1.dp.toPx()) * Math.cos(angle)).toFloat(),
                            (center.y + (radius - 1.dp.toPx()) * Math.sin(angle)).toFloat()
                        )
                        drawLine(color = color, start = start, end = end, strokeWidth = 1.dp.toPx())
                    }

                    // Rotating compass needle (tipped 45 degrees)
                    val needleAngle = Math.toRadians(45.0)
                    val redTip = Offset(
                        (center.x + (radius - 8.dp.toPx()) * Math.cos(needleAngle)).toFloat(),
                        (center.y - (radius - 8.dp.toPx()) * Math.sin(needleAngle)).toFloat()
                    )
                    val blueTip = Offset(
                        (center.x - (radius - 8.dp.toPx()) * Math.cos(needleAngle)).toFloat(),
                        (center.y + (radius - 8.dp.toPx()) * Math.sin(needleAngle)).toFloat()
                    )
                    val sideOffset = 4.dp.toPx()
                    val perpAngle = needleAngle + Math.PI / 2
                    val leftMid = Offset(
                        (center.x + sideOffset * Math.cos(perpAngle)).toFloat(),
                        (center.y - sideOffset * Math.sin(perpAngle)).toFloat()
                    )
                    val rightMid = Offset(
                        (center.x - sideOffset * Math.cos(perpAngle)).toFloat(),
                        (center.y + sideOffset * Math.sin(perpAngle)).toFloat()
                    )

                    // Red half
                    val redPath = Path().apply {
                        moveTo(redTip.x, redTip.y)
                        lineTo(leftMid.x, leftMid.y)
                        lineTo(center.x, center.y)
                        close()
                    }
                    drawPath(redPath, Color(0xFFFF3B30))

                    val redPath2 = Path().apply {
                        moveTo(redTip.x, redTip.y)
                        lineTo(rightMid.x, rightMid.y)
                        lineTo(center.x, center.y)
                        close()
                    }
                    drawPath(redPath2, Color(0xFFFF6961))

                    // Blue/White half
                    val bluePath = Path().apply {
                        moveTo(blueTip.x, blueTip.y)
                        lineTo(leftMid.x, leftMid.y)
                        lineTo(center.x, center.y)
                        close()
                    }
                    drawPath(bluePath, Color(0xFFE5E5EA))

                    val bluePath2 = Path().apply {
                        moveTo(blueTip.x, blueTip.y)
                        lineTo(rightMid.x, rightMid.y)
                        lineTo(center.x, center.y)
                        close()
                    }
                    drawPath(bluePath2, Color(0xFFD1D1D6))

                    // Center pivot
                    drawCircle(Color.White, radius = 3.dp.toPx(), center = center)
                    drawCircle(Color(0xFF8E8E93), radius = 1.5.dp.toPx(), center = center)
                }
            }
        }
        SystemIconType.CAMERA -> {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFE5E5EA), Color(0xFFC7C7CC))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color(0xFF3A3A3C),
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        SystemIconType.PHOTOS -> {
            // Authentic iOS 10 Photos Flower Spectrum
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(46.dp)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val petalColors = listOf(
                        Color(0xFFFF2D55), // Magenta
                        Color(0xFFFF3B30), // Red
                        Color(0xFFFF9500), // Orange
                        Color(0xFFFFCC00), // Yellow
                        Color(0xFF4CD964), // Green
                        Color(0xFF5AC8FA), // Cyan
                        Color(0xFF007AFF), // Blue
                        Color(0xFF5856D6)  // Purple
                    )
                    val petalLength = 14.dp.toPx()
                    val petalWidth = 6.dp.toPx()

                    for (i in petalColors.indices) {
                        val angle = (i * 45.0)
                        val rad = Math.toRadians(angle)
                        val offsetDist = 10.dp.toPx()
                        val petalCenter = Offset(
                            (center.x + offsetDist * Math.cos(rad)).toFloat(),
                            (center.y + offsetDist * Math.sin(rad)).toFloat()
                        )
                        drawRoundRect(
                            color = petalColors[i].copy(alpha = 0.85f),
                            topLeft = Offset(petalCenter.x - petalWidth / 2, petalCenter.y - petalLength / 2),
                            size = Size(petalWidth, petalLength),
                            cornerRadius = CornerRadius(petalWidth / 2, petalWidth / 2)
                        )
                    }
                }
            }
        }
        SystemIconType.SETTINGS -> {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF8E8E93), Color(0xFF636366))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(0xFFF2F2F7),
                    modifier = Modifier.size(38.dp)
                )
            }
        }
        SystemIconType.MUSIC -> {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFF3366), Color(0xFFFF2D55))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        SystemIconType.MAIL -> {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1D82F5), Color(0xFF007AFF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        SystemIconType.CALCULATOR -> {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF3A3A3C), Color(0xFF1C1C1E))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = Color(0xFFFF9500),
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        SystemIconType.CLOCK -> {
            // Live clock with real-time ticking hands
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(46.dp)) {
                    val radius = size.width / 2
                    val center = Offset(radius, radius)
                    // Dial tick marks
                    for (i in 0 until 12) {
                        val angle = Math.toRadians(i * 30.0)
                        val startX = (center.x + (radius - 4.dp.toPx()) * Math.sin(angle)).toFloat()
                        val startY = (center.y - (radius - 4.dp.toPx()) * Math.cos(angle)).toFloat()
                        val endX = (center.x + (radius - 1.dp.toPx()) * Math.sin(angle)).toFloat()
                        val endY = (center.y - (radius - 1.dp.toPx()) * Math.cos(angle)).toFloat()
                        drawLine(
                            color = Color.White.copy(alpha = 0.8f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }

                    // Live Hour hand
                    val hrRad = Math.toRadians(hourAngle.toDouble() - 90.0)
                    val hrLen = radius * 0.55f
                    drawLine(
                        color = Color.White,
                        start = center,
                        end = Offset((center.x + hrLen * Math.cos(hrRad)).toFloat(), (center.y + hrLen * Math.sin(hrRad)).toFloat()),
                        strokeWidth = 2.2.dp.toPx()
                    )

                    // Live Minute hand
                    val minRad = Math.toRadians(minuteAngle.toDouble() - 90.0)
                    val minLen = radius * 0.78f
                    drawLine(
                        color = Color.White,
                        start = center,
                        end = Offset((center.x + minLen * Math.cos(minRad)).toFloat(), (center.y + minLen * Math.sin(minRad)).toFloat()),
                        strokeWidth = 1.6.dp.toPx()
                    )

                    // Live Orange Second hand
                    val secRad = Math.toRadians(secondAngle.toDouble() - 90.0)
                    val secLen = radius * 0.85f
                    drawLine(
                        color = Color(0xFFFF9500),
                        start = center,
                        end = Offset((center.x + secLen * Math.cos(secRad)).toFloat(), (center.y + secLen * Math.sin(secRad)).toFloat()),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawCircle(color = Color(0xFFFF9500), radius = 2.dp.toPx(), center = center)
                }
            }
        }
        SystemIconType.CALENDAR -> {
            val cal = Calendar.getInstance()
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH).toString()
            val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "SUN"
                Calendar.MONDAY -> "MON"
                Calendar.TUESDAY -> "TUE"
                Calendar.WEDNESDAY -> "WED"
                Calendar.THURSDAY -> "THU"
                Calendar.FRIDAY -> "FRI"
                Calendar.SATURDAY -> "SAT"
                else -> "CAL"
            }
            Column(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(Color(0xFFFF3B30)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayOfWeek,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayOfMonth,
                        color = Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
        SystemIconType.MAPS -> {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFEAF5DF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = Color(0xFF007AFF),
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        SystemIconType.WEATHER -> {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF5AC8FA), Color(0xFF1D82F5))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        else -> {
            // Android application icon framed in clean white/frosted squircle
            if (iconBitmap != null) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = null,
                        modifier = Modifier.size(46.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFF007AFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FolderIconPreview(apps: List<AppItem>) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .liquidGlassSurface(
                shape = RoundedCornerShape(13.5.dp),
                backgroundColor = Color(0xAAE8E8F0),
                elevation = 2.dp,
                borderWidth = 1.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(7.dp),
            verticalArrangement = Arrangement.spacedBy(3.5.dp)
        ) {
            val previewApps = apps.take(9)
            val rows = previewApps.chunked(3)
            for (row in rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.5.dp)) {
                    for (miniApp in row) {
                        Box(
                            modifier = Modifier
                                .size(12.5.dp)
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(3.dp))
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF5AC8FA), Color(0xFF007AFF))
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
