package off.kys.backtalk.common.pref.model

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit

/**
 * Preference item for Boolean values.
 */
class BooleanPreferenceItem(
    prefs: SharedPreferences,
    key: String,
    defaultValue: Boolean
) : BasePreferenceItem<Boolean>(prefs, key, defaultValue) {
    override val internalState: MutableState<Boolean> = mutableStateOf(readFromPrefs())

    override fun readFromPrefs(): Boolean = prefs.getBoolean(key, defaultValue)
    override fun writeToPrefs(value: Boolean) = prefs.edit { putBoolean(key, value) }
    override fun serialize(): String = value.toString()
    override fun deserialize(value: String) { this.value = value.toBoolean() }
}