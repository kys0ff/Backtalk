package off.kys.backtalk.common.pref.model

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit

/**
 * Preference item for Integer values.
 */
class IntPreferenceItem(
    prefs: SharedPreferences,
    key: String,
    defaultValue: Int
) : BasePreferenceItem<Int>(prefs, key, defaultValue) {
    override val internalState: MutableState<Int> = mutableStateOf(readFromPrefs())

    override fun readFromPrefs(): Int = prefs.getInt(key, defaultValue)
    override fun writeToPrefs(value: Int) = prefs.edit { putInt(key, value) }
    override fun serialize(): String = value.toString()
    override fun deserialize(value: String) { this.value = value.toIntOrNull() ?: defaultValue }
}
