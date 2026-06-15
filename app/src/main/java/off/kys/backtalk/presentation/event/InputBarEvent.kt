package off.kys.backtalk.presentation.event

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.content.TransferableContent
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.presentation.status.SchedulingStage

sealed interface InputBarEvent {
    data class SendMessage(val text: String) : InputBarEvent
    data class ScheduleMessage(val text: String, val timestamp: Long) : InputBarEvent
    data class UpdateReplyingTo(val message: MessageEntity?) : InputBarEvent
    data class UpdateEditingMessage(val message: MessageEntity?) : InputBarEvent
    data object CancelReply : InputBarEvent
    data object CancelEdit : InputBarEvent
    data object AttachClicked : InputBarEvent

    // Voice Recording Actions
    data object StartRecording : InputBarEvent
    data object CancelRecording : InputBarEvent
    data object StopAndSendRecording : InputBarEvent
    data object ShowTapHint : InputBarEvent
    data object ClearTapHint : InputBarEvent
    data class UpdateOffsetX(val x: Float) : InputBarEvent

    // Scheduling & Permissions
    data class ChangeSchedulingStage(val stage: SchedulingStage) : InputBarEvent
    data object RequestExactAlarmPermission : InputBarEvent
    data object DismissPermissionRationale : InputBarEvent

    // Media & Rich Content
    data object CancelSharedImage : InputBarEvent
    data class SendSharedImages(val uris: List<String>, val caption: String) : InputBarEvent
    data class ContentReceived @OptIn(ExperimentalFoundationApi::class) constructor(
        val transferableContent: TransferableContent
    ) : InputBarEvent
}