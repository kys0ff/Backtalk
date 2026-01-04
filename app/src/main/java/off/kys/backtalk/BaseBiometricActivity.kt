package off.kys.backtalk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

abstract class BaseBiometricActivity : AppCompatActivity() {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    // Track background time
    private var lastTimestamp: Long = 0
    private val lockoutDuration = 30_000 // 30 seconds timeout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBiometricPrompt()
    }

    override fun onResume() {
        super.onResume()
        // Check if app was in background longer than timeout
        if (lastTimestamp != 0L && System.currentTimeMillis() - lastTimestamp > lockoutDuration) {
            showBiometricPrompt()
        } else if (lastTimestamp == 0L) {
            // First launch
            showBiometricPrompt()
        }
    }

    override fun onPause() {
        super.onPause()
        lastTimestamp = System.currentTimeMillis()
    }

    private fun setupBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Logic for successful login
                    onAuthSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Handle errors (e.g., user cancelled or no hardware)
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        finish() // Close app if they cancel the mandatory login
                    }
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.is_that_you))
            .setSubtitle(getString(R.string.log_in_using_your_biometric_credential))
            .setNegativeButtonText(getString(R.string.exit_app))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    }

    fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> biometricPrompt.authenticate(promptInfo)
            else -> onAuthSuccess() // Fallback if biometrics aren't available
        }
    }

    open fun onAuthSuccess() {
        // Reset timestamp so it doesn't prompt again immediately
        lastTimestamp = System.currentTimeMillis()
    }
}