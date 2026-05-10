package off.kys.backtalk.presentation.screen.preferences.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import off.kys.backtalk.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopAppBar(
    onNavigateBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeTopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(R.drawable.round_arrow_back_24),
                    contentDescription = stringResource(R.string.common_navigate_up)
                )
            }
        },
        title = { Text(text = stringResource(R.string.settings_title)) },
        scrollBehavior = scrollBehavior
    )
}