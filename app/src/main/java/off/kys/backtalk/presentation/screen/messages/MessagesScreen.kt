package off.kys.backtalk.presentation.screen.messages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.screen.messages.components.MessagesContent
import off.kys.backtalk.presentation.screen.messages.components.MessagesTopBar
import off.kys.backtalk.presentation.screen.preferences.PreferencesScreen
import off.kys.backtalk.presentation.viewmodel.MessagesViewModel
import org.koin.compose.viewmodel.koinViewModel

class MessagesScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<MessagesViewModel>()
        val state by viewModel.uiState
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()


        BackHandler(state.selectedMessageIds.isNotEmpty()) {
            viewModel.onEvent(MessagesUiEvent.ClearSelection)
        }

        BackHandler(state.replyingTo != null) {
            viewModel.onEvent(MessagesUiEvent.ReplyTo(null))
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                MessagesTopBar(
                    scrollBehavior = scrollBehavior,
                    selectedCount = state.selectedMessageIds.size,
                    onCloseSelection = {
                        viewModel.onEvent(MessagesUiEvent.ClearSelection)
                    },
                    onDelete = {
                        viewModel.onEvent(MessagesUiEvent.DeleteSelected)
                    },
                    onCopy = {
                        viewModel.onEvent(MessagesUiEvent.CopySelected)
                    },
                    onSettings = {
                        navigator += PreferencesScreen()
                    }
                )
            }
        ) { scaffoldPadding ->
            MessagesContent(
                modifier = Modifier.padding(scaffoldPadding),
                state = state,
                onReply = { viewModel.onEvent(MessagesUiEvent.ReplyTo(it)) },
                onToggleSelect = { viewModel.onEvent(MessagesUiEvent.ToggleSelection(it)) },
                onSend = { viewModel.onEvent(MessagesUiEvent.SendMessage(it)) }
            )
        }
    }
}