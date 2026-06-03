package off.kys.backtalk.presentation.screen.components.size_observer

import androidx.compose.runtime.compositionLocalOf

val LocalSizeRegistry = compositionLocalOf<SizeRegistry> {
    error("No SizeRegistry provided. Wrap your hierarchy in CompositionLocalProvider(LocalSizeRegistry provides ...) first.")
}