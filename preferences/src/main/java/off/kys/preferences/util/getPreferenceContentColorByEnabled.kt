package off.kys.preferences.util

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable

@Composable
fun getPreferenceContentColorByEnabled(enabled: Boolean = true) =
    if (enabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.38f)
