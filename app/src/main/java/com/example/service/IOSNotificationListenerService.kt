package com.example.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import com.example.model.NotificationModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IOSNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        syncActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (instance == this) {
            instance = null
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let { addOrUpdateNotification(it) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let {
            val key = it.key ?: "${it.packageName}_${it.id}"
            _notifications.value = _notifications.value.filter { notif -> notif.key != key }
        }
    }

    private fun syncActiveNotifications() {
        try {
            val active = activeNotifications ?: return
            val list = mutableListOf<NotificationModel>()
            for (sbn in active) {
                parseNotification(sbn)?.let { list.add(it) }
            }
            _notifications.value = list.sortedByDescending { it.postTime }
        } catch (_: Exception) {
        }
    }

    private fun addOrUpdateNotification(sbn: StatusBarNotification) {
        val model = parseNotification(sbn) ?: return
        val current = _notifications.value.toMutableList()
        val index = current.indexOfFirst { it.key == model.key }
        if (index >= 0) {
            current[index] = model
        } else {
            current.add(0, model)
        }
        _notifications.value = current.sortedByDescending { it.postTime }
    }

    private fun parseNotification(sbn: StatusBarNotification): NotificationModel? {
        val notification = sbn.notification ?: return null
        val extras = notification.extras ?: return null
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        if (title.isNullOrBlank() && text.isNullOrBlank()) {
            return null
        }

        val pm = applicationContext.packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            sbn.packageName
        }

        val key = sbn.key ?: "${sbn.packageName}_${sbn.id}"
        return NotificationModel(
            key = key,
            packageName = sbn.packageName,
            appName = appName,
            title = title ?: appName,
            text = text ?: "",
            postTime = sbn.postTime,
            isClearable = sbn.isClearable
        )
    }

    companion object {
        private var instance: IOSNotificationListenerService? = null
        private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
        val notifications: StateFlow<List<NotificationModel>> = _notifications.asStateFlow()

        fun isAccessGranted(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val myComponent = ComponentName(context, IOSNotificationListenerService::class.java).flattenToString()
            return enabledListeners.contains(myComponent)
        }

        fun openSettings(context: Context) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        fun dismiss(key: String) {
            try {
                instance?.cancelNotification(key)
            } catch (_: Exception) {
            }
            _notifications.value = _notifications.value.filter { it.key != key }
        }

        fun clearAll() {
            try {
                instance?.cancelAllNotifications()
            } catch (_: Exception) {
            }
            _notifications.value = _notifications.value.filter { !it.isClearable }
        }
    }
}
