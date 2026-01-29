package off.kys.preferences.compose.ui.item

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import off.kys.preferences.data.PreferenceManager
import off.kys.preferences.model.PreferenceItem
import off.kys.preferences.util.getPreferenceContentColorByEnabled

@Composable
fun SwitchPreference(
    preferenceManager: PreferenceManager,
    item: PreferenceItem.Switch
) {
    val scope = rememberCoroutineScope()
    val value by preferenceManager.getPreference(item.key.toPreferencesKey(), item.defaultValue)
        .collectAsState(initial = item.defaultValue)


    ListItem(
        modifier = Modifier.clickable(enabled = item.enabled) {
            scope.launch { preferenceManager.setPreference(item.key.toPreferencesKey(), !value) }
        },
        headlineContent = {
            Text(
                stringResource(item.titleRes),
                color = getPreferenceContentColorByEnabled(item.enabled)
            )
        },
        supportingContent = item.summaryRes?.let {
            {
                Text(
                    text = stringResource(it),
                    color = getPreferenceContentColorByEnabled(item.enabled)
                )
            }
        },
        leadingContent = item.iconRes?.let {
            {
                Icon(
                    painterResource(it),
                    contentDescription = null,
                    tint = getPreferenceContentColorByEnabled(item.enabled)
                )
            }
        },
        trailingContent = {
            Switch(
                enabled = item.enabled,
                checked = value,
                onCheckedChange = { newValue ->
                    scope.launch {
                        preferenceManager.setPreference(
                            item.key.toPreferencesKey(),
                            newValue
                        )
                    }
                }
            )
        }
    )
}