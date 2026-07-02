package off.kys.backtalk.presentation.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Applies padding to the bottom of the layout only when the software keyboard (IME) is hidden.
 *
 * This is useful for maintaining spacing for UI elements (like FABs or bottom bars) that
 * should have a margin from the screen edge when the keyboard is closed, but should
 * sit flush against the keyboard when it is open.
 *
 * @param bottom The amount of padding to apply when the keyboard is not visible.
 * @return A [Modifier] with conditional padding applied based on IME visibility.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Modifier.hiddenKeyboardPadding(bottom: Dp): Modifier {
    val isKeyboardVisible = WindowInsets.isImeVisible

    return this.then(
        if (!isKeyboardVisible) {
            Modifier.padding(bottom = bottom)
        } else {
            Modifier
        }
    )
}