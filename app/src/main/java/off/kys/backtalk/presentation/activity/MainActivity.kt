package off.kys.backtalk.presentation.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import off.kys.backtalk.common.base.BaseLockActivity
import off.kys.backtalk.presentation.activity.components.AppUpdateDialog
import off.kys.backtalk.presentation.activity.components.LockedView
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.screen.messages.MessagesScreen
import off.kys.backtalk.presentation.state.MainUiState
import off.kys.backtalk.presentation.theme.BacktalkTheme
import off.kys.backtalk.presentation.viewmodel.MainViewModel
import off.kys.preferences.compose.provider.PreferenceProvider
import off.kys.preferences.compose.provider.rememberPreference
import off.kys.preferences.core.PreferenceKey
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.minutes

class MainActivity : BaseLockActivity() {

    override val autoLockTimeout: Long
        get() = 1.minutes.inWholeMilliseconds

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreferenceProvider {
                val isDarkTheme =
                    rememberPreference(
                        key = PreferenceKey.Switch("dark_mode"),
                        defaultValue = isSystemInDarkTheme()
                    )
                val viewModel = koinViewModel<MainViewModel>()
                val updateState by viewModel.mainUiState.collectAsState()

                BacktalkTheme(isDarkTheme) {
                    Navigator(MessagesScreen()) {
                        if (isLoggedIn) {
                            CurrentScreen()
                        } else {
                            LockedView()
                        }

                        // Trigger update check
                        LaunchedEffect(key1 = Unit) {
                            viewModel.onEvent(MainUiEvent.CheckUpdate)
                        }

                        // Show dialog if needed
                        if (updateState is MainUiState.UpdateAvailable) {
                            val result = (updateState as MainUiState.UpdateAvailable).result
                            AppUpdateDialog(
                                updateResult = result,
                                onDismissRequest = { viewModel.onEvent(MainUiEvent.DismissDialog) },
                                onUpdateClick = { viewModel.onEvent(MainUiEvent.UpdateNow(result.downloadUrls.first().browserDownloadUrl)) }
                            )
                        }
                    }
                }
            }
        }
    }

}