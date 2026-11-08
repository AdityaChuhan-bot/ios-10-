package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveOverlay
import com.example.model.AppItem
import com.example.ui.components.AppSwitcherSheet
import com.example.ui.components.AssistiveTouchView
import com.example.ui.components.ControlCenterSheet
import com.example.ui.components.IOS10StatusBar
import com.example.ui.components.IOSAppIcon
import com.example.ui.components.IOSDock
import com.example.ui.components.IOSFolderDialog
import com.example.ui.components.IOSLockScreen
import com.example.ui.components.IOSPageIndicator
import com.example.ui.components.IOSSettingsDialog
import com.example.ui.components.IOSTodayWidgetsView
import com.example.ui.components.NotificationCenterSheet
import com.example.ui.components.SpotlightSearchSheet
import com.example.ui.theme.liquidGlassSurface
import com.example.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pages by viewModel.pages.collectAsState()
    val dockApps by viewModel.dockApps.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val activeOverlay by viewModel.activeOverlay.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val recentApps by viewModel.recentApps.collectAsState()
    val allInstalledApps by viewModel.installedApps.collectAsState()
    val assistiveTouchEnabled by viewModel.assistiveTouchEnabled.collectAsState()
    val showIOSStatusBar by viewModel.showIOSStatusBar.collectAsState()
    val selectedWallpaperResId by viewModel.selectedWallpaperResId.collectAsState()

    // Clock hand angles for real-time ticking clock icon
    val secondAngle by viewModel.statusMonitor.secondAngle.collectAsState()
    val minuteAngle by viewModel.statusMonitor.minuteAngle.collectAsState()
    val hourAngle by viewModel.statusMonitor.hourAngle.collectAsState()

    // Page 0 is Today View, Page 1..N are App Pages
    val appPageCount = if (pages.isEmpty()) 1 else pages.size
    val totalPageCount = 1 + appPageCount
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { totalPageCount })

    // Track selected app for folder grouping in edit mode
    var appToMerge by remember { mutableStateOf<AppItem?>(null) }

    // Synchronize currentPage
    LaunchedEffect(pagerState.currentPage) {
        viewModel.setCurrentPage((pagerState.currentPage - 1).coerceAtLeast(0))
    }

    // Android Hardware / Gesture Back Handling
    BackHandler(enabled = activeOverlay != ActiveOverlay.NONE || isEditMode || pagerState.currentPage != 1) {
        when {
            activeOverlay != ActiveOverlay.NONE -> viewModel.closeOverlay()
            isEditMode -> {
                viewModel.statusMonitor.performHapticClick()
                viewModel.setEditMode(false)
                appToMerge = null
            }
            pagerState.currentPage != 1 -> {
                scope.launch { pagerState.animateScrollToPage(1) }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_container")
    ) {
        // Wallpaper background
        Image(
            painter = painterResource(id = selectedWallpaperResId),
            contentDescription = "iOS 10 Wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Main Home Screen Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        if (activeOverlay == ActiveOverlay.NONE) {
                            if (delta > 35) {
                                // Swipe down opens Spotlight Search
                                viewModel.statusMonitor.performHapticClick()
                                viewModel.openOverlay(ActiveOverlay.SPOTLIGHT)
                            } else if (delta < -35) {
                                // Swipe up opens Control Center
                                viewModel.statusMonitor.performHapticClick()
                                viewModel.openOverlay(ActiveOverlay.CONTROL_CENTER)
                            }
                        }
                    }
                )
        ) {
            // iOS 10 Status Bar (single status bar at top)
            if (showIOSStatusBar) {
                IOS10StatusBar(
                    statusMonitor = viewModel.statusMonitor,
                    onTapNotificationCenter = {
                        viewModel.statusMonitor.performHapticClick()
                        viewModel.openOverlay(ActiveOverlay.NOTIFICATION_CENTER)
                    },
                    onTapControlCenter = {
                        viewModel.statusMonitor.performHapticClick()
                        viewModel.openOverlay(ActiveOverlay.CONTROL_CENTER)
                    }
                )
            }

            // Top action bar in edit mode (Done button) or settings icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings button with Liquid Glass styling
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .liquidGlassSurface(
                            shape = RoundedCornerShape(10.dp),
                            backgroundColor = Color(0x40000000),
                            elevation = 3.dp,
                            borderWidth = 0.9.dp,
                            isDark = true
                        )
                        .clickable {
                            viewModel.statusMonitor.performHapticClick()
                            viewModel.openOverlay(ActiveOverlay.SETTINGS)
                        }
                        .testTag("open_settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isEditMode) {
                    Box(
                        modifier = Modifier
                            .liquidGlassSurface(
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = Color(0xE6007AFF),
                                elevation = 6.dp,
                                borderWidth = 1.1.dp
                            )
                            .clickable {
                                viewModel.statusMonitor.performHapticClick()
                                viewModel.setEditMode(false)
                                appToMerge = null
                            }
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                            .testTag("done_edit_mode_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Done",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Pager: Page 0 is Today View; Page 1..N are App Grids
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("home_screen_pager")
            ) { pageIndex ->
                if (pageIndex == 0) {
                    // iOS 10 Today View (Widgets)
                    IOSTodayWidgetsView(
                        statusMonitor = viewModel.statusMonitor,
                        recentApps = recentApps,
                        getIconBitmap = { viewModel.getIconBitmap(it) },
                        onAppClick = { viewModel.launchApp(context, it) },
                        onSearchClick = { viewModel.openOverlay(ActiveOverlay.SPOTLIGHT) }
                    )
                } else {
                    val appPageIndex = pageIndex - 1
                    val pageApps = pages.getOrElse(appPageIndex) { emptyList() }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("app_grid_page_$appPageIndex")
                    ) {
                        items(pageApps, key = { it.id }) { app ->
                            IOSAppIcon(
                                app = app,
                                iconBitmap = viewModel.getIconBitmap(app.packageName),
                                isEditMode = isEditMode,
                                secondAngle = secondAngle,
                                minuteAngle = minuteAngle,
                                hourAngle = hourAngle,
                                onClick = {
                                    if (isEditMode) {
                                        viewModel.statusMonitor.performHapticClick()
                                        if (appToMerge == null) {
                                            appToMerge = app
                                        } else {
                                            viewModel.createFolder(appToMerge!!, app, appPageIndex)
                                            appToMerge = null
                                        }
                                    } else {
                                        viewModel.launchApp(context, app)
                                    }
                                },
                                onLongClick = {
                                    viewModel.statusMonitor.performHapticClick()
                                    viewModel.setEditMode(true)
                                },
                                onDeleteClick = {
                                    viewModel.statusMonitor.performHapticClick()
                                    viewModel.removeAppFromGrid(app)
                                }
                            )
                        }
                    }
                }
            }

            // Page Indicator Dots
            IOSPageIndicator(
                pageCount = totalPageCount,
                currentPage = pagerState.currentPage,
                hasTodayPage = true,
                onPageSelected = { target ->
                    scope.launch { pagerState.animateScrollToPage(target) }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Bottom Control Center Drag Handle Indicator in Liquid Glass
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 3.dp)
                    .liquidGlassSurface(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = Color(0x38000000),
                        elevation = 3.dp,
                        borderWidth = 0.8.dp,
                        isDark = true
                    )
                    .clickable {
                        viewModel.statusMonitor.performHapticClick()
                        viewModel.openOverlay(ActiveOverlay.CONTROL_CENTER)
                    }
                    .padding(horizontal = 14.dp, vertical = 2.dp)
                    .testTag("control_center_swipe_handle"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Swipe up for Control Center",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Frosted 4-slot Dock (shown on Home Screen app pages)
            if (pagerState.currentPage > 0) {
                IOSDock(
                    dockApps = dockApps,
                    isEditMode = isEditMode,
                    getIconBitmap = { viewModel.getIconBitmap(it) },
                    onAppClick = { app ->
                        if (!isEditMode) {
                            viewModel.launchApp(context, app)
                        }
                    },
                    onAppLongClick = {
                        viewModel.statusMonitor.performHapticClick()
                        viewModel.setEditMode(true)
                    }
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // AssistiveTouch floating button and radial menu
        if (assistiveTouchEnabled && activeOverlay == ActiveOverlay.NONE) {
            AssistiveTouchView(
                onHomeClick = {
                    viewModel.statusMonitor.performHapticClick()
                    scope.launch { pagerState.animateScrollToPage(1) }
                },
                onNotificationsClick = {
                    viewModel.statusMonitor.performHapticClick()
                    viewModel.openOverlay(ActiveOverlay.NOTIFICATION_CENTER)
                },
                onControlCenterClick = {
                    viewModel.statusMonitor.performHapticClick()
                    viewModel.openOverlay(ActiveOverlay.CONTROL_CENTER)
                },
                onRecentsClick = {
                    viewModel.statusMonitor.performHapticClick()
                    viewModel.openOverlay(ActiveOverlay.APP_SWITCHER)
                },
                onSettingsClick = {
                    viewModel.statusMonitor.performHapticClick()
                    viewModel.openOverlay(ActiveOverlay.SETTINGS)
                },
                onLockClick = {
                    viewModel.statusMonitor.performHapticClick()
                    viewModel.openOverlay(ActiveOverlay.LOCK_SCREEN)
                }
            )
        }

        // Overlay Sheets
        // 1. Notification Center (slide from top)
        AnimatedVisibility(
            visible = activeOverlay == ActiveOverlay.NOTIFICATION_CENTER,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            NotificationCenterSheet(
                statusMonitor = viewModel.statusMonitor,
                onDismiss = { viewModel.closeOverlay() }
            )
        }

        // 2. Control Center (slide from bottom)
        AnimatedVisibility(
            visible = activeOverlay == ActiveOverlay.CONTROL_CENTER,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            ControlCenterSheet(
                controlsManager = viewModel.controlsManager,
                onDismiss = { viewModel.closeOverlay() }
            )
        }

        // 3. Spotlight Search (fade in)
        AnimatedVisibility(
            visible = activeOverlay == ActiveOverlay.SPOTLIGHT,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SpotlightSearchSheet(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                allApps = allInstalledApps,
                recentApps = recentApps,
                getIconBitmap = { viewModel.getIconBitmap(it) },
                onAppClick = { viewModel.launchApp(context, it) },
                onDismiss = { viewModel.closeOverlay() }
            )
        }

        // 4. Folder Overlay
        AnimatedVisibility(
            visible = activeOverlay == ActiveOverlay.FOLDER_VIEW && selectedFolder != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            selectedFolder?.let { folder ->
                IOSFolderDialog(
                    folder = folder,
                    getIconBitmap = { viewModel.getIconBitmap(it) },
                    onAppClick = { viewModel.launchApp(context, it) },
                    onRenameFolder = { fId, newName -> viewModel.renameFolder(fId, newName) },
                    onRemoveFolder = { fId -> viewModel.removeFolder(fId) },
                    onDismiss = { viewModel.closeOverlay() }
                )
            }
        }

        // 5. App Switcher / Recent Tasks
        AnimatedVisibility(
            visible = activeOverlay == ActiveOverlay.APP_SWITCHER,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AppSwitcherSheet(
                recentApps = recentApps,
                getIconBitmap = { viewModel.getIconBitmap(it) },
                onAppClick = { viewModel.launchApp(context, it) },
                onDismissApp = { dismissed ->
                    // Remove from recent apps
                },
                onDismiss = { viewModel.closeOverlay() }
            )
        }

        // 6. Settings Dialog
        AnimatedVisibility(
            visible = activeOverlay == ActiveOverlay.SETTINGS,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IOSSettingsDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeOverlay() }
            )
        }

        // 7. Lock Screen Companion
        AnimatedVisibility(
            visible = activeOverlay == ActiveOverlay.LOCK_SCREEN,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IOSLockScreen(
                statusMonitor = viewModel.statusMonitor,
                controlsManager = viewModel.controlsManager,
                wallpaperResId = selectedWallpaperResId,
                onUnlock = {
                    viewModel.statusMonitor.performHapticClick()
                    viewModel.closeOverlay()
                },
                onOpenControlCenter = {
                    viewModel.statusMonitor.performHapticClick()
                    viewModel.openOverlay(ActiveOverlay.CONTROL_CENTER)
                }
            )
        }
    }
}
