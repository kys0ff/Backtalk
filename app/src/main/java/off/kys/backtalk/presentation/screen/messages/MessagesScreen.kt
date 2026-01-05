package off.kys.backtalk.presentation.screen.messages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import off.kys.backtalk.presentation.screen.messages.components.MessagesContent
import off.kys.backtalk.presentation.screen.messages.components.MessagesTopBar
import off.kys.backtalk.presentation.viewmodel.MessagesUiEvent
import off.kys.backtalk.presentation.viewmodel.MessagesViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.util.concurrent.TimeUnit

class MessagesScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinViewModel<MessagesViewModel>()
        val state by viewModel.uiState

        BackHandler(state.selectedMessageId != null) {
            viewModel.onEvent(MessagesUiEvent.SelectMessage(null))
        }

        BackHandler(state.replyingTo != null) {
            viewModel.onEvent(MessagesUiEvent.ReplyTo(null))
        }

        Scaffold(
            topBar = {
                MessagesTopBar(
                    selectedMessageId = state.selectedMessageId,
                    onCloseSelection = {
                        viewModel.onEvent(MessagesUiEvent.SelectMessage(null))
                    },
                    onDelete = {
                        state.selectedMessageId?.let {
                            viewModel.onEvent(MessagesUiEvent.DeleteMessage(it))
                        }
                    }
                )
            }
        ) { padding ->
            MessagesContent(
                modifier = Modifier.padding(padding),
                state = state,
                onReply = { viewModel.onEvent(MessagesUiEvent.ReplyTo(it)) },
                onSelect = { viewModel.onEvent(MessagesUiEvent.SelectMessage(it)) },
                onSend = { viewModel.onEvent(MessagesUiEvent.SendMessage(it)) }
            )
        }
    }
}