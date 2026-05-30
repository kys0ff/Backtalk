package off.kys.backtalk.common.pref

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.common.ExportInterval
import off.kys.backtalk.common.ThemeMode
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi

/**
 * Manages the persistent storage and retrieval of application-wide settings using [SharedPreferences].
 *
 * This class acts as a single source of truth for user preferences, wrapping the boilerplate of
 * key management and type casting into clean, observable properties.
 *
 * @param context The context used to access the private shared preferences file.
 */
class BacktalkPreferences(context: Context) {
    /** The [SharedPreferences] instance used for persistence. */
    private val prefs: SharedPreferences =
        context.getSharedPreferences("backtalk_settings", Context.MODE_PRIVATE)

    // Observable states for Compose to react to changes automatically
    private val _lockEnabled = mutableStateOf(prefs.getBoolean(KEY_LOCK_ENABLED, false))
    private val _themeMode = mutableStateOf(
        runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.AUTO.name) ?: ThemeMode.AUTO.name)
        }.getOrDefault(ThemeMode.AUTO)
    )
    private val _dynamicColorEnabled = mutableStateOf(
        prefs.getBoolean(KEY_DYNAMIC_COLOR, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    )
    private val _secureScreenEnabled = mutableStateOf(prefs.getBoolean(KEY_SECURE_SCREEN, false))
    private val _autoUpdateEnabled = mutableStateOf(prefs.getBoolean(KEY_AUTO_UPDATE, false))
    private val _autoExportEnabled = mutableStateOf(prefs.getBoolean(KEY_AUTO_EXPORT_ENABLED, false))
    private val _autoExportUri = mutableStateOf(prefs.getString(KEY_AUTO_EXPORT_URI, null))
    private val _autoExportInterval = mutableStateOf(
        runCatching {
            ExportInterval.valueOf(
                prefs.getString(KEY_AUTO_EXPORT_INTERVAL, ExportInterval.DAILY.name) ?: ExportInterval.DAILY.name
            )
        }.getOrDefault(ExportInterval.DAILY)
    )
    private val _autoExportEncrypted = mutableStateOf(prefs.getBoolean(KEY_AUTO_EXPORT_ENCRYPTED, false))
    private val _autoExportPassword = mutableStateOf(prefs.getString(KEY_AUTO_EXPORT_PASSWORD, null))
    private val _hapticFeedbackEnabled = mutableStateOf(prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true))
    private val _keepScreenOn = mutableStateOf(prefs.getBoolean(KEY_KEEP_SCREEN_ON, BuildConfig.DEBUG))
    private val _devModeEnabled = mutableStateOf(prefs.getBoolean(KEY_DEV_MODE_ENABLED, BuildConfig.DEBUG))
    private val _externalLinkWarningEnabled = mutableStateOf(prefs.getBoolean(KEY_EXTERNAL_LINK_WARNING, true))
    private val _trimMessagesEnabled = mutableStateOf(prefs.getBoolean(KEY_TRIM_MESSAGES, false))
    private val _removeImageMetadataEnabled = mutableStateOf(prefs.getBoolean(KEY_REMOVE_IMAGE_METADATA, false))
    private val _smartImagePointingEnabled = mutableStateOf(prefs.getBoolean(KEY_SMART_IMAGE_POINTING, false))
    private val _pairedDevicesJson = mutableStateOf(prefs.getString(KEY_PAIRED_DEVICES, "[]") ?: "[]")
    private val _firstLaunch = mutableStateOf(prefs.getBoolean(KEY_FIRST_LAUNCH, true))
    private val _deviceId = mutableStateOf(getOrCreateDeviceId())

    @OptIn(ExperimentalUuidApi::class)
    private fun getOrCreateDeviceId(): String {
        val id = prefs.getString(KEY_DEVICE_ID, null)
        return if (id == null) {
            val newId = UUID.randomUUID().toString()
            prefs.edit { putString(KEY_DEVICE_ID, newId) }
            newId
        } else {
            id
        }
    }

    /**
     * Internal reference to the listener.
     *
     * **Warning:** [SharedPreferences] uses a weak reference; we hold this strongly
     * to prevent premature deallocation by the garbage collector.
     */
    private val internalListener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
        when (key) {
            KEY_LOCK_ENABLED -> _lockEnabled.value = p.getBoolean(key, false)
            KEY_THEME_MODE -> _themeMode.value = runCatching {
                ThemeMode.valueOf(p.getString(key, ThemeMode.AUTO.name) ?: ThemeMode.AUTO.name)
            }.getOrDefault(ThemeMode.AUTO)
            KEY_DYNAMIC_COLOR -> _dynamicColorEnabled.value =
                p.getBoolean(key, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            KEY_SECURE_SCREEN -> _secureScreenEnabled.value = p.getBoolean(key, false)
            KEY_AUTO_UPDATE -> _autoUpdateEnabled.value = p.getBoolean(key, false)
            KEY_AUTO_EXPORT_ENABLED -> _autoExportEnabled.value = p.getBoolean(key, false)
            KEY_AUTO_EXPORT_URI -> _autoExportUri.value = p.getString(key, null)
            KEY_AUTO_EXPORT_INTERVAL -> _autoExportInterval.value = runCatching {
                ExportInterval.valueOf(
                    p.getString(key, ExportInterval.DAILY.name) ?: ExportInterval.DAILY.name
                )
            }.getOrDefault(ExportInterval.DAILY)
            KEY_AUTO_EXPORT_ENCRYPTED -> _autoExportEncrypted.value = p.getBoolean(key, false)
            KEY_AUTO_EXPORT_PASSWORD -> _autoExportPassword.value = p.getString(key, null)
            KEY_HAPTIC_FEEDBACK -> _hapticFeedbackEnabled.value = p.getBoolean(key, true)
            KEY_KEEP_SCREEN_ON -> _keepScreenOn.value = p.getBoolean(key, BuildConfig.DEBUG)
            KEY_DEV_MODE_ENABLED -> _devModeEnabled.value = p.getBoolean(key, false)
            KEY_EXTERNAL_LINK_WARNING -> _externalLinkWarningEnabled.value = p.getBoolean(key, true)
            KEY_TRIM_MESSAGES -> _trimMessagesEnabled.value = p.getBoolean(key, false)
            KEY_REMOVE_IMAGE_METADATA -> _removeImageMetadataEnabled.value = p.getBoolean(key, false)
            KEY_SMART_IMAGE_POINTING -> _smartImagePointingEnabled.value = p.getBoolean(key, false)
            KEY_PAIRED_DEVICES -> _pairedDevicesJson.value = p.getString(key, "[]") ?: "[]"
            KEY_FIRST_LAUNCH -> _firstLaunch.value = p.getBoolean(key, true)
            null -> refreshAll() // Handle clear()
        }
        listener?.onSharedPreferenceChanged(p, key)
    }

    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    init {
        prefs.registerOnSharedPreferenceChangeListener(internalListener)
    }

    private fun refreshAll() {
        _lockEnabled.value = prefs.getBoolean(KEY_LOCK_ENABLED, false)
        _themeMode.value = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.AUTO.name) ?: ThemeMode.AUTO.name)
        }.getOrDefault(ThemeMode.AUTO)
        _dynamicColorEnabled.value = prefs.getBoolean(KEY_DYNAMIC_COLOR, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        _secureScreenEnabled.value = prefs.getBoolean(KEY_SECURE_SCREEN, false)
        _autoUpdateEnabled.value = prefs.getBoolean(KEY_AUTO_UPDATE, false)
        _autoExportEnabled.value = prefs.getBoolean(KEY_AUTO_EXPORT_ENABLED, false)
        _autoExportUri.value = prefs.getString(KEY_AUTO_EXPORT_URI, null)
        _autoExportInterval.value = runCatching {
            ExportInterval.valueOf(
                prefs.getString(KEY_AUTO_EXPORT_INTERVAL, ExportInterval.DAILY.name) ?: ExportInterval.DAILY.name
            )
        }.getOrDefault(ExportInterval.DAILY)
        _autoExportEncrypted.value = prefs.getBoolean(KEY_AUTO_EXPORT_ENCRYPTED, false)
        _autoExportPassword.value = prefs.getString(KEY_AUTO_EXPORT_PASSWORD, null)
        _hapticFeedbackEnabled.value = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
        _keepScreenOn.value = prefs.getBoolean(KEY_KEEP_SCREEN_ON, BuildConfig.DEBUG)
        _devModeEnabled.value = prefs.getBoolean(KEY_DEV_MODE_ENABLED, BuildConfig.DEBUG)
        _externalLinkWarningEnabled.value = prefs.getBoolean(KEY_EXTERNAL_LINK_WARNING, true)
        _trimMessagesEnabled.value = prefs.getBoolean(KEY_TRIM_MESSAGES, false)
        _removeImageMetadataEnabled.value = prefs.getBoolean(KEY_REMOVE_IMAGE_METADATA, false)
        _smartImagePointingEnabled.value = prefs.getBoolean(KEY_SMART_IMAGE_POINTING, false)
        _pairedDevicesJson.value = prefs.getString(KEY_PAIRED_DEVICES, "[]") ?: "[]"
        _firstLaunch.value = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    companion object {
        /** Preference key for the visual theme mode. */
        const val KEY_THEME_MODE = "theme_mode"

        /** Preference key for Material You dynamic color extraction. */
        const val KEY_DYNAMIC_COLOR = "dynamic_color"

        /** Preference key for the application-level lock status. */
        const val KEY_LOCK_ENABLED = "lock_enabled"

        /** Preference key for preventing screenshots and screen recordings. */
        const val KEY_SECURE_SCREEN = "secure_screen"

        /** Preference key for background update polling. */
        const val KEY_AUTO_UPDATE = "auto_update"

        /** Preference key for auto export enabled. */
        const val KEY_AUTO_EXPORT_ENABLED = "auto_export_enabled"

        /** Preference key for auto export folder URI. */
        const val KEY_AUTO_EXPORT_URI = "auto_export_uri"

        /** Preference key for auto export interval. */
        const val KEY_AUTO_EXPORT_INTERVAL = "auto_export_interval"

        /** Preference key for auto export encryption enabled. */
        const val KEY_AUTO_EXPORT_ENCRYPTED = "auto_export_encrypted"

        /** Preference key for auto export encryption password. */
        const val KEY_AUTO_EXPORT_PASSWORD = "auto_export_password"

        /** Preference key for enabling/disabling haptic feedback. */
        const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"

        /** Preference key for keeping the screen on. */
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"

        /** Preference key for enabling/disabling developer mode. */
        const val KEY_DEV_MODE_ENABLED = "dev_mode_enabled"

        /** Preference key for enabling/disabling external link warning. */
        const val KEY_EXTERNAL_LINK_WARNING = "external_link_warning"

        /** Preference key for trimming sent messages by default. */
        const val KEY_TRIM_MESSAGES = "trim_messages"

        /** Preference key for removing metadata from sent images. */
        const val KEY_REMOVE_IMAGE_METADATA = "remove_image_metadata"

        /** Preference key for smart pointing image paths. */
        const val KEY_SMART_IMAGE_POINTING = "smart_image_pointing"

        /** Preference key for storing paired devices. */
        const val KEY_PAIRED_DEVICES = "paired_devices"

        /** Preference key for first launch status. */
        const val KEY_FIRST_LAUNCH = "first_launch"

        /** Preference key for the unique device ID. */
        const val KEY_DEVICE_ID = "device_id"
    }

    /**
     * Whether the application-level biometric or PIN lock is active.
     */
    var lockEnabled: Boolean
        get() = _lockEnabled.value
        set(value) {
            _lockEnabled.value = value
            prefs.edit { putBoolean(KEY_LOCK_ENABLED, value) }
        }

    /**
     * The visual theme strategy for the application (Light, Dark, or System Auto).
     */
    var themeMode: ThemeMode
        get() = _themeMode.value
        set(value) {
            _themeMode.value = value
            prefs.edit { putString(KEY_THEME_MODE, value.name) }
        }

    /**
     * Whether Material You dynamic color extraction is enabled.
     *
     * Defaults to `true` on Android 12 (API 31) and above.
     */
    var dynamicColorEnabled: Boolean
        get() = _dynamicColorEnabled.value
        set(value) {
            _dynamicColorEnabled.value = value
            prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, value) }
        }

    /**
     * If enabled, prevents the app's content from appearing in screenshots or the recent apps switcher.
     */
    var secureScreenEnabled: Boolean
        get() = _secureScreenEnabled.value
        set(value) {
            _secureScreenEnabled.value = value
            prefs.edit { putBoolean(KEY_SECURE_SCREEN, value) }
        }

    /**
     * Whether the app should periodically poll for new versions in the background.
     */
    var autoUpdateEnabled: Boolean
        get() = _autoUpdateEnabled.value
        set(value) {
            _autoUpdateEnabled.value = value
            prefs.edit { putBoolean(KEY_AUTO_UPDATE, value) }
        }

    /**
     * Whether the app should automatically export backups.
     */
    var autoExportEnabled: Boolean
        get() = _autoExportEnabled.value
        set(value) {
            _autoExportEnabled.value = value
            prefs.edit { putBoolean(KEY_AUTO_EXPORT_ENABLED, value) }
        }

    /**
     * The persistable URI of the directory where auto-exports are saved.
     */
    var autoExportUri: String?
        get() = _autoExportUri.value
        set(value) {
            _autoExportUri.value = value
            prefs.edit { putString(KEY_AUTO_EXPORT_URI, value) }
        }

    /**
     * The interval at which auto-exports should occur.
     */
    var autoExportInterval: ExportInterval
        get() = _autoExportInterval.value
        set(value) {
            _autoExportInterval.value = value
            prefs.edit { putString(KEY_AUTO_EXPORT_INTERVAL, value.name) }
        }

    /**
     * Whether auto-exports should be encrypted.
     */
    var autoExportEncrypted: Boolean
        get() = _autoExportEncrypted.value
        set(value) {
            _autoExportEncrypted.value = value
            prefs.edit { putBoolean(KEY_AUTO_EXPORT_ENCRYPTED, value) }
        }

    /**
     * The password used for auto-export encryption.
     */
    var autoExportPassword: String?
        get() = _autoExportPassword.value
        set(value) {
            _autoExportPassword.value = value
            prefs.edit { putString(KEY_AUTO_EXPORT_PASSWORD, value) }
        }

    /**
     * Whether haptic feedback (vibration) is enabled for user interactions.
     */
    var hapticFeedbackEnabled: Boolean
        get() = _hapticFeedbackEnabled.value
        set(value) {
            _hapticFeedbackEnabled.value = value
            prefs.edit { putBoolean(KEY_HAPTIC_FEEDBACK, value) }
        }

    /**
     * Whether the screen should stay on while the app is in use.
     */
    var keepScreenOn: Boolean
        get() = _keepScreenOn.value
        set(value) {
            _keepScreenOn.value = value
            prefs.edit { putBoolean(KEY_KEEP_SCREEN_ON, value) }
        }

    /**
     * Whether developer mode is enabled, revealing hidden settings.
     */
    var devModeEnabled: Boolean
        get() = _devModeEnabled.value
        set(value) {
            _devModeEnabled.value = value
            prefs.edit { putBoolean(KEY_DEV_MODE_ENABLED, value) }
        }

    /**
     * Whether to show a warning before opening external links.
     */
    var externalLinkWarningEnabled: Boolean
        get() = _externalLinkWarningEnabled.value
        set(value) {
            _externalLinkWarningEnabled.value = value
            prefs.edit { putBoolean(KEY_EXTERNAL_LINK_WARNING, value) }
        }

    /**
     * Whether to trim sent messages by default.
     */
    var trimMessagesEnabled: Boolean
        get() = _trimMessagesEnabled.value
        set(value) {
            _trimMessagesEnabled.value = value
            prefs.edit { putBoolean(KEY_TRIM_MESSAGES, value) }
        }

    /**
     * Whether to remove metadata from sent images.
     */
    var removeImageMetadataEnabled: Boolean
        get() = _removeImageMetadataEnabled.value
        set(value) {
            _removeImageMetadataEnabled.value = value
            prefs.edit { putBoolean(KEY_REMOVE_IMAGE_METADATA, value) }
        }

    /**
     * Whether to use smart pointing for image paths instead of duplicating them.
     */
    var smartImagePointingEnabled: Boolean
        get() = _smartImagePointingEnabled.value
        set(value) {
            _smartImagePointingEnabled.value = value
            prefs.edit { putBoolean(KEY_SMART_IMAGE_POINTING, value) }
        }

    /**
     * The serialized list of paired devices.
     */
    var pairedDevicesJson: String
        get() = _pairedDevicesJson.value
        set(value) {
            _pairedDevicesJson.value = value
            prefs.edit { putString(KEY_PAIRED_DEVICES, value) }
        }

    /**
     * Whether it's the first time the app is being launched.
     */
    var firstLaunch: Boolean
        get() = _firstLaunch.value
        set(value) {
            _firstLaunch.value = value
            prefs.edit { putBoolean(KEY_FIRST_LAUNCH, value) }
        }

    /**
     * The unique ID for this device used for sync pairing.
     */
    val deviceId: String
        get() = _deviceId.value

    /**
     * Clears all stored preferences and resets to defaults.
     */
    fun clearAll() {
        prefs.edit { clear() }
        refreshAll()
    }
}
