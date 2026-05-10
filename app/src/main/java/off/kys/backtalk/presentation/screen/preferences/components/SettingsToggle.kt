package off.kys.backtalk.presentation.screen.preferences.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SettingsToggle(
    label: String,
    supportingText: String? = null,
    icon: Painter,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onCheckedChange(!checked) },
        headlineContent = { Text(label, fontWeight = FontWeight.Medium) },
        supportingContent = supportingText?.let { { Text(it) } },
        leadingContent = { SettingsIcon(icon) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}