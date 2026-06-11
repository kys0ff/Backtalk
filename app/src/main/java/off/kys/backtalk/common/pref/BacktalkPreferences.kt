package off.kys.backtalk.common.pref

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.common.AppDateFormat
import off.kys.backtalk.common.AppTimeFormat
import off.kys.backtalk.common.RepeatFrequency
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.common.pref.base.PreferenceItem
import off.kys.backtalk.common.pref.model.BooleanPreferenceItem
import off.kys.backtalk.common.pref.model.EnumPreferenceItem
import off.kys.backtalk.common.pref.model.LongPreferenceItem
import off.kys.backtalk.common.pref.model.StringPreferenceItem
import java.util.UUID
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Manages the persistent storage and retrieval of application-wide settings using [SharedPreferences].
 *
 * This class acts as a single source of truth for user preferences, wrapping the boilerplate of
 * key management and type casting into clean, observable properties.
 *
 * @param context The context used to access the private shared preferences file.
 */
class BacktalkPreferences(private val context: Context) {
    /** The [SharedPreferences] instance used for persistence. */
    private val prefs: SharedPreferences =
        context.getSharedPreferences("backtalk_settings", Context.MODE_PRIVATE)

    private val registry = mutableMapOf<String, PreferenceItem<*>>()

    /**
     * Registers a [PreferenceItem] and returns a delegate that handles getting and setting its value.
     */
    private fun <T, P : PreferenceItem<T>> preference(item: P): ReadWriteProperty<BacktalkPreferences, T> {
        registry[item.key] = item
        return object : ReadWriteProperty<BacktalkPreferences, T> {
            override fun getValue(thisRef: BacktalkPreferences, property: KProperty<*>): T = item.value
            override fun setValue(thisRef: BacktalkPreferences, property: KProperty<*>, value: T) {
                item.value = value
            }
        }
    }

    /** The number of milliseconds the app can remain in the background before locking. */
    var lockTimeoutMillis by preference(LongPreferenceItem(prefs, KEY_LOCK_TIMEOUT_MILLIS, 0L))

    /** Whether the app should lock immediately when the screen is turned off. */
    var lockOnScreenOff by preference(BooleanPreferenceItem(prefs, KEY_LOCK_ON_SCREEN_OFF, false))

    /** Whether the application-level biometric or PIN lock is active. */
    var lockEnabled by preference(BooleanPreferenceItem(prefs, KEY_LOCK_ENABLED, false))

    /** The visual theme strategy for the application (Light, Dark, or System Auto). */
    var themeMode by preference(
        EnumPreferenceItem(prefs, KEY_THEME_MODE, ThemeMode.AUTO, ThemeMode::class.java)
    )

    /** Whether Material You dynamic color extraction is enabled. */
    var dynamicColorEnabled by preference(
        BooleanPreferenceItem(prefs, KEY_DYNAMIC_COLOR, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    )

    /** If enabled, prevents the app's content from appearing in screenshots or the recent apps switcher. */
    var secureScreenEnabled by preference(BooleanPreferenceItem(prefs, KEY_SECURE_SCREEN, false))

    /** Whether the app should periodically poll for new versions in the background. */
    var autoUpdateEnabled by preference(BooleanPreferenceItem(prefs, KEY_AUTO_UPDATE, false))

    /** Whether the app should automatically export backups. */
    var autoExportEnabled by preference(BooleanPreferenceItem(prefs, KEY_AUTO_EXPORT_ENABLED, false))

    /** Whether reminders are enabled. */
    var remindersEnabled by preference(BooleanPreferenceItem(prefs, KEY_REMINDERS_ENABLED, false))

    /** The interval at which reminders should occur. */
    var reminderInterval by preference(
        EnumPreferenceItem(prefs, KEY_REMINDER_INTERVAL, RepeatFrequency.DAILY, RepeatFrequency::class.java)
    )

    /** The date format for displaying dates throughout the app. */
    var dateFormat by preference(
        EnumPreferenceItem(prefs, KEY_DATE_FORMAT, AppDateFormat.SYSTEM, AppDateFormat::class.java)
    )

    /** The time format for displaying times throughout the app. */
    var timeFormat by preference(
        EnumPreferenceItem(prefs, KEY_TIME_FORMAT, AppTimeFormat.SYSTEM, AppTimeFormat::class.java)
    )

    /** The custom date format pattern. */
    var customDateFormat by preference(StringPreferenceItem(prefs, KEY_CUSTOM_DATE_FORMAT, "MMM d, yyyy"))

    /** The persistable URI of the directory where auto-exports are saved. */
    var autoExportUri by preference(StringPreferenceItem(prefs, KEY_AUTO_EXPORT_URI, null))

    /** The interval at which auto-exports should occur. */
    var autoRepeatFrequency by preference(
        EnumPreferenceItem(prefs, KEY_AUTO_EXPORT_INTERVAL, RepeatFrequency.DAILY, RepeatFrequency::class.java)
    )

    /** Whether auto-exports should be encrypted. */
    var autoExportEncrypted by preference(BooleanPreferenceItem(prefs, KEY_AUTO_EXPORT_ENCRYPTED, false))

    /** The password used for auto-export encryption. */
    var autoExportPassword by preference(StringPreferenceItem(prefs, KEY_AUTO_EXPORT_PASSWORD, null))

    /** Whether haptic feedback (vibration) is enabled for user interactions. */
    var hapticFeedbackEnabled by preference(BooleanPreferenceItem(prefs, KEY_HAPTIC_FEEDBACK, true))

    /** Whether the screen should stay on while the app is in use. */
    var keepScreenOn by preference(BooleanPreferenceItem(prefs, KEY_KEEP_SCREEN_ON, BuildConfig.DEBUG))

    /** Whether developer mode is enabled, revealing hidden settings. */
    var devModeEnabled by preference(BooleanPreferenceItem(prefs, KEY_DEV_MODE_ENABLED, BuildConfig.DEBUG))

    /** Whether to show a warning before opening external links. */
    var externalLinkWarningEnabled by preference(BooleanPreferenceItem(prefs, KEY_EXTERNAL_LINK_WARNING, true))

    /** Whether to trim sent messages by default. */
    var trimMessagesEnabled by preference(BooleanPreferenceItem(prefs, KEY_TRIM_MESSAGES, false))

    /** Whether to show a preview of the first link in a message. */
    var linkPreviewEnabled by preference(BooleanPreferenceItem(prefs, KEY_LINK_PREVIEW_ENABLED, false))

    /** Whether the keyboard's enter key should trigger a send action instead of a new line. */
    var sendWithEnter by preference(BooleanPreferenceItem(prefs, KEY_SEND_WITH_ENTER, false))

    /** Whether to remove metadata from sent images. */
    var removeImageMetadataEnabled by preference(BooleanPreferenceItem(prefs, KEY_REMOVE_IMAGE_METADATA, false))

    /** Whether to use smart pointing for image paths instead of duplicating them. */
    var smartImagePointingEnabled by preference(BooleanPreferenceItem(prefs, KEY_SMART_IMAGE_POINTING, false))

    /** Whether the hashtags bar is shown at the top of the messages list. */
    var showTagsBar by preference(BooleanPreferenceItem(prefs, KEY_SHOW_TAGS_BAR, true))

    /** The serialized list of paired devices. */
    var pairedDevicesJson by preference(StringPreferenceItem(prefs, KEY_PAIRED_DEVICES, "[]"))

    /** Whether it's the first time the app is being launched. */
    var firstLaunch by preference(BooleanPreferenceItem(prefs, KEY_FIRST_LAUNCH, true))

    /** The last version of the app for which the changelog was seen. */
    var lastSeenChangelogVersion by preference(StringPreferenceItem(prefs, KEY_LAST_SEEN_CHANGELOG_VERSION, ""))

    // --- Compose State Device ID ---
    private val _deviceIdState = mutableStateOf(getOrCreateDeviceId())

    /** The unique ID for this device used for sync pairing. */
    val deviceId: String get() = _deviceIdState.value

    // --- Initialization Logic and Listeners ---

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
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_DYNAMIC_COLOR = "dynamic_color"
        const val KEY_LOCK_ENABLED = "lock_enabled"
        const val KEY_LOCK_ON_SCREEN_OFF = "lock_on_screen_off"
        const val KEY_LOCK_TIMEOUT_MILLIS = "lock_timeout_millis"
        const val KEY_SECURE_SCREEN = "secure_screen"
        const val KEY_AUTO_UPDATE = "auto_update"
        const val KEY_AUTO_EXPORT_ENABLED = "auto_export_enabled"
        const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        const val KEY_REMINDER_INTERVAL = "reminder_interval"
        const val KEY_DATE_FORMAT = "date_format"
        const val KEY_TIME_FORMAT = "time_format"
        const val KEY_CUSTOM_DATE_FORMAT = "custom_date_format"
        const val KEY_AUTO_EXPORT_URI = "auto_export_uri"
        const val KEY_AUTO_EXPORT_INTERVAL = "auto_export_interval"
        const val KEY_AUTO_EXPORT_ENCRYPTED = "auto_export_encrypted"
        const val KEY_AUTO_EXPORT_PASSWORD = "auto_export_password"
        const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        const val KEY_DEV_MODE_ENABLED = "dev_mode_enabled"
        const val KEY_EXTERNAL_LINK_WARNING = "external_link_warning"
        const val KEY_TRIM_MESSAGES = "trim_messages"
        const val KEY_LINK_PREVIEW_ENABLED = "link_preview_enabled"
        const val KEY_SEND_WITH_ENTER = "send_with_enter"
        const val KEY_REMOVE_IMAGE_METADATA = "remove_image_metadata"
        const val KEY_SMART_IMAGE_POINTING = "smart_image_pointing"
        const val KEY_SHOW_TAGS_BAR = "show_tags_bar"
        const val KEY_PAIRED_DEVICES = "paired_devices"
        const val KEY_FIRST_LAUNCH = "first_launch"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_LAST_SEEN_CHANGELOG_VERSION = "last_seen_changelog_version"
    }

    fun getExportablePreferences(): Map<String, String> {
        val excludedKeys = setOf(KEY_DEVICE_ID, KEY_AUTO_EXPORT_URI, KEY_PAIRED_DEVICES, KEY_FIRST_LAUNCH)
        return registry.filter { it.key !in excludedKeys }
            .mapValues { it.value.serialize() ?: "" }
    }

    fun importPreferences(data: Map<String, String>) {
        data.forEach { (key, value) ->
            registry[key]?.deserialize(value)
        }
    }

    fun clearAll() {
        prefs.edit { clear() }
        refreshAll()
    }
}