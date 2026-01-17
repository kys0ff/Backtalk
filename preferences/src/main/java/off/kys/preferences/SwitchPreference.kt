package off.kys.preferences

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun SwitchPreference(
    preferenceManager: PreferenceManager,
    item: PreferenceItem.Switch
) {
    val scope = rememberCoroutineScope()
    val value by preferenceManager.getPreference(item.key.toPreferencesKey(), item.defaultValue).collectAsState(initial = item.defaultValue)

    
    ListItem(
        modifier = Modifier.clickable {
            scope.launch { preferenceManager.setPreference(item.key.toPreferencesKey(), !value) }
        },
        headlineContent = { Text(item.title) },
        supportingContent = item.summary?.let { { Text(it) } },
        leadingContent = item.icon?.let { { Icon(it, contentDescription = null) } },
        trailingContent = {
            Switch(
                checked = value,
                onCheckedChange = { newValue ->
                    scope.launch { preferenceManager.setPreference(item.key.toPreferencesKey(), newValue) }
                }
            )
        }
    )
}