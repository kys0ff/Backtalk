package off.kys.preferences.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import off.kys.preferences.core.PreferenceKey

@Composable
fun <T> rememberPreference(key: PreferenceKey, defaultValue: T): T {
    val manager = LocalPreferenceManager.current
    // Collect the flow as a state and return the value
    return manager.getPreference(key = key.toPreferencesKey(), defaultValue = defaultValue).collectAsState(initial = defaultValue).value
}