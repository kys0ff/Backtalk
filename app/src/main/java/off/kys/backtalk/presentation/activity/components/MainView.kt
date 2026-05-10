package off.kys.backtalk.presentation.activity.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.screen.messages.MessagesScreen
import off.kys.backtalk.presentation.screen.onboarding.OnboardingScreen
import off.kys.backtalk.presentation.state.MainUiState
import off.kys.backtalk.presentation.theme.BacktalkTheme
import off.kys.backtalk.presentation.viewmodel.MainViewModel

@Composable
fun MainView(
    viewModel: MainViewModel,
    isLoggedIn: Boolean
) {
    val updateState by viewModel.mainUiState.collectAsStateWithLifecycle()
    val isDarkTheme = viewModel.preferences.themeMode.isDark(isSystemInDarkTheme())
    val dynamicColor = viewModel.preferences.dynamicColorEnabled

    var showOnboarding by remember { mutableStateOf(viewModel.preferences.firstLaunch) }

    LaunchedEffect(Unit) {
        if (!BuildConfig.IS_FDROID) {
            viewModel.onEvent(MainUiEvent.CheckUpdate)
        }
    }

    BacktalkTheme(
        darkTheme = isDarkTheme,
        dynamicColor = dynamicColor
    ) {
        if (showOnboarding) {
            OnboardingScreen(
                onFinished = {
                    viewModel.preferences.firstLaunch = false
                    showOnboarding = false
                }
            )
        } else {
            Crossfade(targetState = isLoggedIn, label = "LoginState") { loggedIn ->
                if (loggedIn) {
                    Navigator(MessagesScreen()) { navigator ->
                        SlideTransition(navigator)
                    }

                    (updateState as? MainUiState.UpdateAvailable)?.let { state ->
                        val url = state.result.downloadUrls.firstOrNull()?.browserDownloadUrl
                            ?: return@let
                        AppUpdateDialog(
                            updateResult = state.result,
                            onDismissRequest = { viewModel.onEvent(MainUiEvent.DismissDialog) },
                            onUpdateClick = { viewModel.onEvent(MainUiEvent.UpdateNow(url)) }
                        )
                    }
                } else {
                    LockView()
                }
            }
        }
    }
}
