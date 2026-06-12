package off.kys.backtalk.presentation.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import off.kys.backtalk.R
import off.kys.backtalk.common.lock.AppLockManager
import off.kys.backtalk.common.lock.BiometricResult
import off.kys.backtalk.common.lock.LocalAppLockManager
import off.kys.backtalk.common.lock.LocalBiometricManager
import off.kys.backtalk.common.lock.setBiometricContent
import off.kys.backtalk.presentation.activity.components.AppLifecycleHandler
import off.kys.backtalk.presentation.activity.components.MainView
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.screen.bug.BugScreen
import off.kys.backtalk.presentation.viewmodel.MainViewModel
import off.kys.backtalk.presentation.viewmodel.MessagesViewModel
import off.kys.backtalk.util.emptyString
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MainViewModel>()
    private val messagesViewModel by viewModel<MessagesViewModel>()

    private var isAuthChecked by mutableStateOf(false)
    private var isAuthenticated by mutableStateOf(false)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

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
            val lifecycleOwner = LocalLifecycleOwner.current
            val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

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

            LaunchedEffect(isCurrentlyUnlocked, lifecycleState, intent) {
                if (lifecycleState == Lifecycle.State.RESUMED) {
                    isAuthenticating = false
                    handleIntent(intent)
                }
                if (preferences.lockEnabled && !isCurrentlyUnlocked && lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
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
                onRetryAuthentication = authenticate,
                crashData = intent.takeIf { it.hasExtra("EXTRA_CRASH_NAME") }?.let {
                    BugScreen(
                        exceptionName = it.getStringExtra("EXTRA_CRASH_NAME") ?: "Unknown",
                        message = it.getStringExtra("EXTRA_CRASH_MESSAGE"),
                        stackTrace = it.getStringExtra("EXTRA_CRASH_STACKTRACE") ?: emptyString(),
                        threadName = it.getStringExtra("EXTRA_CRASH_THREAD") ?: "main"
                    )
                }
            )
        }
    }

    fun checkForUpdates() {
        viewModel.onEvent(MainUiEvent.CheckUpdate(isManual = true))
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND) {
            when {
                intent.type == "text/plain" -> {
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                        messagesViewModel.onEvent(MessagesUiEvent.SetSharedText(sharedText))
                        intent.action = null
                    }
                }
                intent.type?.startsWith("image/") == true -> {
                    IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, android.net.Uri::class.java)?.let { uri ->
                        messagesViewModel.onEvent(MessagesUiEvent.SetSharedImage(uri.toString()))
                        intent.action = null
                    }
                }
            }
        }
    }
}