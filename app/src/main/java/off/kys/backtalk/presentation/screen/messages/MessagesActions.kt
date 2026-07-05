package off.kys.backtalk.presentation.screen.messages

import androidx.compose.runtime.staticCompositionLocalOf
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.util.AudioPlayer

/**
 * Data class representing the actions that can be performed on the messages screen.
 */
data class MessagesActions(
    val onEvent: (MessagesUiEvent) -> Unit = {},
    val onSettingsClick: () -> Unit = {},
    val onThreadsClick: () -> Unit = {},
    val onRemindersClick: () -> Unit = {},
    val onStatisticsClick: () -> Unit = {},
    val onStopAudio: () -> Unit = {},
    val onScrollToMessage: (MessageId) -> Unit = {},
    val onPinSelected: () -> Unit = {},
    val onDeleteSelected: () -> Unit = {},
    val onCopySelected: () -> Unit = {},
    val onCloseSelection: () -> Unit = {},
    val onNavigatePinned: () -> Unit = {}
)

/**
 * CompositionLocal to provide [MessagesActions] down the layout tree.
 */
val LocalMessagesActions = staticCompositionLocalOf { MessagesActions() }

/**
 * CompositionLocal to provide [AudioPlayer] down the layout tree.
 */
val LocalAudioPlayer = staticCompositionLocalOf<AudioPlayer> { error("No AudioPlayer provided") }
