package off.kys.preferences.core

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

sealed class PreferenceKey {
    data class Action(val title: String) : PreferenceKey()
    data class Switch(val title: String) : PreferenceKey()
    data class Slider(val title: String) : PreferenceKey()
    data class List(val title: String) : PreferenceKey()

    @Suppress("UNCHECKED_CAST")
    internal fun <T> toPreferencesKey(): Preferences.Key<T> = when (this) {
        is Action -> stringPreferencesKey(title)
        is Switch -> booleanPreferencesKey(title)
        is Slider -> floatPreferencesKey(title)
        is List -> stringPreferencesKey(title)
    } as Preferences.Key<T>
}