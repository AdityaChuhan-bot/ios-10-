package com.example.model

enum class SystemIconType {
    PHONE,
    MESSAGES,
    SAFARI,
    CAMERA,
    PHOTOS,
    SETTINGS,
    MUSIC,
    MAIL,
    CALCULATOR,
    CLOCK,
    CALENDAR,
    NOTES,
    MAPS,
    WEATHER,
    DEFAULT
}

data class AppItem(
    val id: String,
    val label: String,
    val packageName: String,
    val activityName: String = "",
    val isFolder: Boolean = false,
    val folderApps: List<AppItem> = emptyList(),
    val systemIconType: SystemIconType = SystemIconType.DEFAULT,
    val badgeCount: Int = 0
)

data class NotificationModel(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val postTime: Long,
    val isClearable: Boolean = true
)

enum class ActiveOverlay {
    NONE,
    NOTIFICATION_CENTER,
    CONTROL_CENTER,
    SPOTLIGHT,
    APP_SWITCHER,
    FOLDER_VIEW,
    SETTINGS,
    LOCK_SCREEN
}
