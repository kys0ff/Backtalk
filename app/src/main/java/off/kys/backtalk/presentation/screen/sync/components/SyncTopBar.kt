package off.kys.backtalk.presentation.screen.sync.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import off.kys.backtalk.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncTopBar(
    isDiscovering: Boolean,
    onBackClick: () -> Unit,
    onDiscoveryClick: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(R.string.sync_title)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painterResource(R.drawable.round_arrow_back_24),
                    stringResource(R.string.common_back)
                )
            }
        },
        actions = {
            IconButton(onClick = onDiscoveryClick) {
                Icon(
                    painter = painterResource(if (isDiscovering) R.drawable.round_close_24 else R.drawable.round_refresh_24),
                    contentDescription = stringResource(if (isDiscovering) R.string.sync_stop_discovery else R.string.common_search)
                )
            }
        }
    )
}