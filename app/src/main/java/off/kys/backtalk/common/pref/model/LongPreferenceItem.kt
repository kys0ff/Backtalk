package off.kys.backtalk.common.pref.model

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit

/**
 * Preference item for Long values.
 */
class LongPreferenceItem(
    prefs: SharedPreferences,
    key: String,
    defaultValue: Long
) : BasePreferenceItem<Long>(prefs, key, defaultValue) {
    override val internalState: MutableState<Long> = mutableStateOf(readFromPrefs())

    override fun readFromPrefs(): Long = prefs.getLong(key, defaultValue)
    override fun writeToPrefs(value: Long) = prefs.edit { putLong(key, value) }
    override fun serialize(): String = value.toString()
    override fun deserialize(value: String) { this.value = value.toLongOrNull() ?: defaultValue }
}
