package off.kys.backtalk.presentation.activity.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.screen.bug.BugScreen
import off.kys.backtalk.presentation.screen.messages.MessagesScreen
import off.kys.backtalk.presentation.screen.onboarding.OnboardingScreen
import off.kys.backtalk.presentation.state.MainUiState
import off.kys.backtalk.presentation.theme.BacktalkTheme
import off.kys.backtalk.presentation.viewmodel.MainViewModel

@Composable
fun MainView(
    viewModel: MainViewModel,
    isAuthenticated: Boolean,
    onRetryAuthentication: () -> Unit = {},
    crashData: BugScreen? = null
) {
    val updateState by viewModel.mainUiState.collectAsStateWithLifecycle()
    val isDarkTheme = viewModel.preferences.themeMode.isDark(isSystemInDarkTheme())
    val dynamicColor = viewModel.preferences.dynamicColorEnabled

    BacktalkTheme(
        darkTheme = isDarkTheme,
        dynamicColor = dynamicColor
    ) {
        if (crashData != null) {
            Navigator(crashData) { navigator ->
                SlideTransition(navigator)
            }
        } else {
            Crossfade(targetState = isAuthenticated, label = "LoginState") { loggedIn ->
                if (loggedIn) {
                    val initialScreen = remember {
                        if (viewModel.preferences.firstLaunch) OnboardingScreen() else MessagesScreen()
                    }
                    Navigator(initialScreen) { navigator ->
                        SlideTransition(navigator)
                    }
                } else {
                    LockView(onRetryAuthentication = onRetryAuthentication)
                }
            }
        }

        if (!viewModel.preferences.firstLaunch) {
            (updateState as? MainUiState.UpdateAvailable)?.let { state ->
                val url = state.result.downloadUrls.firstOrNull()?.browserDownloadUrl
                    ?: return@let
                AppUpdateDialog(
                    updateResult = state.result,
                    onDismissRequest = { viewModel.onEvent(MainUiEvent.DismissDialog) },
                    onUpdateClick = { viewModel.onEvent(MainUiEvent.UpdateNow(url)) }
                )
            }
        }
    }
}
