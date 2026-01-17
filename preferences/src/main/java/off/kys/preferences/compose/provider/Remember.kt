package off.kys.preferences.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.core.Preferences

@Composable
fun <T> rememberPreference(key: Preferences.Key<T>, defaultValue: T): T {
    val manager = LocalPreferenceManager.current
    // Collect the flow as a state and return the value
    return manager.getPreference(key = key, defaultValue = defaultValue).collectAsState(initial = defaultValue).value
}