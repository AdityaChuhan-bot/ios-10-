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
import android.telephony.TelephonyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SystemStatusMonitor(private val context: Context) {

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _wifiConnected = MutableStateFlow(true)
    val wifiConnected: StateFlow<Boolean> = _wifiConnected.asStateFlow()

    private val _wifiLevel = MutableStateFlow(3) // 0 to 3 bars
    val wifiLevel: StateFlow<Int> = _wifiLevel.asStateFlow()

    private val _carrierName = MutableStateFlow("Carrier")
    val carrierName: StateFlow<String> = _carrierName.asStateFlow()

    private val _cellularDots = MutableStateFlow(4) // 0 to 5 dots (iOS 10 iconic 5-dot signal)
    val cellularDots: StateFlow<Int> = _cellularDots.asStateFlow()

    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _is24Hour = MutableStateFlow(false)
    val is24Hour: StateFlow<Boolean> = _is24Hour.asStateFlow()

    private val _secondAngle = MutableStateFlow(0f)
    val secondAngle: StateFlow<Float> = _secondAngle.asStateFlow()

    private val _minuteAngle = MutableStateFlow(0f)
    val minuteAngle: StateFlow<Float> = _minuteAngle.asStateFlow()

    private val _hourAngle = MutableStateFlow(0f)
    val hourAngle: StateFlow<Float> = _hourAngle.asStateFlow()

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator

    private val scope = CoroutineScope(Dispatchers.Main)

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            intent?.let { updateBattery(it) }
        }
    }

    init {
        // Register battery receiver
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialBattery = context.registerReceiver(batteryReceiver, filter)
        initialBattery?.let { updateBattery(it) }

        // Start time ticker
        scope.launch {
            while (true) {
                updateTime()
                delay(1000)
            }
        }

        // Monitor Network
        checkNetwork()
        updateCarrier()
    }

    fun set24HourFormat(enable: Boolean) {
        _is24Hour.value = enable
        updateTime()
    }

    private fun updateBattery(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level >= 0 && scale > 0) {
            _batteryLevel.value = ((level.toFloat() / scale.toFloat()) * 100).toInt()
        }
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun updateTime() {
        val now = Date()
        val timePattern = if (_is24Hour.value) "HH:mm" else "h:mm"
        val timeFormat = SimpleDateFormat(timePattern, Locale.getDefault())
        _currentTime.value = timeFormat.format(now)

        val datePattern = "EEEE, MMMM d"
        val dateFormat = SimpleDateFormat(datePattern, Locale.getDefault())
        _currentDate.value = dateFormat.format(now)

        val cal = java.util.Calendar.getInstance()
        val sec = cal.get(java.util.Calendar.SECOND)
        val min = cal.get(java.util.Calendar.MINUTE)
        val hr = cal.get(java.util.Calendar.HOUR)

        _secondAngle.value = sec * 6f
        _minuteAngle.value = min * 6f + (sec / 60f) * 6f
        _hourAngle.value = hr * 30f + (min / 60f) * 30f
    }

    fun performHapticClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(android.os.VibrationEffect.createOneShot(15, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {
        }
    }

    private fun checkNetwork() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm != null) {
            val net = cm.activeNetwork
            val caps = cm.getNetworkCapabilities(net)
            if (caps != null) {
                val hasWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                val hasCell = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                _wifiConnected.value = hasWifi
                if (hasWifi) {
                    val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                    val rssi = wm?.connectionInfo?.rssi ?: -60
                    _wifiLevel.value = WifiManager.calculateSignalLevel(rssi, 4).coerceIn(1, 3)
                }
                _cellularDots.value = if (hasCell || hasWifi) 4 else 2
            } else {
                _wifiConnected.value = false
                _cellularDots.value = 1
            }
        }
    }

    private fun updateCarrier() {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val name = tm?.networkOperatorName
        if (!name.isNullOrBlank()) {
            _carrierName.value = name
        } else {
            _carrierName.value = "Cellular"
        }
    }

    fun cleanup() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {
        }
    }
}
