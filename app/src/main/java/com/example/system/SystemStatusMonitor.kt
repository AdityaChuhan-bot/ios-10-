package com.example.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Observes device status for the launcher.
 *
 * All callbacks are registered once and released by [cleanup]. The clock job is
 * owned by this monitor (rather than an unbounded, detached Main scope).
 */
class SystemStatusMonitor(context: Context) {

    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var isCleanedUp = false
    private var receiverRegistered = false
    private var networkCallbackRegistered = false

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _wifiConnected = MutableStateFlow(false)
    val wifiConnected: StateFlow<Boolean> = _wifiConnected.asStateFlow()

    private val _wifiLevel = MutableStateFlow(0)
    val wifiLevel: StateFlow<Int> = _wifiLevel.asStateFlow()

    private val _carrierName = MutableStateFlow("Cellular")
    val carrierName: StateFlow<String> = _carrierName.asStateFlow()

    private val _cellularDots = MutableStateFlow(0)
    val cellularDots: StateFlow<Int> = _cellularDots.asStateFlow()

    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _is24Hour = MutableStateFlow(
        Settings.System.getString(appContext.contentResolver, Settings.System.TIME_12_24) == "24"
    )
    val is24Hour: StateFlow<Boolean> = _is24Hour.asStateFlow()

    private val _secondAngle = MutableStateFlow(0f)
    val secondAngle: StateFlow<Float> = _secondAngle.asStateFlow()

    private val _minuteAngle = MutableStateFlow(0f)
    val minuteAngle: StateFlow<Float> = _minuteAngle.asStateFlow()

    private val _hourAngle = MutableStateFlow(0f)
    val hourAngle: StateFlow<Float> = _hourAngle.asStateFlow()

    private val vibrator =
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator

    private val deviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_CHANGED -> intent.let(::updateBattery)
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED,
                Intent.ACTION_DATE_CHANGED,
                Intent.ACTION_LOCALE_CHANGED -> updateTime()
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = refreshNetwork()
        override fun onLost(network: Network) = refreshNetwork()
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) = refreshNetwork()
    }

    init {
        val stateFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_LOCALE_CHANGED)
        }

        // ACTION_BATTERY_CHANGED is sticky: the returned intent supplies the
        // initial state without waiting for the next battery transition.
        val initialBattery = appContext.registerReceiver(deviceStateReceiver, stateFilter)
        receiverRegistered = true
        initialBattery?.let(::updateBattery)

        refreshNetwork()
        updateCarrier()
        updateTime()

        try {
            connectivityManager?.registerDefaultNetworkCallback(networkCallback)
            networkCallbackRegistered = connectivityManager != null
        } catch (_: Exception) {
            // Keep the last known network state if the platform rejects callback registration.
        }

        scope.launch {
            while (isActive) {
                updateTime()
                delay(1_000L)
            }
        }
    }

    fun set24HourFormat(enable: Boolean) {
        _is24Hour.value = enable
        updateTime()
    }

    private fun updateBattery(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level >= 0 && scale > 0) {
            _batteryLevel.value = ((level.toFloat() / scale.toFloat()) * 100f)
                .toInt()
                .coerceIn(0, 100)
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun updateTime() {
        val now = Date()
        val timePattern = if (_is24Hour.value) "HH:mm" else "h:mm"
        _currentTime.value = SimpleDateFormat(timePattern, Locale.getDefault()).format(now)
        _currentDate.value =
            SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now)

        val calendar = java.util.Calendar.getInstance()
        val seconds = calendar.get(java.util.Calendar.SECOND)
        val minutes = calendar.get(java.util.Calendar.MINUTE)
        val hours = calendar.get(java.util.Calendar.HOUR)

        _secondAngle.value = seconds * 6f
        _minuteAngle.value = minutes * 6f + seconds / 10f
        _hourAngle.value = hours * 30f + minutes / 2f
    }

    fun performHapticClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    android.os.VibrationEffect.createOneShot(
                        15L,
                        android.os.VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15L)
            }
        } catch (_: Exception) {
        }
    }

    private fun refreshNetwork() {
        val manager = connectivityManager ?: run {
            _wifiConnected.value = false
            _wifiLevel.value = 0
            _cellularDots.value = 0
            return
        }

        try {
            val network = manager.activeNetwork
            val capabilities = network?.let(manager::getNetworkCapabilities)
            val hasWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            val hasCellular =
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

            _wifiConnected.value = hasWifi
            _cellularDots.value = when {
                hasCellular -> 4
                hasWifi -> 3
                else -> 0
            }

            if (hasWifi) {
                val wifiManager =
                    appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                @Suppress("DEPRECATION")
                val rssi = wifiManager?.connectionInfo?.rssi ?: -100
                _wifiLevel.value =
                    WifiManager.calculateSignalLevel(rssi, 4).coerceIn(0, 3)
            } else {
                _wifiLevel.value = 0
            }
        } catch (_: SecurityException) {
            _wifiConnected.value = false
            _wifiLevel.value = 0
            _cellularDots.value = 0
        } catch (_: Exception) {
            // A transient network-service failure must not crash the launcher.
        }
    }

    private fun updateCarrier() {
        try {
            val telephony =
                appContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            _carrierName.value = telephony?.networkOperatorName
                ?.takeIf { it.isNotBlank() }
                ?: "Cellular"
        } catch (_: Exception) {
            _carrierName.value = "Cellular"
        }
    }

    fun cleanup() {
        if (isCleanedUp) return
        isCleanedUp = true
        scope.cancel()

        if (receiverRegistered) {
            try {
                appContext.unregisterReceiver(deviceStateReceiver)
            } catch (_: IllegalArgumentException) {
            } finally {
                receiverRegistered = false
            }
        }

        if (networkCallbackRegistered) {
            try {
                connectivityManager?.unregisterNetworkCallback(networkCallback)
            } catch (_: Exception) {
            } finally {
                networkCallbackRegistered = false
            }
        }
    }
}
