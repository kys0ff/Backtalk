package off.kys.backtalk.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class PopupState(initialVisible: Boolean = false) {
    var isVisible by mutableStateOf(initialVisible)
        private set

    fun show() { isVisible = true }
    fun hide() { isVisible = false }
    fun toggle() { isVisible = !isVisible }
}

@Composable
fun rememberPopupState(initialVisible: Boolean = false): PopupState {
    return remember { PopupState(initialVisible = initialVisible) }
}