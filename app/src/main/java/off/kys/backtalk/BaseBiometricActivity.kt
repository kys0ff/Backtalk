package off.kys.backtalk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

abstract class BaseBiometricActivity : AppCompatActivity() {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    // Flag to keep the splash screen visible
    protected var isReady = false

    private var lastTimestamp: Long = 0
    private val lockoutDuration = 30_000

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the splash screen visible until isReady is true
        splashScreen.setKeepOnScreenCondition { !isReady }

        setupBiometricPrompt()
    }

    override fun onResume() {
        super.onResume()
        val timeElapsed = System.currentTimeMillis() - lastTimestamp

        if (lastTimestamp == 0L || timeElapsed > lockoutDuration) {
            // App is launching or timed out
            isReady = false
            showBiometricPrompt()
        }
    }

    override fun onPause() {
        super.onPause()
        lastTimestamp = System.currentTimeMillis()
    }

    private fun setupBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user cancels or closes the prompt, exit the app
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        finish()
                    }
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.is_that_you))
            .setSubtitle(getString(R.string.log_in_using_your_biometric_credential))
            .setNegativeButtonText(getString(R.string.exit_app))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    }

    private fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            == BiometricManager.BIOMETRIC_SUCCESS
        ) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            // Fallback: No biometrics enrolled/supported
            isReady = true
        }
    }

    open fun onAuthSuccess() {
        // Stop showing the splash screen
        isReady = true
        lastTimestamp = System.currentTimeMillis()
    }
}