package off.kys.backtalk.presentation.event

sealed interface MainUiEvent {
    object CheckUpdate : MainUiEvent
    object DismissDialog : MainUiEvent
    data class UpdateNow(val downloadUrl: String) : MainUiEvent
}