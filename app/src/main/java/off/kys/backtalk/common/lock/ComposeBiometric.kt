package off.kys.backtalk.common.lock

import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import off.kys.backtalk.R
import org.koin.compose.koinInject

val LocalBiometricManager = staticCompositionLocalOf<BiometricPromptManager> {
    error("BiometricPromptManager not provided. Did you use setBiometricContent?")
}

val LocalAppLockManager = staticCompositionLocalOf<AppLockManager> {
    error("AppLockManager not provided. Did you use setBiometricContent?")
}

@Composable
fun rememberBiometricLauncher(
    onResult: (BiometricResult) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: error("Context must be FragmentActivity")
    val manager = remember(activity) { BiometricPromptManager(activity) }

    return {
        manager.authenticate(
            titleRes = R.string.auth_title,
            subtitleRes = R.string.auth_subtitle,
            onResult = onResult
        )
    }
}

fun FragmentActivity.setBiometricContent(
    content: @Composable () -> Unit
) {
    setContent {
        val biometricManager = remember(this) { BiometricPromptManager(this) }
        val appLockManager = koinInject<AppLockManager>()

        CompositionLocalProvider(
            LocalBiometricManager provides biometricManager,
            LocalAppLockManager provides appLockManager
        ) {
            content()
        }
    }
}