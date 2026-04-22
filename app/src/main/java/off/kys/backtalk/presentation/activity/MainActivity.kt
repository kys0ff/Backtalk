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
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.launch
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.common.base.BaseLockActivity
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.presentation.activity.components.AppUpdateDialog
import off.kys.backtalk.presentation.activity.components.LockedView
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.screen.messages.MessagesScreen
import off.kys.backtalk.presentation.state.MainUiState
import off.kys.backtalk.presentation.theme.BacktalkTheme
import off.kys.backtalk.presentation.viewmodel.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.time.Duration.Companion.minutes

class MainActivity : BaseLockActivity() {

    private val viewModel by viewModel<MainViewModel>()
    private val preferences by inject<BacktalkPreferences>()

    override var autoLockTimeout: Long = 1.minutes.inWholeMilliseconds
    override var lockOnCreateEnabled: Boolean = preferences.lockEnabled
    override var isAnonymousMode: Boolean = preferences.secureScreenEnabled

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observePreferences()
        enableEdgeToEdge()
        setContent {
            val isDarkTheme =
                preferences.themeMode == ThemeMode.DARK || (preferences.themeMode == ThemeMode.AUTO && isSystemInDarkTheme())
            val dynamicColor = preferences.dynamicColorEnabled
            val autoUpdateEnabled = preferences.autoUpdateEnabled
            val updateState by viewModel.mainUiState.collectAsState()

            BacktalkTheme(
                darkTheme = isDarkTheme,
                dynamicColor = dynamicColor
            ) {
                Navigator(MessagesScreen()) {
                    if (isLoggedIn) {
                        CurrentScreen()
                    } else {
                        LockedView()
                    }

                    LaunchedEffect(key1 = Unit) {
                        if (autoUpdateEnabled) {
                            viewModel.onEvent(MainUiEvent.CheckUpdate)
                        }
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
        preferences.unregisterObserver()
    }

    fun checkForUpdates() {
        lifecycleScope.launch {
            viewModel.onEvent(MainUiEvent.CheckUpdate)
        }
    }

    private fun observePreferences() {
        lifecycleScope.launch {
            launch {
                preferences.observeChanges { key ->
                    when (key) {
                        BacktalkPreferences.KEY_DYNAMIC_COLOR -> recreate()
                        BacktalkPreferences.KEY_THEME_MODE -> recreate()
                        BacktalkPreferences.KEY_SECURE_SCREEN -> {
                            val enabled = preferences.secureScreenEnabled
                            isAnonymousMode = enabled
                            updateSystemFlags(enabled)
                        }
                    }
                }
            }
        }
    }

}