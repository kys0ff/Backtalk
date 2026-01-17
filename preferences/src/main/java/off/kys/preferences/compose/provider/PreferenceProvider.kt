package off.kys.preferences.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import off.kys.preferences.data.PreferenceManager

// This is the "hook" we will use to access the manager
val LocalPreferenceManager = staticCompositionLocalOf<PreferenceManager> {
    error("No PreferenceManager provided! Wrap your app in PreferenceProvider.")
}

@Composable
fun PreferenceProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val manager = remember { PreferenceManager(context) }
    
    CompositionLocalProvider(LocalPreferenceManager provides manager) {
        content()
    }
}