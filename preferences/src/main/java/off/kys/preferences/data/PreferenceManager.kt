package off.kys.preferences.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferenceManager(private val context: Context) {

    // Generic function to get a preference flow
    fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }

    // Generic function to save a preference
    suspend fun <T> setPreference(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}