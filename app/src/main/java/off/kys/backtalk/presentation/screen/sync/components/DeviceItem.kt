package off.kys.backtalk.presentation.screen.sync.components

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import off.kys.backtalk.R
import off.kys.backtalk.sync.DeviceInfo

@Composable
fun DeviceItem(device: DeviceInfo, onPairClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
        ),
        onClick = onPairClick
    ) {
        ListItem(
            headlineContent = { Text(device.name) },
            supportingContent = { Text(device.address.orEmpty()) },
            leadingContent = { Icon(painterResource(R.drawable.round_phone_android_24), null) },
            trailingContent = {
                IconButton(onClick = onPairClick) {
                    Icon(painterResource(R.drawable.round_add_link_24), null)
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}