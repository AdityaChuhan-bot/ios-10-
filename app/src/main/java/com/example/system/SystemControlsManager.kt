package com.example.system

import android.content.Context
import android.content.Intent
import android.bluetooth.BluetoothAdapter
import android.app.NotificationManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SystemControlsManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _volumeLevel = MutableStateFlow(0.5f)
    val volumeLevel: StateFlow<Float> = _volumeLevel.asStateFlow()

    private val _brightnessLevel = MutableStateFlow(0.7f)
    val brightnessLevel: StateFlow<Float> = _brightnessLevel.asStateFlow()

    private val _isWifiOn = MutableStateFlow(true)
    val isWifiOn: StateFlow<Boolean> = _isWifiOn.asStateFlow()

    private val _isBluetoothOn = MutableStateFlow(true)
    val isBluetoothOn: StateFlow<Boolean> = _isBluetoothOn.asStateFlow()

    private val _isAirplaneMode = MutableStateFlow(false)
    val isAirplaneMode: StateFlow<Boolean> = _isAirplaneMode.asStateFlow()

    private val _isRotationLocked = MutableStateFlow(false)
    val isRotationLocked: StateFlow<Boolean> = _isRotationLocked.asStateFlow()

    private val _isDndActive = MutableStateFlow(false)
    val isDndActive: StateFlow<Boolean> = _isDndActive.asStateFlow()

    private val _isPlayingMusic = MutableStateFlow(false)
    val isPlayingMusic: StateFlow<Boolean> = _isPlayingMusic.asStateFlow()

    private var cameraIdWithFlash: String? = null

    init {
        // Find camera with flash
        try {
            cameraManager?.let { cm ->
                for (id in cm.cameraIdList) {
                    val characteristics = cm.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    if (hasFlash) {
                        cameraIdWithFlash = id
                        break
                    }
                }
            }
        } catch (_: Exception) {
        }

        // Initialize controls from actual device state.
        syncVolume()
        syncSystemStates()
    }

    fun syncSystemStates() {
        try {
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
            _isWifiOn.value = wifi?.isWifiEnabled == true
        } catch (_: Exception) {}
        try {
            @Suppress("DEPRECATION")
            val adapter = BluetoothAdapter.getDefaultAdapter()
            _isBluetoothOn.value = adapter?.isEnabled == true
        } catch (_: Exception) {}
        try {
            _isAirplaneMode.value = Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
        } catch (_: Exception) {}
        try {
            _isRotationLocked.value = Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1) == 0
        } catch (_: Exception) {}
        try {
            val brightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                178
            )
            _brightnessLevel.value = (brightness / 255f).coerceIn(0.05f, 1f)
        } catch (_: Exception) {}
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            _isDndActive.value = manager?.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
        } catch (_: Exception) {}
    }

    fun syncVolume() {
        try {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
            _volumeLevel.value = (current / max).coerceIn(0f, 1f)
            _isPlayingMusic.value = audioManager.isMusicActive
        } catch (_: Exception) {
        }
    }

    fun setVolume(ratio: Float) {
        _volumeLevel.value = ratio.coerceIn(0f, 1f)
        try {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = (ratio * max).toInt().coerceIn(0, max)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        } catch (_: Exception) {
        }
    }

    fun setBrightness(ratio: Float) {
        _brightnessLevel.value = ratio.coerceIn(0.05f, 1f)
        try {
            if (!Settings.System.canWrite(context)) {
                val permissionIntent = Intent(
                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + context.packageName)
                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                context.startActivity(permissionIntent)
                return
            }
            val value = (_brightnessLevel.value * 255).toInt().coerceIn(10, 255)
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
        } catch (_: Exception) {
        }
    }

    fun toggleFlashlight() {
        val target = !_isFlashlightOn.value
        val id = cameraIdWithFlash ?: return
        val manager = cameraManager ?: return
        try {
            manager.setTorchMode(id, target)
            _isFlashlightOn.value = target
        } catch (_: Exception) {
            // Keep the last confirmed hardware state; never report a fake toggle.
        }
    }

    fun toggleWifi() {
        // Android 10+ requires user-mediated Wi-Fi changes; open the system panel.
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_WIFI)
            } else {
                Intent(Settings.ACTION_WIFI_SETTINGS)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    fun toggleBluetooth() {
        // Modern Android restricts direct Bluetooth toggles for ordinary apps.
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun toggleAirplaneMode() {
        // Airplane mode is protected by the OS; open its system settings.
        try {
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun toggleRotationLock() {
        try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openCastSettings() {
        try {
            context.startActivity(Intent(Settings.ACTION_CAST_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (_: Exception) {}
        }
    }

    fun openNightLightSettings() {
        val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Settings.ACTION_NIGHT_DISPLAY_SETTINGS
        } else {
            Settings.ACTION_DISPLAY_SETTINGS
        }
        try {
            context.startActivity(Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (_: Exception) {}
        }
    }

    fun toggleDnd() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openCamera() {
        try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    fun openCalculator() {
        val calculatorPackages = listOf(
            "com.google.android.calculator",
            "com.android.calculator2",
            "com.sec.android.app.popupcalculator",
            "com.miui.calculator"
        )
        for (pkg in calculatorPackages) {
            val launch = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launch)
                return
            }
        }
        // Fallback: general intent
        try {
            val intent = Intent().apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_APP_CALCULATOR)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openTimer() {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(clockIntent)
            } catch (_: Exception) {
            }
        }
    }

    fun sendMediaKey(keyCode: Int) {
        try {
            val eventDown = KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, keyCode, 0)
            val eventUp = KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, keyCode, 0)
            audioManager.dispatchMediaKeyEvent(eventDown)
            audioManager.dispatchMediaKeyEvent(eventUp)
            if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                _isPlayingMusic.value = !_isPlayingMusic.value
            }
        } catch (_: Exception) {
        }
    }

    fun cleanup() {
        if (_isFlashlightOn.value && cameraIdWithFlash != null) {
            try {
                cameraManager?.setTorchMode(cameraIdWithFlash!!, false)
            } catch (_: Exception) {
            }
        }
    }
}
