package off.kys.backtalk.common.lock

import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.fragment.app.FragmentActivity

val LocalBiometricManager = staticCompositionLocalOf<BiometricPromptManager> {
    error("BiometricPromptManager not provided. Did you use setBiometricContent?")
}

val LocalAppLockManager = staticCompositionLocalOf<AppLockManager> {
    error("AppLockManager not provided. Did you use setBiometricContent?")
}

// Global instance to survive activity recreation. Use a DI framework in production.
private val globalAppLockManager by lazy { AppLockManager() }

fun FragmentActivity.setBiometricContent(
    content: @Composable () -> Unit
) {
    setContent {
        val biometricManager = remember(this) { BiometricPromptManager(this) }

        CompositionLocalProvider(
            LocalBiometricManager provides biometricManager,
            LocalAppLockManager provides globalAppLockManager
        ) {
            content()
        }
    }
}