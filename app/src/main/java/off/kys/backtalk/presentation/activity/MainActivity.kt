package off.kys.backtalk.presentation.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import kotlinx.coroutines.launch
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.common.base.BaseLockActivity
import off.kys.backtalk.presentation.activity.components.AppUpdateDialog
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.screen.messages.MessagesScreen
import off.kys.backtalk.presentation.state.MainUiState
import off.kys.backtalk.presentation.theme.BacktalkTheme
import off.kys.backtalk.presentation.viewmodel.MainViewModel
import off.kys.backtalk.presentation.viewmodel.MessagesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.time.Duration.Companion.minutes

class MainActivity : BaseLockActivity() {

    private val viewModel by viewModel<MainViewModel>()
    private val messagesViewModel by viewModel<MessagesViewModel>()

    override var autoLockTimeout: Long = 1.minutes.inWholeMilliseconds
    override var isAuthRequired: Boolean = true
    override var isAnonymousMode: Boolean = true

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        isAuthRequired = viewModel.preferences.lockEnabled
        isAnonymousMode = viewModel.preferences.secureScreenEnabled
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !isLoggedIn || messagesViewModel.uiState.value.isLoading
        }

        enableEdgeToEdge()
        setContent {
            val isDarkTheme = viewModel.preferences.themeMode.isDark(isSystemInDarkTheme())
            val dynamicColor = viewModel.preferences.dynamicColorEnabled
            val updateState by viewModel.mainUiState.collectAsStateWithLifecycle()

            LaunchedEffect(viewModel.preferences.secureScreenEnabled) {
                val enabled = viewModel.preferences.secureScreenEnabled
                isAnonymousMode = enabled
                updateSystemFlags(enabled)
            }

            LaunchedEffect(Unit) {
                if (!BuildConfig.IS_FDROID) {
                    viewModel.onEvent(MainUiEvent.CheckUpdate)
                }
            }

            BacktalkTheme(
                darkTheme = isDarkTheme,
                dynamicColor = dynamicColor
            ) {
                Navigator(MessagesScreen()) { navigator ->
                    SlideTransition(navigator)
                }

                (updateState as? MainUiState.UpdateAvailable)?.let { state ->
                    val url =
                        state.result.downloadUrls.firstOrNull()?.browserDownloadUrl ?: return@let
                    AppUpdateDialog(
                        updateResult = state.result,
                        onDismissRequest = { viewModel.onEvent(MainUiEvent.DismissDialog) },
                        onUpdateClick = { viewModel.onEvent(MainUiEvent.UpdateNow(url)) }
                    )
                }
            }
        }
    }

    fun checkForUpdates() {
        lifecycleScope.launch {
            viewModel.onEvent(MainUiEvent.CheckUpdate)
        }
    }

}