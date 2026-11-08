package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.model.ActiveOverlay
import com.example.model.AppItem
import com.example.model.SystemIconType
import com.example.service.IOSNotificationListenerService
import com.example.system.SystemControlsManager
import com.example.system.SystemStatusMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = application.getSharedPreferences("launcher_preferences", Context.MODE_PRIVATE)
    private val hiddenAppIds = preferences.getStringSet("hidden_app_ids", emptySet())
        ?.toMutableSet() ?: mutableSetOf()

    val statusMonitor = SystemStatusMonitor(application)
    val controlsManager = SystemControlsManager(application)

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val _pages = MutableStateFlow<List<List<AppItem>>>(emptyList())
    val pages: StateFlow<List<List<AppItem>>> = _pages.asStateFlow()

    private val _dockApps = MutableStateFlow<List<AppItem>>(emptyList())
    val dockApps: StateFlow<List<AppItem>> = _dockApps.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _activeOverlay = MutableStateFlow(ActiveOverlay.NONE)
    val activeOverlay: StateFlow<ActiveOverlay> = _activeOverlay.asStateFlow()

    private val _selectedFolder = MutableStateFlow<AppItem?>(null)
    val selectedFolder: StateFlow<AppItem?> = _selectedFolder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _recentApps = MutableStateFlow<List<AppItem>>(emptyList())
    val recentApps: StateFlow<List<AppItem>> = _recentApps.asStateFlow()

    private val _assistiveTouchEnabled = MutableStateFlow(preferences.getBoolean("assistive_touch_enabled", true))
    val assistiveTouchEnabled: StateFlow<Boolean> = _assistiveTouchEnabled.asStateFlow()

    private val _showIOSStatusBar = MutableStateFlow(preferences.getBoolean("show_ios_status_bar", true))
    val showIOSStatusBar: StateFlow<Boolean> = _showIOSStatusBar.asStateFlow()

    private val _selectedWallpaperResId = MutableStateFlow(preferences.getInt("wallpaper_res_id", R.drawable.wallpaper_ios10_wave))
    val selectedWallpaperResId: StateFlow<Int> = _selectedWallpaperResId.asStateFlow()

    fun setShowIOSStatusBar(enabled: Boolean) {
        _showIOSStatusBar.value = enabled
        preferences.edit().putBoolean("show_ios_status_bar", enabled).apply()
    }

    // Cache of loaded app icon bitmaps
    private val iconCache = mutableMapOf<String, ImageBitmap>()

    init {
        loadApplications()
    }

    fun loadApplications() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolvedList: List<ResolveInfo> = pm.queryIntentActivities(mainIntent, 0)
            val myPackage = getApplication<Application>().packageName

            val rawApps = mutableListOf<AppItem>()

            for (resolveInfo in resolvedList) {
                val pkgName = resolveInfo.activityInfo.packageName
                if (pkgName == myPackage) continue // Skip our own launcher from the grid

                val label = resolveInfo.loadLabel(pm).toString()
                val activityName = resolveInfo.activityInfo.name
                val iconType = detectSystemIconType(label, pkgName)

                // Load icon bitmap
                try {
                    val drawable = resolveInfo.loadIcon(pm)
                    val bitmap = drawableToBitmap(drawable)
                    iconCache[pkgName] = bitmap.asImageBitmap()
                } catch (_: Exception) {
                }

                rawApps.add(
                    AppItem(
                        id = "$pkgName/$activityName",
                        label = label,
                        packageName = pkgName,
                        activityName = activityName,
                        systemIconType = iconType
                    )
                )
            }

            // Sort alphabetically for clean initial structure
            rawApps.sortBy { it.label.lowercase() }
            _installedApps.value = rawApps

            // Load saved layout or generate default
            loadSavedLayout(rawApps.filterNot { it.id in hiddenAppIds })
        }
    }

    private fun detectSystemIconType(label: String, pkg: String): SystemIconType {
        val lowerLabel = label.lowercase()
        val lowerPkg = pkg.lowercase()

        return when {
            lowerPkg.contains("dialer") || lowerPkg.contains("phone") || lowerLabel == "phone" -> SystemIconType.PHONE
            lowerPkg.contains("messaging") || lowerPkg.contains("mms") || lowerLabel.contains("message") -> SystemIconType.MESSAGES
            lowerPkg.contains("chrome") || lowerPkg.contains("browser") || lowerLabel.contains("browser") || lowerLabel == "safari" -> SystemIconType.SAFARI
            lowerPkg.contains("camera") || lowerLabel == "camera" -> SystemIconType.CAMERA
            lowerPkg.contains("gallery") || lowerPkg.contains("photos") || lowerLabel.contains("photo") -> SystemIconType.PHOTOS
            lowerPkg.contains("settings") || lowerLabel == "settings" -> SystemIconType.SETTINGS
            lowerPkg.contains("music") || lowerPkg.contains("spotify") || lowerLabel.contains("music") -> SystemIconType.MUSIC
            lowerPkg.contains("gm") || lowerPkg.contains("email") || lowerPkg.contains("mail") || lowerLabel.contains("mail") -> SystemIconType.MAIL
            lowerPkg.contains("calculator") || lowerLabel == "calculator" -> SystemIconType.CALCULATOR
            lowerPkg.contains("clock") || lowerPkg.contains("deskclock") || lowerLabel == "clock" -> SystemIconType.CLOCK
            lowerPkg.contains("calendar") || lowerLabel == "calendar" -> SystemIconType.CALENDAR
            lowerPkg.contains("notes") || lowerPkg.contains("keep") || lowerLabel.contains("note") -> SystemIconType.NOTES
            lowerPkg.contains("maps") || lowerLabel.contains("map") -> SystemIconType.MAPS
            lowerPkg.contains("weather") || lowerLabel.contains("weather") -> SystemIconType.WEATHER
            else -> SystemIconType.DEFAULT
        }
    }

    private fun loadSavedLayout(allApps: List<AppItem>) {
        val file = File(getApplication<Application>().filesDir, "launcher_layout.json")
        if (!file.exists()) {
            buildDefaultLayout(allApps)
            return
        }

        try {
            val json = JSONObject(file.readText())
            val appMap = allApps.associateBy { it.id }.toMutableMap()

            // Restore Dock
            val dockJson = json.getJSONArray("dock")
            val dockList = mutableListOf<AppItem>()
            for (i in 0 until dockJson.length()) {
                val id = dockJson.getString(i)
                appMap.remove(id)?.let { dockList.add(it) }
            }

            // Restore Pages & Folders
            val pagesJson = json.getJSONArray("pages")
            val pagesList = mutableListOf<List<AppItem>>()
            for (p in 0 until pagesJson.length()) {
                val pageArray = pagesJson.getJSONArray(p)
                val pageItems = mutableListOf<AppItem>()
                for (i in 0 until pageArray.length()) {
                    val itemJson = pageArray.getJSONObject(i)
                    if (itemJson.optBoolean("isFolder", false)) {
                        val folderName = itemJson.optString("label", "Folder")
                        val folderId = itemJson.optString("id", UUID.randomUUID().toString())
                        val folderAppsJson = itemJson.getJSONArray("apps")
                        val folderApps = mutableListOf<AppItem>()
                        for (f in 0 until folderAppsJson.length()) {
                            val fId = folderAppsJson.getString(f)
                            appMap.remove(fId)?.let { folderApps.add(it) }
                        }
                        if (folderApps.isNotEmpty()) {
                            pageItems.add(
                                AppItem(
                                    id = folderId,
                                    label = folderName,
                                    packageName = "",
                                    isFolder = true,
                                    folderApps = folderApps
                                )
                            )
                        }
                    } else {
                        val id = itemJson.getString("id")
                        appMap.remove(id)?.let { pageItems.add(it) }
                    }
                }
                if (pageItems.isNotEmpty()) {
                    pagesList.add(pageItems)
                }
            }

            // Append any newly installed apps to the last page
            val remainingApps = appMap.values.toList()
            if (remainingApps.isNotEmpty()) {
                if (pagesList.isEmpty()) {
                    pagesList.add(remainingApps)
                } else {
                    val mutablePages = pagesList.map { it.toMutableList() }.toMutableList()
                    for (app in remainingApps) {
                        val lastPage = mutablePages.last()
                        if (lastPage.size < 24) {
                            lastPage.add(app)
                        } else {
                            mutablePages.add(mutableListOf(app))
                        }
                    }
                    pagesList.clear()
                    pagesList.addAll(mutablePages)
                }
            }

            _dockApps.value = dockList.take(4)
            _pages.value = if (pagesList.isEmpty()) listOf(emptyList()) else pagesList
        } catch (_: Exception) {
            buildDefaultLayout(allApps)
        }
    }

    private fun buildDefaultLayout(allApps: List<AppItem>) {
        val remaining = allApps.toMutableList()

        // Select dock items: Phone, Safari/Chrome, Messages, Music
        val dockList = mutableListOf<AppItem>()
        val phoneApp = remaining.firstOrNull { it.systemIconType == SystemIconType.PHONE }
        phoneApp?.let {
            dockList.add(it)
            remaining.remove(it)
        }
        val safariApp = remaining.firstOrNull { it.systemIconType == SystemIconType.SAFARI }
        safariApp?.let {
            dockList.add(it)
            remaining.remove(it)
        }
        val messagesApp = remaining.firstOrNull { it.systemIconType == SystemIconType.MESSAGES }
        messagesApp?.let {
            dockList.add(it)
            remaining.remove(it)
        }
        val musicApp = remaining.firstOrNull { it.systemIconType == SystemIconType.MUSIC }
        musicApp?.let {
            dockList.add(it)
            remaining.remove(it)
        }

        // Fill dock if less than 4
        while (dockList.size < 4 && remaining.isNotEmpty()) {
            dockList.add(remaining.removeAt(0))
        }

        // Split remaining into 4x6 = 24 items per page
        val pagesList = mutableListOf<List<AppItem>>()
        val chunkSize = 24
        for (i in remaining.indices step chunkSize) {
            val chunk = remaining.subList(i, (i + chunkSize).coerceAtMost(remaining.size))
            pagesList.add(chunk)
        }

        if (pagesList.isEmpty()) {
            pagesList.add(emptyList())
        }

        _dockApps.value = dockList
        _pages.value = pagesList
        saveLayout()
    }

    fun saveLayout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = JSONObject()
                val dockArray = JSONArray()
                for (item in _dockApps.value) {
                    dockArray.put(item.id)
                }
                json.put("dock", dockArray)

                val pagesArray = JSONArray()
                for (page in _pages.value) {
                    val pageJson = JSONArray()
                    for (item in page) {
                        val itemObj = JSONObject()
                        itemObj.put("id", item.id)
                        itemObj.put("isFolder", item.isFolder)
                        if (item.isFolder) {
                            itemObj.put("label", item.label)
                            val folderAppsArray = JSONArray()
                            for (fApp in item.folderApps) {
                                folderAppsArray.put(fApp.id)
                            }
                            itemObj.put("apps", folderAppsArray)
                        }
                        pageJson.put(itemObj)
                    }
                    pagesArray.put(pageJson)
                }
                json.put("pages", pagesArray)

                val file = File(getApplication<Application>().filesDir, "launcher_layout.json")
                file.writeText(json.toString(2))
            } catch (_: Exception) {
            }
        }
    }

    fun getIconBitmap(packageName: String): ImageBitmap? {
        return iconCache[packageName]
    }

    fun launchApp(context: Context, app: AppItem) {
        if (app.isFolder) {
            _selectedFolder.value = app
            _activeOverlay.value = ActiveOverlay.FOLDER_VIEW
            return
        }

        // Add to recents
        val updatedRecents = (_recentApps.value.filter { it.id != app.id }).toMutableList()
        updatedRecents.add(0, app)
        _recentApps.value = updatedRecents.take(12)

        // Close any active overlay
        _activeOverlay.value = ActiveOverlay.NONE
        _searchQuery.value = ""

        try {
            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (_: Exception) {
        }
    }

    fun setEditMode(enable: Boolean) {
        _isEditMode.value = enable
    }

    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }

    fun openOverlay(overlay: ActiveOverlay) {
        _activeOverlay.value = overlay
        if (overlay == ActiveOverlay.CONTROL_CENTER) {
            controlsManager.syncVolume()
            controlsManager.syncSystemStates()
        }
    }

    fun closeOverlay() {
        _activeOverlay.value = ActiveOverlay.NONE
        _selectedFolder.value = null
        _searchQuery.value = ""
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleAssistiveTouch() {
        val enabled = !_assistiveTouchEnabled.value
        _assistiveTouchEnabled.value = enabled
        preferences.edit().putBoolean("assistive_touch_enabled", enabled).apply()
    }

    fun setWallpaper(resId: Int) {
        _selectedWallpaperResId.value = resId
        preferences.edit().putInt("wallpaper_res_id", resId).apply()
    }

    fun dismissRecentApp(app: AppItem) {
        _recentApps.value = _recentApps.value.filterNot { it.id == app.id }
    }

    /**
     * Reorders two icons on the same home-screen page. Dropping onto a folder
     * moves the dragged app into that folder; dropping onto an app swaps slots.
     */
    fun moveApp(source: AppItem, target: AppItem, pageIndex: Int) {
        if (source.id == target.id) return
        val currentPages = _pages.value.map { it.toMutableList() }.toMutableList()
        if (pageIndex !in currentPages.indices) return
        val page = currentPages[pageIndex]
        val sourceIndex = page.indexOfFirst { it.id == source.id }
        val targetIndex = page.indexOfFirst { it.id == target.id }
        if (sourceIndex < 0 || targetIndex < 0) return

        if (target.isFolder) {
            page[targetIndex] = target.copy(folderApps = target.folderApps + source)
            page.removeAt(sourceIndex)
        } else {
            page[sourceIndex] = target
            page[targetIndex] = source
        }
        _pages.value = currentPages
        saveLayout()
    }

    fun createFolder(source: AppItem, target: AppItem, pageIndex: Int) {
        if (source.id == target.id) return

        val currentPages = _pages.value.map { it.toMutableList() }.toMutableList()
        if (pageIndex !in currentPages.indices) return

        val page = currentPages[pageIndex]
        page.remove(source)

        val targetIdx = page.indexOfFirst { it.id == target.id }
        if (targetIdx >= 0) {
            if (target.isFolder) {
                // Add to existing folder
                val updatedFolder = target.copy(folderApps = target.folderApps + source)
                page[targetIdx] = updatedFolder
            } else {
                // Create new folder containing both
                val folder = AppItem(
                    id = "folder_${UUID.randomUUID()}",
                    label = "Folder",
                    packageName = "",
                    isFolder = true,
                    folderApps = listOf(target, source)
                )
                page[targetIdx] = folder
            }
        }

        _pages.value = currentPages
        saveLayout()
    }

    fun renameFolder(folderId: String, newName: String) {
        val currentPages = _pages.value.map { it.toMutableList() }.toMutableList()
        for (page in currentPages) {
            val idx = page.indexOfFirst { it.id == folderId }
            if (idx >= 0) {
                page[idx] = page[idx].copy(label = newName)
                _pages.value = currentPages
                _selectedFolder.value = page[idx]
                saveLayout()
                return
            }
        }
    }

    fun removeFolder(folderId: String) {
        val currentPages = _pages.value.map { it.toMutableList() }.toMutableList()
        for (page in currentPages) {
            val folder = page.firstOrNull { it.id == folderId }
            if (folder != null) {
                page.remove(folder)
                // Put apps back onto page
                page.addAll(folder.folderApps)
                _pages.value = currentPages
                closeOverlay()
                saveLayout()
                return
            }
        }
    }

    fun removeAppFromGrid(app: AppItem) {
        if (app.isFolder) return

        val currentPages = _pages.value.map { page ->
            page.mapNotNull { item ->
                when {
                    item.id == app.id -> null
                    item.isFolder && item.folderApps.any { it.id == app.id } -> {
                        val remaining = item.folderApps.filterNot { it.id == app.id }
                        if (remaining.isEmpty()) null
                        else if (remaining.size == 1) remaining.first()
                        else item.copy(folderApps = remaining)
                    }
                    else -> item
                }
            }
        }

        _dockApps.value = _dockApps.value.filterNot { it.id == app.id }
        _pages.value = if (currentPages.isEmpty()) listOf(emptyList()) else currentPages
        hiddenAppIds.add(app.id)
        preferences.edit().putStringSet("hidden_app_ids", hiddenAppIds.toSet()).apply()
        saveLayout()
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun onCleared() {
        super.onCleared()
        statusMonitor.cleanup()
        controlsManager.cleanup()
    }
}
