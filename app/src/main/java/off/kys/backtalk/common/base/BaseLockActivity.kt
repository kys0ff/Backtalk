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

abstract class BaseLockActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    protected open val autoLockEnabled: Boolean = true
    protected abstract val autoLockTimeout: Long
    protected open var lockOnCreateEnabled: Boolean = true

    protected open val isAnonymousMode: Boolean = true

    private val lockRunnable = Runnable { lockApp() }

    private val executor by lazy { ContextCompat.getMainExecutor(this) }

    protected var isLoggedIn by mutableStateOf(false)
        private set

    private var isBiometricVisible = false

    private val lifecycleEventObserver = LifecycleEventObserver { _, event ->
        if (!autoLockEnabled) return@LifecycleEventObserver

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

        if (lockOnCreateEnabled.not()) {
            isLoggedIn = true
            isBiometricVisible = false
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleEventObserver)
    }

    protected open fun getBiometricTitle(): String = getString(R.string.is_that_you)
    protected open fun getBiometricSubtitle(): String = getString(R.string.log_in_using_your_biometric_credential)

    protected open fun onAuthenticated() {}
    protected open fun onAuthenticationFailed(errorCode: Int, errString: CharSequence) {
        finish()
    }

    protected fun tryShowBiometric() = CoroutineScope(Dispatchers.Main).launch {
        delay(100L)
        if (!isLoggedIn && !isBiometricVisible) {
            isBiometricVisible = true
            biometricPrompt.authenticate(promptInfo)
        }
    }

    protected fun lockApp() {
        isLoggedIn = false
    }
}
