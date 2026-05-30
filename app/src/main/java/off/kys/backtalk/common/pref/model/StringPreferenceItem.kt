package off.kys.backtalk.common.pref.model

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit

/**
 * Preference item for String values.
 */
class StringPreferenceItem(
    prefs: SharedPreferences,
    key: String,
    defaultValue: String?
) : BasePreferenceItem<String?>(prefs, key, defaultValue) {
    override val internalState: MutableState<String?> = mutableStateOf(readFromPrefs())

    override fun readFromPrefs(): String? = prefs.getString(key, defaultValue)
    override fun writeToPrefs(value: String?) = prefs.edit { putString(key, value) }
    override fun serialize(): String? = value
    override fun deserialize(value: String) { this.value = value }
}