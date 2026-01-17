package off.kys.backtalk.presentation.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import off.kys.backtalk.R
import off.kys.preferences.compose.ui.screen.PreferenceScreen
import off.kys.preferences.core.PreferenceKey

class PreferencesScreen : Screen {

    @Composable
    override fun Content() {
        PreferenceScreen {
            PreferenceCategory(
                title = "General",
                description = "General app behavior",
                icon = R.drawable.round_send_24
            ) {
                Switch(
                    key = PreferenceKey.Switch("dark_mode"),
                    title = "Dark Mode",
                    icon = R.drawable.round_arrow_back_24,
                    summary = "Use dark theme",
                    defaultValue = false
                )

                Slider(
                    key = PreferenceKey.Slider("font_scale"),
                    title = "Font Size",
                    valueRange = 0.8f..1.4f,
                    steps = 5,
                    defaultValue = 1.0f
                )
            }

            PreferenceCategory(
                title = "About"
            ) {
                Action(
                    title = "Version",
                    summary = "1.0.0"
                ) {

                }
            }
        }
    }
}