package off.kys.backtalk.presentation.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import off.kys.backtalk.R
import off.kys.backtalk.common.lock.AppLockManager
import off.kys.backtalk.common.lock.BiometricResult
import off.kys.backtalk.common.lock.LocalAppLockManager
import off.kys.backtalk.common.lock.LocalBiometricManager
import off.kys.backtalk.common.lock.setBiometricContent
import off.kys.backtalk.presentation.activity.components.AppLifecycleHandler
import off.kys.backtalk.presentation.activity.components.MainView
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.viewmodel.MainViewModel
import off.kys.backtalk.presentation.viewmodel.MessagesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MainViewModel>()
    private val messagesViewModel by viewModel<MessagesViewModel>()

    private var isAuthChecked by mutableStateOf(false)
    private var isAuthenticated by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        val preferences = viewModel.preferences
        val messagesUiState by messagesViewModel.uiState

        isAuthenticated = !preferences.lockEnabled
        isAuthChecked = !preferences.lockEnabled

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !isAuthChecked || (isAuthenticated && messagesUiState.isLoading)
        }

        enableEdgeToEdge()

        setBiometricContent {
            val biometricManager = LocalBiometricManager.current
            val appLockManager = LocalAppLockManager.current

            val unlockedKeys by appLockManager.unlockedKeys.collectAsState()
            val isCurrentlyUnlocked = !preferences.lockEnabled || appLockManager.isUnlocked(AppLockManager.Keys.MAIN)

            var isAuthenticating by remember { mutableStateOf(false) }

            val authenticate = remember(biometricManager, appLockManager) {
                {
                    if (!isAuthenticating) {
                        isAuthenticating = true
                        biometricManager.authenticate(
                            titleRes = R.string.auth_title,
                            subtitleRes = R.string.auth_subtitle,
                        ) { result ->
                            isAuthenticating = false
                            isAuthChecked = true
                            if (result is BiometricResult.Success) {
                                appLockManager.setUnlocked(AppLockManager.Keys.MAIN)
                            }
                        }
                    }
                }
            }

            LaunchedEffect(isCurrentlyUnlocked, unlockedKeys) {
                isAuthenticated = isCurrentlyUnlocked
                if (!preferences.lockEnabled || isCurrentlyUnlocked) {
                    isAuthChecked = true
                }
            }

            LaunchedEffect(isCurrentlyUnlocked) {
                if (preferences.lockEnabled && !isCurrentlyUnlocked) {
                    authenticate()
                }
            }

            AppLifecycleHandler(
                prefs = preferences,
                window = window
            )

            MainView(
                viewModel = viewModel,
                isAuthenticated = isCurrentlyUnlocked,
                onRetryAuthentication = authenticate
            )
        }
    }

    fun checkForUpdates() {
        viewModel.onEvent(MainUiEvent.CheckUpdate)
    }
}