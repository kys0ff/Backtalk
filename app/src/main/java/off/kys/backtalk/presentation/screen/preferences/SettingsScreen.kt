package off.kys.backtalk.presentation.screen.preferences

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.backtalk.presentation.activity.MainActivity
import off.kys.backtalk.presentation.components.SplitThemeContainer
import off.kys.backtalk.presentation.screen.bug.BugScreen
import off.kys.backtalk.presentation.screen.license.LicenseScreen
import off.kys.backtalk.presentation.screen.preferences.components.SettingsScreenContent
import off.kys.backtalk.presentation.screen.sync.SyncScreen
import off.kys.backtalk.presentation.state.SettingsUiState
import off.kys.backtalk.presentation.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * The settings screen of the application.
 */
class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<SettingsViewModel>()
        val state by viewModel.state.collectAsState(SettingsUiState.empty())
        val navigator = LocalNavigator.currentOrThrow
        val mainActivity = LocalActivity.current as? MainActivity

        SettingsScreenContent(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateBack = { navigator.pop() },
            onSyncClicked = { navigator.push(SyncScreen()) },
            onLicenseClicked = { navigator.push(LicenseScreen()) },
            onBugScreenClicked = {
                throw Exception("This is a sample bug report for demonstration.")

            },
            onCheckUpdates = { mainActivity?.checkForUpdates() }
        )
    }
}

@Preview(
    showSystemUi = true,
    device = "id:pixel_10",
)
@Composable
private fun SettingsScreenPreview() {
    SplitThemeContainer {
        SettingsScreenContent(
            state = SettingsUiState(),
            onEvent = {},
            onNavigateBack = {},
            onSyncClicked = {},
            onLicenseClicked = {},
            onBugScreenClicked = {},
            onCheckUpdates = {}
        )
    }
}
