package off.kys.backtalk.presentation.event

/**
 * Sealed interface representing UI events for the main screen.
 */
sealed interface MainUiEvent {
    /**
     * Event to check for app updates.
     */
    object CheckUpdate : MainUiEvent

    /**
     * Event to dismiss the update dialog.
     */
    object DismissDialog : MainUiEvent

    /**
     * Event to open the update URL.
     *
     * @param downloadUrl The URL to open.
     */
    data class UpdateNow(val downloadUrl: String) : MainUiEvent
}