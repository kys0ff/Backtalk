package off.kys.backtalk.common.pref

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.common.ExportInterval
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.common.pref.base.PreferenceItem
import off.kys.backtalk.common.pref.model.BooleanPreferenceItem
import off.kys.backtalk.common.pref.model.EnumPreferenceItem
import off.kys.backtalk.common.pref.model.StringPreferenceItem
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

    private val registry = mutableMapOf<String, PreferenceItem<*>>()

    private inline fun <reified T> register(item: T): T where T : PreferenceItem<*> {
        registry[item.key] = item
        return item
    }

    // Preference Items
    private val _themeModeItem = register(
        EnumPreferenceItem(
            prefs,
            KEY_THEME_MODE,
            ThemeMode.AUTO,
            ThemeMode::class.java
        )
    )
    private val _dynamicColorItem = register(
        BooleanPreferenceItem(
            prefs,
            KEY_DYNAMIC_COLOR,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        )
    )
    private val _lockEnabledItem = register(BooleanPreferenceItem(prefs, KEY_LOCK_ENABLED, false))
    private val _secureScreenItem = register(BooleanPreferenceItem(prefs, KEY_SECURE_SCREEN, false))
    private val _autoUpdateItem = register(BooleanPreferenceItem(prefs, KEY_AUTO_UPDATE, false))
    private val _autoExportEnabledItem = register(
        BooleanPreferenceItem(
            prefs,
            KEY_AUTO_EXPORT_ENABLED,
            false
        )
    )
    private val _autoExportUriItem = register(
        StringPreferenceItem(
            prefs,
            KEY_AUTO_EXPORT_URI,
            null
        )
    )
    private val _autoExportIntervalItem = register(
        EnumPreferenceItem(
            prefs,
            KEY_AUTO_EXPORT_INTERVAL,
            ExportInterval.DAILY,
            ExportInterval::class.java
        )
    )
    private val _autoExportEncryptedItem = register(
        BooleanPreferenceItem(
            prefs,
            KEY_AUTO_EXPORT_ENCRYPTED,
            false
        )
    )
    private val _autoExportPasswordItem = register(
        StringPreferenceItem(
            prefs,
            KEY_AUTO_EXPORT_PASSWORD,
            null
        )
    )
    private val _hapticFeedbackItem = register(
        BooleanPreferenceItem(
            prefs,
            KEY_HAPTIC_FEEDBACK,
            true
        )
    )
    private val _keepScreenOnItem = register(
        BooleanPreferenceItem(
            prefs,
            KEY_KEEP_SCREEN_ON,
            BuildConfig.DEBUG
        )
    )
    private val _devModeEnabledItem = register(
        BooleanPreferenceItem(
            prefs,
            KEY_DEV_MODE_ENABLED,
            BuildConfig.DEBUG
        )
    )
    private val _externalLinkWarningItem = register(
        BooleanPreferenceItem(
            prefs,
            KEY_EXTERNAL_LINK_WARNING,
            true
        )
    )
    private val _trimMessagesItem = register(BooleanPreferenceItem(prefs, KEY_TRIM_MESSAGES, false))
    private val _removeImageMetadataItem = register(
        BooleanPreferenceItem(
            prefs,
            KEY_REMOVE_IMAGE_METADATA,
            false
        )
    )
    private val _smartImagePointingItem = register(
        BooleanPreferenceItem(
            prefs,
            KEY_SMART_IMAGE_POINTING,
            false
        )
    )
    private val _pairedDevicesItem = register(StringPreferenceItem(prefs, KEY_PAIRED_DEVICES, "[]"))
    private val _firstLaunchItem = register(BooleanPreferenceItem(prefs, KEY_FIRST_LAUNCH, true))

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
        if (key == null) {
            refreshAll()
        } else {
            registry[key]?.refresh()
        }
        listener?.onSharedPreferenceChanged(p, key)
    }

    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    init {
        prefs.registerOnSharedPreferenceChangeListener(internalListener)
    }

    private fun refreshAll() {
        registry.values.forEach { it.refresh() }
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
        get() = _lockEnabledItem.value
        set(value) { _lockEnabledItem.value = value }

    /**
     * The visual theme strategy for the application (Light, Dark, or System Auto).
     */
    var themeMode: ThemeMode
        get() = _themeModeItem.value
        set(value) { _themeModeItem.value = value }

    /**
     * Whether Material You dynamic color extraction is enabled.
     *
     * Defaults to `true` on Android 12 (API 31) and above.
     */
    var dynamicColorEnabled: Boolean
        get() = _dynamicColorItem.value
        set(value) { _dynamicColorItem.value = value }

    /**
     * If enabled, prevents the app's content from appearing in screenshots or the recent apps switcher.
     */
    var secureScreenEnabled: Boolean
        get() = _secureScreenItem.value
        set(value) { _secureScreenItem.value = value }

    /**
     * Whether the app should periodically poll for new versions in the background.
     */
    var autoUpdateEnabled: Boolean
        get() = _autoUpdateItem.value
        set(value) { _autoUpdateItem.value = value }

    /**
     * Whether the app should automatically export backups.
     */
    var autoExportEnabled: Boolean
        get() = _autoExportEnabledItem.value
        set(value) { _autoExportEnabledItem.value = value }

    /**
     * The persistable URI of the directory where auto-exports are saved.
     */
    var autoExportUri: String?
        get() = _autoExportUriItem.value
        set(value) { _autoExportUriItem.value = value }

    /**
     * The interval at which auto-exports should occur.
     */
    var autoExportInterval: ExportInterval
        get() = _autoExportIntervalItem.value
        set(value) { _autoExportIntervalItem.value = value }

    /**
     * Whether auto-exports should be encrypted.
     */
    var autoExportEncrypted: Boolean
        get() = _autoExportEncryptedItem.value
        set(value) { _autoExportEncryptedItem.value = value }

    /**
     * The password used for auto-export encryption.
     */
    var autoExportPassword: String?
        get() = _autoExportPasswordItem.value
        set(value) { _autoExportPasswordItem.value = value }

    /**
     * Whether haptic feedback (vibration) is enabled for user interactions.
     */
    var hapticFeedbackEnabled: Boolean
        get() = _hapticFeedbackItem.value
        set(value) { _hapticFeedbackItem.value = value }

    /**
     * Whether the screen should stay on while the app is in use.
     */
    var keepScreenOn: Boolean
        get() = _keepScreenOnItem.value
        set(value) { _keepScreenOnItem.value = value }

    /**
     * Whether developer mode is enabled, revealing hidden settings.
     */
    var devModeEnabled: Boolean
        get() = _devModeEnabledItem.value
        set(value) { _devModeEnabledItem.value = value }

    /**
     * Whether to show a warning before opening external links.
     */
    var externalLinkWarningEnabled: Boolean
        get() = _externalLinkWarningItem.value
        set(value) { _externalLinkWarningItem.value = value }

    /**
     * Whether to trim sent messages by default.
     */
    var trimMessagesEnabled: Boolean
        get() = _trimMessagesItem.value
        set(value) { _trimMessagesItem.value = value }

    /**
     * Whether to remove metadata from sent images.
     */
    var removeImageMetadataEnabled: Boolean
        get() = _removeImageMetadataItem.value
        set(value) { _removeImageMetadataItem.value = value }

    /**
     * Whether to use smart pointing for image paths instead of duplicating them.
     */
    var smartImagePointingEnabled: Boolean
        get() = _smartImagePointingItem.value
        set(value) { _smartImagePointingItem.value = value }

    /**
     * The serialized list of paired devices.
     */
    var pairedDevicesJson: String
        get() = _pairedDevicesItem.value ?: "[]"
        set(value) { _pairedDevicesItem.value = value }

    /**
     * Whether it's the first time the app is being launched.
     */
    var firstLaunch: Boolean
        get() = _firstLaunchItem.value
        set(value) { _firstLaunchItem.value = value }

    /**
     * The unique ID for this device used for sync pairing.
     */
    val deviceId: String
        get() = _deviceId.value

    /**
     * Returns a map of all exportable preferences.
     * Excludes device-specific or sensitive keys like device_id or auto_export_uri.
     */
    fun getExportablePreferences(): Map<String, String> {
        val excludedKeys = setOf(KEY_DEVICE_ID, KEY_AUTO_EXPORT_URI, KEY_PAIRED_DEVICES, KEY_FIRST_LAUNCH)
        return registry.filter { it.key !in excludedKeys }
            .mapValues { it.value.serialize() ?: "" }
    }

    /**
     * Restores preferences from a map.
     */
    fun importPreferences(data: Map<String, String>) {
        data.forEach { (key, value) ->
            registry[key]?.deserialize(value)
        }
    }

    /**
     * Clears all stored preferences and resets to defaults.
     */
    fun clearAll() {
        prefs.edit { clear() }
        refreshAll()
    }
}

