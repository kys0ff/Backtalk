package off.kys.backtalk.presentation.screen.preferences.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsItem(
    label: String,
    value: String? = null,
    icon: Painter,
    badge: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(label, fontWeight = FontWeight.Medium)
                badge?.invoke()
            }
        },
        supportingContent = value?.let {
            {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        leadingContent = { SettingsIcon(icon) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}