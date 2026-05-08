package off.kys.backtalk.presentation.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import kotlinx.coroutines.launch
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.common.base.BaseLockActivity
import off.kys.backtalk.presentation.activity.components.AppUpdateDialog
import off.kys.backtalk.presentation.activity.components.LockedView
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.screen.messages.MessagesScreen
import off.kys.backtalk.presentation.state.MainUiState
import off.kys.backtalk.presentation.theme.BacktalkTheme
import off.kys.backtalk.presentation.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.time.Duration.Companion.minutes

class MainActivity : BaseLockActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override var autoLockTimeout: Long = 1.minutes.inWholeMilliseconds
    override var isAuthRequired: Boolean = true
    override var isAnonymousMode: Boolean = true

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        isAuthRequired = viewModel.preferences.lockEnabled
        isAnonymousMode = viewModel.preferences.secureScreenEnabled
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme =
                viewModel.preferences.themeMode == ThemeMode.DARK || (viewModel.preferences.themeMode == ThemeMode.AUTO && isSystemInDarkTheme())
            val dynamicColor = viewModel.preferences.dynamicColorEnabled
            val updateState by viewModel.mainUiState.collectAsState()

            LaunchedEffect(viewModel.preferences.secureScreenEnabled) {
                val enabled = viewModel.preferences.secureScreenEnabled
                isAnonymousMode = enabled
                updateSystemFlags(enabled)
            }

            BacktalkTheme(
                darkTheme = isDarkTheme,
                dynamicColor = dynamicColor
            ) {
                Navigator(MessagesScreen()) { navigator ->
                    if (isLoggedIn) {
                        SlideTransition(navigator)
                    } else {
                        LockedView()
                    }

                    if (updateState is MainUiState.UpdateAvailable) {
                        if (!BuildConfig.IS_FDROID) {
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.preferences.unregisterObserver()
    }

    fun checkForUpdates() {
        lifecycleScope.launch {
            viewModel.onEvent(MainUiEvent.CheckUpdate)
        }
    }

}