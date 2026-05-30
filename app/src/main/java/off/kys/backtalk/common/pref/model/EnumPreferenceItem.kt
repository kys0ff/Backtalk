package off.kys.backtalk.common.pref.model

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit

/**
 * Preference item for Enum values.
 */
class EnumPreferenceItem<E : Enum<E>>(
    prefs: SharedPreferences,
    key: String,
    defaultValue: E,
    private val enumClass: Class<E>
) : BasePreferenceItem<E>(prefs, key, defaultValue) {
    override val internalState: MutableState<E> = mutableStateOf(readFromPrefs())

    override fun readFromPrefs(): E {
        val name = prefs.getString(key, defaultValue.name) ?: defaultValue.name
        return try {
            java.lang.Enum.valueOf(enumClass, name)
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    override fun writeToPrefs(value: E) = prefs.edit { putString(key, value.name) }
    override fun serialize(): String = value.name
    override fun deserialize(value: String) {
        this.value = try {
            java.lang.Enum.valueOf(enumClass, value)
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }
}