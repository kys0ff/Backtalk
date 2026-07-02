package off.kys.backtalk.presentation.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adds the specified [padding] to the bottom of the layout,
 * but only when the software keyboard (IME) is visible.
 */
@Stable
@Composable
fun Modifier.keyboardPadding(padding: Dp): Modifier = this.then(
    Modifier.padding(
        bottom = if (WindowInsets.ime.getBottom(LocalDensity.current) > 0) {
            padding
        } else {
            0.dp
        }
    )
)