package off.kys.backtalk.presentation.state

import androidx.compose.foundation.text.input.TextFieldState
import off.kys.backtalk.presentation.model.MessageUiModel
import off.kys.backtalk.presentation.status.SchedulingStage
import java.util.Locale

data class InputBarUiState(
    val textFieldState: TextFieldState = TextFieldState(),
    val replyingTo: MessageUiModel? = null,
    val editingMessage: MessageUiModel? = null,
    val isRecording: Boolean = false,
    val secondsElapsed: Int = 0,
    val amplitudes: List<Float> = emptyList(),
    val showTapHint: Boolean = false,
    val schedulingStage: SchedulingStage = SchedulingStage.Hidden,
    val showPermissionRationale: Boolean = false,
    val sharedImageUris: List<String> = emptyList(),
    val linkPreviewEnabled: Boolean = true,
    val offsetX: Float = 0f
) {
    val durationText: String
        get() {
            val minutes = secondsElapsed / 60
            val seconds = secondsElapsed % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }

    val isSendButtonVisible: Boolean
        get() = textFieldState.text.isNotBlank() && !isRecording
}
