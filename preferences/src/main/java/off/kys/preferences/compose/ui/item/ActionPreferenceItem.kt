package off.kys.preferences.compose.ui.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.preferences.model.PreferenceItem
import off.kys.preferences.util.getPreferenceContentColorByEnabled

@Composable
fun ActionPreferenceItem(
    item: PreferenceItem.Action
) {
    ListItem(
        modifier = Modifier.clickable(enabled = item.enabled) { item.onClick?.let { it() } },
        headlineContent = {
            Text(
                text = stringResource(item.titleRes),
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
                    painter = painterResource(it),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = getPreferenceContentColorByEnabled(item.enabled)
                )
            }
        }
    )
}