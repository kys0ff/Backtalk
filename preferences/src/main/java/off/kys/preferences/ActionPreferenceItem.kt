package off.kys.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ActionPreferenceItem(
    item: PreferenceItem.Action
) {
    ListItem(
        modifier = Modifier.clickable { item.onClick?.let { it() } },
        headlineContent = { Text(item.title) },
        supportingContent = item.summary?.let { { Text(it) } },
        leadingContent = item.icon?.let {
            {
                Icon(
                    painter = it,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}