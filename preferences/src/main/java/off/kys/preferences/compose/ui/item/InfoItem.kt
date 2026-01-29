package off.kys.preferences.compose.ui.item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.preferences.R

@Composable
fun InfoItem(@StringRes infoRes: Int) {
    ListItem(
        headlineContent = {
            Box(Modifier.padding(bottom = 16.dp)) {
                Icon(
                    painter = painterResource(R.drawable.round_info_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        supportingContent = {
            Text(stringResource(infoRes))
        }
    )
}