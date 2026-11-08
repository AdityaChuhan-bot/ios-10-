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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.ui.theme.LiquidGlassSheenEffect
import com.example.ui.theme.liquidGlassSurface

@Composable
fun IOSFolderDialog(
    folder: AppItem,
    getIconBitmap: (String) -> ImageBitmap?,
    onAppClick: (AppItem) -> Unit,
    onRenameFolder: (String, String) -> Unit,
    onRemoveFolder: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var folderName by remember(folder.id) { mutableStateOf(folder.label) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
            .testTag("folder_dialog_scrim"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume clicks inside card */ },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Folder title - editable
            BasicTextField(
                value = folderName,
                onValueChange = {
                    folderName = it
                    onRenameFolder(folder.id, it)
                },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(Color.White),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("folder_title_input")
            )

            // Liquid glass square 3x3 folder container
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .liquidGlassSurface(
                        shape = RoundedCornerShape(32.dp),
                        backgroundColor = Color(0xD8F8F8FD),
                        elevation = 20.dp,
                        borderWidth = 1.3.dp
                    )
                    .padding(16.dp)
                    .testTag("folder_apps_container"),
                contentAlignment = Alignment.Center
            ) {
                // Liquid Glass caustics sheen
                LiquidGlassSheenEffect(alpha = 0.10f)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(folder.folderApps) { app ->
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

            Spacer(modifier = Modifier.height(20.dp))

            // Delete folder button in Liquid Glass
            Row(
                modifier = Modifier
                    .liquidGlassSurface(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = Color(0x38000000),
                        elevation = 4.dp,
                        borderWidth = 0.9.dp,
                        isDark = true
                    )
                    .clickable { onRemoveFolder(folder.id) }
                    .padding(horizontal = 18.dp, vertical = 9.dp)
                    .testTag("remove_folder_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Folder",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Ungroup Folder",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
