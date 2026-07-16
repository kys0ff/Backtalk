package off.kys.backtalk.presentation.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Modifier.conditionalImePadding(bottom: Dp): Modifier {
    val isKeyboardVisible = WindowInsets.isImeVisible

    return if (!isKeyboardVisible) {
        this.padding(bottom = bottom)
    } else {
        this.imePadding()
    }
}