package off.kys.backtalk.presentation.state

import androidx.annotation.StringRes

sealed interface InputBarEffect {
    data object TriggerShake : InputBarEffect
    data class ShowError(@StringRes val messageRes: Int) : InputBarEffect
}