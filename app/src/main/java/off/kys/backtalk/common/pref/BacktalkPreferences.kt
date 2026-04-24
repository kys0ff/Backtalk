package off.kys.backtalk.common.pref

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
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

    /**
     * Internal reference to the listener.
     *
     * **Warning:** [SharedPreferences] uses a weak reference; we hold this strongly
     * to prevent premature deallocation by the garbage collector.
     */
    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

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
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Removes the change listener and clears the reference.
     *
     * Call this during lifecycle teardown (e.g., `onDestroy`) to avoid leaking resources.
     */
    fun unregisterObserver() {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
        listener = null
    }

    /**
     * Whether the application-level biometric or PIN lock is active.
     */
    var lockEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOCK_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_LOCK_ENABLED, value) }

    /**
     * The visual theme strategy for the application (Light, Dark, or System Auto).
     */
    var themeMode: ThemeMode
        get() = ThemeMode.valueOf(
            prefs.getString(KEY_THEME_MODE, ThemeMode.AUTO.name) ?: ThemeMode.AUTO.name
        )
        set(value) = prefs.edit { putString(KEY_THEME_MODE, value.name) }

    /**
     * Whether Material You dynamic color extraction is enabled.
     *
     * Defaults to `true` on Android 12 (API 31) and above.
     */
    var dynamicColorEnabled: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        set(value) = prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, value) }

    /**
     * If enabled, prevents the app's content from appearing in screenshots or the recent apps switcher.
     */
    var secureScreenEnabled: Boolean
        get() = prefs.getBoolean(KEY_SECURE_SCREEN, false)
        set(value) = prefs.edit { putBoolean(KEY_SECURE_SCREEN, value) }

    /**
     * Whether the app should periodically poll for new versions in the background.
     */
    var autoUpdateEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_UPDATE, false)
        set(value) = prefs.edit { putBoolean(KEY_AUTO_UPDATE, value) }
}