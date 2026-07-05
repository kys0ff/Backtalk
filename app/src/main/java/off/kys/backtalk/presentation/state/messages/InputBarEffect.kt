package off.kys.backtalk.presentation.state.messages

import androidx.annotation.StringRes
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

sealed interface InputBarEffect {
    data object TriggerShake : InputBarEffect
    data class ShowError(@StringRes val messageRes: Int) : InputBarEffect
    data class PerformHapticFeedback(val type: HapticFeedbackType) : InputBarEffect
}