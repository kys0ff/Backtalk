package off.kys.backtalk.presentation.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Applies padding to the bottom of the layout only when the software keyboard (IME) is hidden.
 *
 * This is useful for maintaining specific spacing for UI elements (like text fields or buttons)
 * that should have a margin from the navigation bars when the keyboard is closed, but should
 * sit flush against the keyboard when it's open.
 *
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Modifier.imeClosedBottomInset(bottom: Dp): Modifier {
    val isKeyboardVisible = WindowInsets.isImeVisible

    return if (!isKeyboardVisible) {
        this.navigationBarsPadding().padding(bottom = bottom)
    } else {
        this
    }
}