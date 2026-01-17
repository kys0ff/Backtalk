package off.kys.preferences.compose.ui.item

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import off.kys.preferences.data.PreferenceManager
import off.kys.preferences.model.PreferenceItem
import java.util.Locale

@Composable
fun SliderPreferenceItem(
    preferenceManager: PreferenceManager,
    item: PreferenceItem.Slider
) {
    val scope = rememberCoroutineScope()
    // Read value from DataStore
    val value by preferenceManager.getPreference(item.key.toPreferencesKey(), item.defaultValue)
        .collectAsState(initial = item.defaultValue)

    ListItem(
        headlineContent = { Text(stringResource(item.titleRes)) },
        leadingContent = item.iconRes?.let { { Icon(painterResource(it), contentDescription = null) } },
        supportingContent = {
            Column {
                // Display the current value formatted nicely
                Text(text = String.format(Locale.getDefault(), "%.1f", value))

                Slider(
                    value = value,
                    onValueChange = { newValue ->
                        // Instant update to DataStore
                        scope.launch { preferenceManager.setPreference(item.key.toPreferencesKey(), newValue) }
                    },
                    valueRange = item.valueRange,
                    steps = item.steps
                )
            }
        }
    )
}