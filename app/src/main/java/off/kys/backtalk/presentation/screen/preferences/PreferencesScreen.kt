package off.kys.backtalk.presentation.screen.preferences

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.R
import off.kys.backtalk.common.PreferenceKeys
import off.kys.backtalk.common.base.BaseLockActivity
import off.kys.backtalk.util.copyToClipboard
import off.kys.backtalk.util.isBiometricAvailable
import off.kys.preferences.compose.ui.screen.PreferenceScreen

class PreferencesScreen : Screen {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val isSystemInDarkTheme = isSystemInDarkTheme()
        val activity = LocalActivity.current as BaseLockActivity

        PreferenceScreen {
            PreferenceCategory(
                titleRes = R.string.general,
                descriptionRes = R.string.theme_language,
                iconRes = R.drawable.round_home_24
            ) {
                Section(R.string.theme)
                Switch(
                    key = PreferenceKeys.DARK_MODE,
                    titleRes = R.string.dark_mode,
                    summaryRes = R.string.use_dark_theme,
                    defaultValue = isSystemInDarkTheme
                )
                Switch(
                    key = PreferenceKeys.USE_DYNAMIC_COLOR,
                    titleRes = R.string.use_dynamic_color,
                    defaultValue = true
                )
                Section(R.string.language)

            }

            PreferenceCategory(
                titleRes = R.string.security_and_privacy,
                descriptionRes = R.string.app_lock_secure_screen,
                iconRes = R.drawable.round_security_24
            ) {
                Section(R.string.security)
                Switch(
                    key = PreferenceKeys.APP_LOCK,
                    titleRes = R.string.app_lock,
                    summaryRes = R.string.lock_the_app,
                    defaultValue = true,
                    enabled = context.isBiometricAvailable()
                )

                if (context.isBiometricAvailable())
                    Info(infoRes = R.string.biometric_not_available)

                Switch(
                    key = PreferenceKeys.SECURE_SCREEN,
                    titleRes = R.string.enable_secure_screen,
                    summaryRes = R.string.secure_screen_summary,
                    defaultValue = true,
                )
            }

            PreferenceCategory(
                iconRes = R.drawable.round_info_24,
                titleRes = R.string.about,
                descriptionRes = R.string.about_the_app
            ) {
                Preference {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(84.dp),
                            painter = painterResource(R.mipmap.ic_launcher_foreground),
                            tint = MaterialTheme.colorScheme.onBackground,
                            contentDescription = null
                        )
                    }
                }
                Preference {
                    ListItem(
                        modifier = Modifier.clickable { context.copyToClipboard(BuildConfig.VERSION_NAME) },
                        headlineContent = { Text(stringResource(R.string.version)) },
                        supportingContent = { Text(BuildConfig.VERSION_NAME) },
                    )
                }
            }
        }
    }
}