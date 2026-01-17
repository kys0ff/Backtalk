package off.kys.preferences.compose.ui.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import off.kys.preferences.data.PreferenceManager
import off.kys.preferences.model.PreferenceItem

private const val TAG = "ListPreference"

@Composable
fun ListPreference(
    preferenceManager: PreferenceManager,
    item: PreferenceItem.List
) {
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentValue by preferenceManager.getPreference(item.key.toPreferencesKey(), item.defaultValue)
        .collectAsState(initial = item.defaultValue)

    var selectedValue by remember { mutableStateOf(currentValue) }

    LaunchedEffect(key1 = currentValue) {
        selectedValue = currentValue
    }

    val currentDisplay =
        item.entries.entries.find { it.value == selectedValue }?.key ?: "Select"

    ListItem(
        modifier = Modifier.clickable { showDialog = true },
        headlineContent = { Text(item.title) },
        supportingContent = { Text(currentDisplay) }, // Show current selection
        leadingContent = item.icon?.let { { Icon(painter = painterResource(it), contentDescription = null) } }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(item.title) },
            text = {
                Column {
                    item.entries.forEach { (displayName, storedValue) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedValue = storedValue
                                    scope.launch {
                                        preferenceManager.setPreference(
                                            key = item.key.toPreferencesKey(),
                                            value = storedValue
                                        )
                                    }
                                    showDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (storedValue == currentValue),
                                onClick = null // Handled by Row
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}