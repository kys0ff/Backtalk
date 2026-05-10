package off.kys.backtalk.common.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import off.kys.backtalk.R

/**
 * A base activity that provides automatic locking and biometric authentication capabilities.
 * It also supports "anonymous mode" which prevents screenshots and screen recordings.
 */
abstract class BaseLockActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    /**
     * Whether the application should automatically lock when moved to the background.
     */
    protected open val autoLockEnabled: Boolean = true

    /**
     * The delay in milliseconds before the application locks after being moved to the background.
     */
    protected abstract val autoLockTimeout: Long

    /**
     * Whether authentication is required for this activity.
     */
    protected open var isAuthRequired: Boolean = true

    /**
     * Whether anonymous mode is enabled, disabling screenshots and screen recordings.
     */
    protected open var isAnonymousMode: Boolean = true

    private val lockRunnable = Runnable { lockApp() }

    private val executor by lazy { ContextCompat.getMainExecutor(this) }

    /**
     * The current login state of the user.
     */
    protected var isLoggedIn by mutableStateOf(false)
        private set

    private var isBiometricVisible = false

    private val lifecycleEventObserver = LifecycleEventObserver { _, event ->
        if (!autoLockEnabled) return@LifecycleEventObserver
        if (!isAuthRequired) return@LifecycleEventObserver

        when (event) {
            Lifecycle.Event.ON_STOP -> {
                handler.postDelayed(lockRunnable, autoLockTimeout)
            }
            Lifecycle.Event.ON_START -> {
                handler.removeCallbacks(lockRunnable)
                tryShowBiometric()
            }
            else -> Unit
        }
    }


    private val biometricPrompt by lazy {
        BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                isLoggedIn = true
                isBiometricVisible = false
                onAuthenticated()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                isBiometricVisible = false
                onAuthenticationFailed(errorCode, errString)
            }
        })
    }

    private val promptInfo by lazy {
        BiometricPrompt.PromptInfo.Builder()
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .setTitle(getBiometricTitle())
            .setSubtitle(getBiometricSubtitle())
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isAnonymousMode) {
            // Disable screenshots and screen recordings
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        if (isAuthRequired.not()) {
            isLoggedIn = true
            isBiometricVisible = false
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleEventObserver)
    }

    /**
     * Returns the title for the biometric prompt.
     */
    protected open fun getBiometricTitle(): String = getString(R.string.auth_title)

    /**
     * Returns the subtitle for the biometric prompt.
     */
    protected open fun getBiometricSubtitle(): String = getString(R.string.auth_subtitle)

    /**
     * Called when the user has successfully authenticated.
     */
    protected open fun onAuthenticated() {}

    /**
     * Called when biometric authentication fails or is canceled.
     * Default implementation finishes the activity.
     */
    protected open fun onAuthenticationFailed(errorCode: Int, errString: CharSequence) {
        finishAffinity()
    }

    /**
     * Attempts to show the biometric authentication prompt if the user is not logged in.
     */
    protected fun tryShowBiometric() = CoroutineScope(Dispatchers.Main).launch {
        delay(100L)
        if (!isLoggedIn && !isBiometricVisible) {
            isBiometricVisible = true
            biometricPrompt.authenticate(promptInfo)
        }
    }

    /**
     * Manually locks the application.
     */
    protected fun lockApp() {
        isLoggedIn = false
    }

    /**
     * Updates the anonymous mode state and applies the corresponding system flags.
     * @param enabled True to enable anonymous mode (disable screenshots), false otherwise.
     */
    protected fun updateSystemFlags(enabled: Boolean) {
        isAnonymousMode = enabled
        if (isAnonymousMode) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
