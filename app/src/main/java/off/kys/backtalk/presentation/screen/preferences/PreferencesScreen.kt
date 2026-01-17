package off.kys.backtalk.presentation.screen.preferences

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.R
import off.kys.preferences.compose.ui.screen.PreferenceScreen
import off.kys.preferences.core.PreferenceKey

class PreferencesScreen : Screen {
    
    @Composable
    override fun Content() {
        val isSystemInDarkTheme = isSystemInDarkTheme()

        PreferenceScreen {
            PreferenceCategory(
                title = "General",
                description = "General app behavior",
                icon = R.drawable.round_home_24
            ) {
                Switch(
                    key = PreferenceKey.Switch("dark_mode"),
                    title = "Dark Mode",
                    summary = "Use dark theme",
                    defaultValue = isSystemInDarkTheme
                )
            }

            PreferenceCategory(
                title = "About"
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
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null
                        )
                    }
                }
                Action(
                    title = "Version",
                    summary = BuildConfig.VERSION_NAME
                ) {

                }
            }
        }
    }
}