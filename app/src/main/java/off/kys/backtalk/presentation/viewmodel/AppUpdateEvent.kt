package off.kys.backtalk.presentation.viewmodel

sealed interface AppUpdateEvent {
    object CheckUpdate : AppUpdateEvent
    object DismissDialog : AppUpdateEvent
    data class UpdateNow(val downloadUrl: String) : AppUpdateEvent
}