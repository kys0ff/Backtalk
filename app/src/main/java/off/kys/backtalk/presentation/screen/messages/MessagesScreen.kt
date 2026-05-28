package off.kys.backtalk.presentation.screen.messages

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat.enableEdgeToEdge
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import off.kys.backtalk.common.manager.VibrationManager
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.components.SplitThemeContainer
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.screen.messages.components.InputBar
import off.kys.backtalk.presentation.screen.messages.components.MediaPickerSheet
import off.kys.backtalk.presentation.screen.messages.components.MessagesContent
import off.kys.backtalk.presentation.screen.messages.components.MessagesTopBar
import off.kys.backtalk.presentation.screen.messages.components.ScrollToBottomFab
import off.kys.backtalk.presentation.screen.preferences.SettingsScreen
import off.kys.backtalk.presentation.screen.reminders.RemindersScreen
import off.kys.backtalk.presentation.screen.statistics.StatisticsScreen
import off.kys.backtalk.presentation.screen.threads.ThreadsScreen
import off.kys.backtalk.presentation.state.MessagesUiState
import off.kys.backtalk.presentation.viewmodel.MessagesViewModel
import off.kys.backtalk.util.AudioPlayer
import off.kys.backtalk.util.compose.rememberHashtags
import off.kys.backtalk.util.compose.rememberScrollToBottomVisibility
import off.kys.backtalk.util.getAssetFile
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

class MessagesScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<MessagesViewModel>()
        val audioPlayer = koinInject<AudioPlayer>()
        val state by viewModel.uiState

        MessagesScreenContent(
            state = state,
            onEvent = viewModel::onEvent,
            onSettingsClick = { navigator += SettingsScreen() },
            onThreadsClick = { navigator += ThreadsScreen() },
            onRemindersClick = { navigator += RemindersScreen() },
            onStatisticsClick = { navigator += StatisticsScreen() },
            onStopAudio = { audioPlayer.stop() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreenContent(
    state: MessagesUiState,
    onEvent: (event: MessagesUiEvent) -> Unit,
    onSettingsClick: () -> Unit,
    onThreadsClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onStopAudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val messagesScrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showScrollToBottom = rememberScrollToBottomVisibility(messagesScrollState)
    val tags = rememberHashtags(state.messages)

    fun scrollToAndBlink(id: MessageId) {
        scope.launch {
            val targetIndex = state.filteredMessages.asReversed().indexOfFirst { it.id == id }
            if (targetIndex != -1) {
                messagesScrollState.animateScrollToItem(targetIndex)
                onEvent(MessagesUiEvent.BlinkMessage(id))
            }
        }
    }

    BackHandler(state.selectedMessageIds.isNotEmpty()) {
        onEvent(MessagesUiEvent.ClearSelection)
    }

    BackHandler(state.showDeleteConfirmation) {
        onEvent(MessagesUiEvent.DismissDeleteConfirmation)
    }

    BackHandler(state.replyingTo != null) {
        onEvent(MessagesUiEvent.ReplyTo(null))
    }

    BackHandler(state.editingMessage != null) {
        onEvent(MessagesUiEvent.EditMessage(null))
    }

    BackHandler(state.isSearchActive) {
        onEvent(MessagesUiEvent.ToggleSearch(false))
    }

    BackHandler(state.showPinnedMessagesDialog) {
        onEvent(MessagesUiEvent.TogglePinnedMessagesDialog(false))
    }

    BackHandler(state.showMediaPicker) {
        onEvent(MessagesUiEvent.ToggleMediaPicker(false))
    }

    LaunchedEffect(state.shouldScrollToSearch) {
        if (state.isSearchActive && state.currentSearchResultIndex != -1 && state.shouldScrollToSearch) {
            val targetId = state.searchResults[state.currentSearchResultIndex]
            scrollToAndBlink(targetId)
            onEvent(MessagesUiEvent.ConsumedScrollToSearch)
        }
    }

    LaunchedEffect(state.shouldScrollToPinned) {
        if (state.shouldScrollToPinned) {
            val activePinned = state.pinnedMessages.getOrNull(state.activePinnedMessageIndex)
            if (activePinned != null) {
                scrollToAndBlink(activePinned.id)
            }
            onEvent(MessagesUiEvent.ConsumedScrollToPinned)
        }
    }

    LaunchedEffect(state.shouldScrollToBottom) {
        if (state.shouldScrollToBottom) {
            messagesScrollState.scrollToItem(0)
            onEvent(MessagesUiEvent.ConsumedScrollToBottom)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onStopAudio()
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            MessagesTopBar(
                scrollBehavior = scrollBehavior,
                selectedCount = state.selectedMessageIds.size,
                isSearchActive = state.isSearchActive,
                searchQuery = state.searchQuery,
                searchResultsCount = state.searchResults.size,
                currentSearchIndex = state.currentSearchResultIndex,
                onCloseSelection = { onEvent(MessagesUiEvent.ClearSelection) },
                onDelete = { onEvent(MessagesUiEvent.DeleteSelected) },
                onCopy = { onEvent(MessagesUiEvent.CopySelected) },
                onPin = {
                    val selectedId = state.selectedMessageIds.firstOrNull()
                    if (selectedId != null) {
                        val isPinned =
                            state.messages.find { it.id == selectedId }?.isPinned ?: false
                        onEvent(MessagesUiEvent.TogglePinMessage(selectedId, !isPinned))
                        onEvent(MessagesUiEvent.ClearSelection)
                    }
                },
                onSettings = onSettingsClick,
                onThreads = onThreadsClick,
                onReminders = onRemindersClick,
                onStatistics = onStatisticsClick,
                onToggleSearch = { active: Boolean ->
                    onEvent(MessagesUiEvent.ToggleSearch(active))
                },
                onSearchQueryChange = { query: String ->
                    onEvent(MessagesUiEvent.UpdateSearchQuery(query))
                },
                onNavigateSearch = { up: Boolean ->
                    onEvent(MessagesUiEvent.NavigateSearch(up))
                },
                tags = tags,
                selectedTag = state.selectedTag,
                onTagClick = { onEvent(MessagesUiEvent.SelectTag(it)) }
            )
        },
        bottomBar = {
            InputBar(
                messageInput = state.editingMessage?.editedText.orEmpty().ifEmpty {
                    state.editingMessage?.text.orEmpty()
                },
                replyingTo = state.replyingTo,
                editingMessage = state.editingMessage,
                onCancelReply = { onEvent(MessagesUiEvent.ReplyTo(null)) },
                onCancelEdit = { onEvent(MessagesUiEvent.EditMessage(null)) },
                onMessageSend = {
                    onEvent(MessagesUiEvent.SendMessage(it))
                },
                onVoiceSend = { path, duration, waveform ->
                    onEvent(MessagesUiEvent.SendVoiceMessage(path, duration, waveform))
                },
                onMessageSchedule = { text, time ->
                    onEvent(MessagesUiEvent.ScheduleMessage(text, time))
                },
                onAttachClick = {
                    onEvent(MessagesUiEvent.ToggleMediaPicker(true))
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
        if (state.showMediaPicker) {
            MediaPickerSheet(
                onMediaSelected = { uris, type, description ->
                    onEvent(
                        MessagesUiEvent.SendMediaMessages(
                            uris = uris.map { it.toString() },
                            type = type,
                            description = description
                        )
                    )
                },
                onDismiss = { onEvent(MessagesUiEvent.ToggleMediaPicker(false)) }
            )
        }

        MessagesContent(
            modifier = Modifier.padding(scaffoldPadding),
            state = state,
            listState = messagesScrollState,
            onEditMessage = { onEvent(MessagesUiEvent.EditMessage(it)) },
            onReply = { onEvent(MessagesUiEvent.ReplyTo(it)) },
            onToggleSelect = { onEvent(MessagesUiEvent.ToggleSelection(it)) },
            onDismissRationale = { onEvent(MessagesUiEvent.DismissPermissionRationale) },
            onConfirmDelete = { onEvent(MessagesUiEvent.ConfirmDeleteSelected) },
            onDismissDelete = { onEvent(MessagesUiEvent.DismissDeleteConfirmation) },
            onTagClick = { onEvent(MessagesUiEvent.SelectTag(it)) },
            onNavigatePinned = {
                onEvent(MessagesUiEvent.NavigatePinned)
            },
            onTogglePinnedDialog = { onEvent(MessagesUiEvent.TogglePinnedMessagesDialog(it)) },
            onTogglePin = { message, isPinned ->
                onEvent(
                    MessagesUiEvent.TogglePinMessage(
                        message.id,
                        isPinned
                    )
                )
            },
            onScrollToMessage = { id ->
                scrollToAndBlink(id)
                onEvent(MessagesUiEvent.ScrollToMessage(id))
            },
            onImageDelete = { messageId, imagePath ->
                onEvent(MessagesUiEvent.RemoveImageFromMessage(messageId, imagePath))
            }
        )
    }
}

@Preview(
    showSystemUi = true,
    device = "id:pixel_10",
)
@Composable
private fun MessagesScreenPreview() {
    val context = LocalContext.current

    KoinApplication(
        configuration = koinConfiguration(
            declaration = {
                modules(
                    module {
                        single { BacktalkPreferences(context) }
                        single { VibrationManager(context, get()) }
                        single { AudioPlayer() }
                    }
                )

            }
        ),
        content = {
            SplitThemeContainer {
                val window = LocalActivity.currentOrThrow.window
                enableEdgeToEdge(window)
                val mockMessages = buildList {
                    add(
                        MessageEntity(
                            id = MessageId.generate(),
                            text = "Saw a massive duck today. Need to remember the name of that park.",
                            timestamp = System.currentTimeMillis() - 600000,
                            repliedToId = null
                        )
                    )

                    val duckImageId = MessageId.generate()
                    add(
                        MessageEntity(
                            id = duckImageId,
                            text = "duck",
                            timestamp = System.currentTimeMillis() - 500000,
                            repliedToId = null,
                            mediaPath = context.getAssetFile("duck.jpg").absolutePath,
                            mediaType = "image/jpeg"
                        )
                    )

                    add(
                        MessageEntity(
                            id = MessageId.generate(),
                            text = "it's a good duck.",
                            timestamp = System.currentTimeMillis() - 400000,
                            repliedToId = duckImageId
                        )
                    )

                    add(
                        MessageEntity(
                            id = MessageId.generate(),
                            text = "Buy birdseed / bread crumbs for the pond tomorrow.",
                            timestamp = System.currentTimeMillis() - 100000,
                            repliedToId = null,
                            isReminder = true,
                            scheduledTimestamp = System.currentTimeMillis() + 86400000,
                            isPinned = true
                        )
                    )
                }

                MessagesScreenContent(
                    state = MessagesUiState(
                        messages = mockMessages,
                        filteredMessages = mockMessages,
                        pinnedMessages = mockMessages.filter { it.isPinned }.reversed(),
                        isLoading = false,
                    ),
                    onEvent = {},
                    onSettingsClick = {},
                    onThreadsClick = {},
                    onRemindersClick = {},
                    onStatisticsClick = {},
                    onStopAudio = {}
                )
            }
        }
    )
}
