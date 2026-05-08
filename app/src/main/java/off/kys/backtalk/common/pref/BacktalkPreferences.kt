package off.kys.backtalk.common.pref

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import off.kys.backtalk.common.ExportInterval
import off.kys.backtalk.common.ThemeMode

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
        ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.AUTO.name) ?: ThemeMode.AUTO.name)
    )
    private val _dynamicColorEnabled = mutableStateOf(
        prefs.getBoolean(KEY_DYNAMIC_COLOR, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    )
    private val _secureScreenEnabled = mutableStateOf(prefs.getBoolean(KEY_SECURE_SCREEN, false))
    private val _autoUpdateEnabled = mutableStateOf(prefs.getBoolean(KEY_AUTO_UPDATE, false))
    private val _autoExportEnabled = mutableStateOf(prefs.getBoolean(KEY_AUTO_EXPORT_ENABLED, false))
    private val _autoExportUri = mutableStateOf(prefs.getString(KEY_AUTO_EXPORT_URI, null))
    private val _autoExportInterval = mutableStateOf(
        ExportInterval.valueOf(
            prefs.getString(KEY_AUTO_EXPORT_INTERVAL, ExportInterval.DAILY.name) ?: ExportInterval.DAILY.name
        )
    )
    private val _autoExportEncrypted = mutableStateOf(prefs.getBoolean(KEY_AUTO_EXPORT_ENCRYPTED, false))
    private val _autoExportPassword = mutableStateOf(prefs.getString(KEY_AUTO_EXPORT_PASSWORD, null))
    private val _hapticFeedbackEnabled = mutableStateOf(prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true))

    /**
     * Internal reference to the listener.
     *
     * **Warning:** [SharedPreferences] uses a weak reference; we hold this strongly
     * to prevent premature deallocation by the garbage collector.
     */
    private val internalListener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
        when (key) {
            KEY_LOCK_ENABLED -> _lockEnabled.value = p.getBoolean(key, false)
            KEY_THEME_MODE -> _themeMode.value = ThemeMode.valueOf(
                p.getString(key, ThemeMode.AUTO.name) ?: ThemeMode.AUTO.name
            )
            KEY_DYNAMIC_COLOR -> _dynamicColorEnabled.value =
                p.getBoolean(key, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            KEY_SECURE_SCREEN -> _secureScreenEnabled.value = p.getBoolean(key, false)
            KEY_AUTO_UPDATE -> _autoUpdateEnabled.value = p.getBoolean(key, false)
            KEY_AUTO_EXPORT_ENABLED -> _autoExportEnabled.value = p.getBoolean(key, false)
            KEY_AUTO_EXPORT_URI -> _autoExportUri.value = p.getString(key, null)
            KEY_AUTO_EXPORT_INTERVAL -> _autoExportInterval.value = ExportInterval.valueOf(
                p.getString(key, ExportInterval.DAILY.name) ?: ExportInterval.DAILY.name
            )
            KEY_AUTO_EXPORT_ENCRYPTED -> _autoExportEncrypted.value = p.getBoolean(key, false)
            KEY_AUTO_EXPORT_PASSWORD -> _autoExportPassword.value = p.getString(key, null)
            KEY_HAPTIC_FEEDBACK -> _hapticFeedbackEnabled.value = p.getBoolean(key, true)
        }
        listener?.onSharedPreferenceChanged(p, key)
    }

    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    init {
        prefs.registerOnSharedPreferenceChangeListener(internalListener)
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
    }

    /**
     * Registers a callback to be notified of any changes to the preference keys.
     *
     * @param onChanged A lambda that receives the key string of the modified preference.
     */
    fun observeChanges(onChanged: (String) -> Unit) {
        listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            key?.let { onChanged(it) }
        }
    }

    /**
     * Removes the change listener and clears the reference.
     *
     * Call this during lifecycle teardown (e.g., `onDestroy`) to avoid leaking resources.
     */
    fun unregisterObserver() {
        listener = null
    }

    /**
     * Whether the application-level biometric or PIN lock is active.
     */
    var lockEnabled: Boolean
        get() = _lockEnabled.value
        set(value) = prefs.edit { putBoolean(KEY_LOCK_ENABLED, value) }

    /**
     * The visual theme strategy for the application (Light, Dark, or System Auto).
     */
    var themeMode: ThemeMode
        get() = _themeMode.value
        set(value) = prefs.edit { putString(KEY_THEME_MODE, value.name) }

    /**
     * Whether Material You dynamic color extraction is enabled.
     *
     * Defaults to `true` on Android 12 (API 31) and above.
     */
    var dynamicColorEnabled: Boolean
        get() = _dynamicColorEnabled.value
        set(value) = prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, value) }

    /**
     * If enabled, prevents the app's content from appearing in screenshots or the recent apps switcher.
     */
    var secureScreenEnabled: Boolean
        get() = _secureScreenEnabled.value
        set(value) = prefs.edit { putBoolean(KEY_SECURE_SCREEN, value) }

    /**
     * Whether the app should periodically poll for new versions in the background.
     */
    var autoUpdateEnabled: Boolean
        get() = _autoUpdateEnabled.value
        set(value) = prefs.edit { putBoolean(KEY_AUTO_UPDATE, value) }

    /**
     * Whether the app should automatically export backups.
     */
    var autoExportEnabled: Boolean
        get() = _autoExportEnabled.value
        set(value) = prefs.edit { putBoolean(KEY_AUTO_EXPORT_ENABLED, value) }

    /**
     * The persistable URI of the directory where auto-exports are saved.
     */
    var autoExportUri: String?
        get() = _autoExportUri.value
        set(value) = prefs.edit { putString(KEY_AUTO_EXPORT_URI, value) }

    /**
     * The interval at which auto-exports should occur.
     */
    var autoExportInterval: ExportInterval
        get() = _autoExportInterval.value
        set(value) = prefs.edit { putString(KEY_AUTO_EXPORT_INTERVAL, value.name) }

    /**
     * Whether auto-exports should be encrypted.
     */
    var autoExportEncrypted: Boolean
        get() = _autoExportEncrypted.value
        set(value) = prefs.edit { putBoolean(KEY_AUTO_EXPORT_ENCRYPTED, value) }

    /**
     * The password used for auto-export encryption.
     */
    var autoExportPassword: String?
        get() = _autoExportPassword.value
        set(value) = prefs.edit { putString(KEY_AUTO_EXPORT_PASSWORD, value) }

    /**
     * Whether haptic feedback (vibration) is enabled for user interactions.
     */
    var hapticFeedbackEnabled: Boolean
        get() = _hapticFeedbackEnabled.value
        set(value) = prefs.edit { putBoolean(KEY_HAPTIC_FEEDBACK, value) }
}
