package off.kys.backtalk.presentation.event

import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId

/**
 * Sealed interface representing the different UI events related to messages.
 */
sealed interface MessagesUiEvent {
    /**
     * UI event to load messages from the data source.
     */
    data object LoadMessages : MessagesUiEvent

    /**
     * UI event to send a message.
     *
     * @param text The text of the message to send.
     */
    data class SendMessage(val text: String) : MessagesUiEvent

    /**
     * UI event to send a voice message.
     *
     * @param path The path to the recorded audio file.
     * @param duration The duration of the recording in milliseconds.
     * @param waveform The waveform data.
     */
    data class SendVoiceMessage(val path: String, val duration: Long, val waveform: List<Float>) : MessagesUiEvent
    
    /**
     * UI event to edit a message.
     *
     * @param message The message to edit, or null if no message is being edited.
     */
    data class EditMessage(val message: MessageEntity?) : MessagesUiEvent

    /**
     * UI event to reply to a message.
     *
     * @param message The message to reply to, or null if no message is being replied to.
     */
    data class ReplyTo(val message: MessageEntity?) : MessagesUiEvent

    /**
     * UI event to select a message.
     *
     * @param id The ID of the message to select
     */
    data class ToggleSelection(val id: MessageId) : MessagesUiEvent

    /**
     * UI event to clear the current selection of messages.
     */
    object ClearSelection : MessagesUiEvent

    /**
     * UI event to delete the currently selected messages.
     */
    object DeleteSelected : MessagesUiEvent

    /**
     * UI event to confirm the deletion of the currently selected messages.
     */
    object ConfirmDeleteSelected : MessagesUiEvent

    /**
     * UI event to dismiss the delete confirmation dialog.
     */
    object DismissDeleteConfirmation : MessagesUiEvent

    /**
     * UI event to copy the currently selected messages to the clipboard.
     */
    object CopySelected : MessagesUiEvent

    /**
     * UI event to toggle the search mode.
     *
     * @param active Whether the search mode should be active.
     */
    data class ToggleSearch(val active: Boolean) : MessagesUiEvent

    /**
     * UI event to update the search query.
     *
     * @param query The new search query.
     */
    data class UpdateSearchQuery(val query: String) : MessagesUiEvent

    /**
     * UI event to navigate through search results.
     *
     * @param up Whether to navigate to the previous result (up) or next result (down).
     */
    data class NavigateSearch(val up: Boolean) : MessagesUiEvent

    /**
     * UI event to schedule a message for the future.
     */
    data class ScheduleMessage(val text: String, val scheduledTime: Long) : MessagesUiEvent

    /**
     * UI event to dismiss the permission rationale dialog.
     */
    object DismissPermissionRationale : MessagesUiEvent

    /**
     * UI event to select a tag for filtering.
     *
     * @param tag The tag to select, or null to clear the filter.
     */
    data class SelectTag(val tag: String?) : MessagesUiEvent

    /**
     * UI event to toggle the pinned status of a message.
     */
    data class TogglePinMessage(val id: MessageId, val isPinned: Boolean) : MessagesUiEvent

    /**
     * UI event to navigate to the next pinned message.
     */
    object NavigatePinned : MessagesUiEvent

    /**
     * UI event to toggle the pinned messages dialog.
     */
    data class TogglePinnedMessagesDialog(val show: Boolean) : MessagesUiEvent

    /**
     * UI event to scroll to a specific message.
     */
    data class ScrollToMessage(val id: MessageId) : MessagesUiEvent

    /**
     * UI event to trigger a blink animation for a message.
     */
    data class BlinkMessage(val id: MessageId?) : MessagesUiEvent

    /**
     * UI event to toggle the media picker bottom sheet.
     */
    data class ToggleMediaPicker(val show: Boolean) : MessagesUiEvent

    /**
     * UI event to send media messages.
     */
    data class SendMediaMessages(val uris: List<String>, val type: String, val description: String? = null) : MessagesUiEvent

    /**
     * UI event to notify that the scroll to bottom has been consumed.
     */
    data object ConsumedScrollToBottom : MessagesUiEvent

    /**
     * UI event to notify that the scroll to a pinned message has been consumed.
     */
    data object ConsumedScrollToPinned : MessagesUiEvent

    /**
     * UI event to notify that the scroll to a search result has been consumed.
     */
    data object ConsumedScrollToSearch : MessagesUiEvent

    /**
     * UI event to remove a single image from a message.
     *
     * @param messageId The ID of the message containing the image.
     * @param imagePath The file path of the image to remove.
     */
    data class RemoveImageFromMessage(val messageId: MessageId, val imagePath: String) : MessagesUiEvent
}
