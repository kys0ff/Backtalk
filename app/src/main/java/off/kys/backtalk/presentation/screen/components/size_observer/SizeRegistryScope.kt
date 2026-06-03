package off.kys.backtalk.presentation.screen.components.size_observer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

/**
 * Encapsulates the [SizeRegistry] context, eliminating the need to manually
 * instantiate or provide [LocalSizeRegistry] in your screen layouts.
 */
@Composable
fun SizeRegistryScope(
    content: @Composable () -> Unit
) {
    val sizeRegistry = remember { SizeRegistry() }
    
    CompositionLocalProvider(LocalSizeRegistry provides sizeRegistry) {
        content()
    }
}