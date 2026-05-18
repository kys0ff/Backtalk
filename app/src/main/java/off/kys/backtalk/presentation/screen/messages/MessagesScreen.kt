package off.kys.backtalk.presentation.screen.messages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.screen.messages.components.InputBar
import off.kys.backtalk.presentation.screen.messages.components.MessagesContent
import off.kys.backtalk.presentation.screen.messages.components.MessagesTopBar
import off.kys.backtalk.presentation.screen.messages.components.ScrollToBottomFab
import off.kys.backtalk.presentation.screen.preferences.SettingsScreen
import off.kys.backtalk.presentation.screen.reminders.RemindersScreen
import off.kys.backtalk.presentation.screen.statistics.StatisticsScreen
import off.kys.backtalk.presentation.screen.threads.ThreadsScreen
import off.kys.backtalk.presentation.viewmodel.MessagesViewModel
import off.kys.backtalk.util.AudioPlayer
import off.kys.backtalk.util.compose.rememberHashtags
import off.kys.backtalk.util.compose.rememberScrollToBottomVisibility
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

class MessagesScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<MessagesViewModel>()
        val audioPlayer = koinInject<AudioPlayer>()
        val state by viewModel.uiState
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val messagesScrollState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val showScrollToBottom = rememberScrollToBottomVisibility(messagesScrollState)
        val tags = rememberHashtags(state.messages)

        BackHandler(state.selectedMessageIds.isNotEmpty()) {
            viewModel.onEvent(MessagesUiEvent.ClearSelection)
        }

        BackHandler(state.showDeleteConfirmation) {
            viewModel.onEvent(MessagesUiEvent.DismissDeleteConfirmation)
        }

        BackHandler(state.replyingTo != null) {
            viewModel.onEvent(MessagesUiEvent.ReplyTo(null))
        }

        BackHandler(state.editingMessage != null) {
            viewModel.onEvent(MessagesUiEvent.EditMessage(null))
        }

        BackHandler(state.isSearchActive) {
            viewModel.onEvent(MessagesUiEvent.ToggleSearch(false))
        }

        LaunchedEffect(state.currentSearchResultIndex) {
            if (state.isSearchActive && state.currentSearchResultIndex != -1) {
                val targetId = state.searchResults[state.currentSearchResultIndex]
                val targetIndex = state.messages.reversed().indexOfFirst { it.id == targetId }
                if (targetIndex != -1) {
                    messagesScrollState.animateScrollToItem(targetIndex)
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                audioPlayer.stop()
            }
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            topBar = {
                MessagesTopBar(
                    scrollBehavior = scrollBehavior,
                    selectedCount = state.selectedMessageIds.size,
                    isSearchActive = state.isSearchActive,
                    searchQuery = state.searchQuery,
                    searchResultsCount = state.searchResults.size,
                    currentSearchIndex = state.currentSearchResultIndex,
                    onCloseSelection = { viewModel.onEvent(MessagesUiEvent.ClearSelection) },
                    onDelete = { viewModel.onEvent(MessagesUiEvent.DeleteSelected) },
                    onCopy = { viewModel.onEvent(MessagesUiEvent.CopySelected) },
                    onSettings = { navigator += SettingsScreen() },
                    onThreads = { navigator += ThreadsScreen() },
                    onReminders = { navigator += RemindersScreen() },
                    onStatistics = { navigator += StatisticsScreen() },
                    onToggleSearch = { active: Boolean ->
                        viewModel.onEvent(
                            MessagesUiEvent.ToggleSearch(
                                active
                            )
                        )
                    },
                    onSearchQueryChange = { query: String ->
                        viewModel.onEvent(
                            MessagesUiEvent.UpdateSearchQuery(
                                query
                            )
                        )
                    },
                    onNavigateSearch = { up: Boolean ->
                        viewModel.onEvent(
                            MessagesUiEvent.NavigateSearch(
                                up
                            )
                        )
                    },
                    tags = tags,
                    selectedTag = state.selectedTag,
                    onTagClick = { viewModel.onEvent(MessagesUiEvent.SelectTag(it)) }
                )
            },
            bottomBar = {
                InputBar(
                    messageInput = state.editingMessage?.let { it.editedText ?: it.text }.orEmpty(),
                    replyingTo = state.replyingTo,
                    editingMessage = state.editingMessage,
                    onCancelReply = { viewModel.onEvent(MessagesUiEvent.ReplyTo(null)) },
                    onCancelEdit = { viewModel.onEvent(MessagesUiEvent.EditMessage(null)) },
                    onMessageSend = { viewModel.onEvent(MessagesUiEvent.SendMessage(it)) },
                    onVoiceSend = { path, duration, waveform ->
                        viewModel.onEvent(
                            MessagesUiEvent.SendVoiceMessage(
                                path,
                                duration,
                                waveform
                            )
                        )
                    },
                    onMessageSchedule = { text, time ->
                        viewModel.onEvent(MessagesUiEvent.ScheduleMessage(text, time))
                    }
                )
            },
            floatingActionButton = {
                ScrollToBottomFab(
                    isVisible = showScrollToBottom,
                    onClick = {
                        scope.launch {
                            messagesScrollState.animateScrollToItem(0)
                        }
                    }
                )
            }
        ) { scaffoldPadding ->
            MessagesContent(
                modifier = Modifier.padding(scaffoldPadding),
                state = state,
                listState = messagesScrollState,
                onEditMessage = { viewModel.onEvent(MessagesUiEvent.EditMessage(it)) },
                onReply = { viewModel.onEvent(MessagesUiEvent.ReplyTo(it)) },
                onToggleSelect = { viewModel.onEvent(MessagesUiEvent.ToggleSelection(it)) },
                onDismissRationale = { viewModel.onEvent(MessagesUiEvent.DismissPermissionRationale) },
                onConfirmDelete = { viewModel.onEvent(MessagesUiEvent.ConfirmDeleteSelected) },
                onDismissDelete = { viewModel.onEvent(MessagesUiEvent.DismissDeleteConfirmation) },
                onTagClick = { viewModel.onEvent(MessagesUiEvent.SelectTag(it)) }
            )
        }
    }
}
