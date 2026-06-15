package off.kys.backtalk.presentation.state

sealed interface InputBarEffect {
    data object TriggerShake : InputBarEffect
}