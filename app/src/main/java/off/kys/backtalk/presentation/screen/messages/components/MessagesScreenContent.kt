package off.kys.backtalk.presentation.screen.messages.components

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.coroutines.launch
import off.kys.backtalk.common.Constants
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.screen.components.changelog.ChangelogDialog
import off.kys.backtalk.presentation.state.MessagesUiState
import off.kys.backtalk.util.compose.rememberHashtags
import off.kys.backtalk.util.compose.rememberScrollToBottomVisibility
import off.kys.backtalk.util.emptyString

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

    // Move heavy collection metrics out of the raw recomposition path
    val selectionMetrics = remember(state.selectedMessageIds, state.selectedImagePaths, state.messages) {
        val selectedMessagesCount = state.selectedMessageIds.size
        val selectedImagesCount = state.selectedImagePaths.values.sumOf { it.size }

        val totalSelectedCount = selectedMessagesCount + state.selectedImagePaths.filterKeys {
            it !in state.selectedMessageIds
        }.values.sumOf { it.size }

        val currentTime = System.currentTimeMillis()
        val deletableMessagesCount = state.messages.count {
            it.id in state.selectedMessageIds && (currentTime - it.timestamp) < Constants.MESSAGE_EDIT_DELETE_WINDOW
        }

        val deletableImagesCount = state.selectedImagePaths.filterKeys { it !in state.selectedMessageIds }.entries.sumOf { (messageId, paths) ->
            val message = state.messages.find { it.id == messageId }
            if (message != null && (currentTime - message.timestamp) < Constants.MESSAGE_EDIT_DELETE_WINDOW) {
                paths.size
            } else 0
        }

        SelectionMetrics(
            selectedMessagesCount = selectedMessagesCount,
            selectedImagesCount = selectedImagesCount,
            totalSelectedCount = totalSelectedCount,
            totalDeletableCount = deletableMessagesCount + deletableImagesCount
        )
    }

    fun scrollToAndBlink(id: MessageId) {
        scope.launch {
            val targetIndex = state.filteredMessages.asReversed().indexOfFirst { it.id == id }
            if (targetIndex != -1) {
                messagesScrollState.animateScrollToItem(targetIndex)
                onEvent(MessagesUiEvent.BlinkMessage(id))
            }
        }
    }

    // Consolidate seven BackHandlers into one sequential interceptor
    val isBackHandlerActive = remember(state) {
        derivedStateOf {
            state.selectedMessageIds.isNotEmpty() || state.selectedImagePaths.isNotEmpty() ||
                    state.showDeleteConfirmation || state.replyingTo != null ||
                    state.editingMessage != null || state.isSearchActive ||
                    state.showPinnedMessagesDialog || state.showMediaPicker
        }
    }

    BackHandler(enabled = isBackHandlerActive.value) {
        when {
            state.selectedMessageIds.isNotEmpty() || state.selectedImagePaths.isNotEmpty() -> onEvent(MessagesUiEvent.ClearSelection)
            state.showDeleteConfirmation -> onEvent(MessagesUiEvent.DismissDeleteConfirmation)
            state.replyingTo != null -> onEvent(MessagesUiEvent.ReplyTo(null))
            state.editingMessage != null -> onEvent(MessagesUiEvent.EditMessage(null))
            state.isSearchActive -> onEvent(MessagesUiEvent.ToggleSearch(false))
            state.showPinnedMessagesDialog -> onEvent(MessagesUiEvent.TogglePinnedMessagesDialog(false))
            state.showMediaPicker -> onEvent(MessagesUiEvent.ToggleMediaPicker(false))
        }
    }

    LaunchedEffect(state.scrollToSearchTrigger) {
        if (state.isSearchActive && state.currentSearchResultIndex != -1 && state.scrollToSearchTrigger > 0) {
            val targetId = state.searchResults[state.currentSearchResultIndex]
            scrollToAndBlink(targetId)
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
                selectedCount = selectionMetrics.totalSelectedCount,
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
                        val isPinned = state.messages.find { it.id == selectedId }?.isPinned ?: false
                        onEvent(MessagesUiEvent.TogglePinMessage(selectedId, !isPinned))
                        onEvent(MessagesUiEvent.ClearSelection)
                    }
                },
                onSettings = onSettingsClick,
                onThreads = onThreadsClick,
                onReminders = onRemindersClick,
                onStatistics = onStatisticsClick,
                onToggleSearch = { active -> onEvent(MessagesUiEvent.ToggleSearch(active)) },
                onSearchQueryChange = { query -> onEvent(MessagesUiEvent.UpdateSearchQuery(query)) },
                onNavigateSearch = { up -> onEvent(MessagesUiEvent.NavigateSearch(up)) },
                isImageSelectionOnly = selectionMetrics.selectedMessagesCount == 0 && selectionMetrics.selectedImagesCount > 0,
                canDelete = selectionMetrics.totalDeletableCount > 0
            )
        },
        bottomBar = {
            InputBar(
                messageInput = when {
                    state.editingMessage != null -> state.editingMessage.editedText.orEmpty().ifEmpty {
                        state.editingMessage.text
                    }
                    state.sharedText != null -> state.sharedText
                    else -> emptyString()
                },
                replyingTo = state.replyingTo,
                editingMessage = state.editingMessage,
                onCancelReply = { onEvent(MessagesUiEvent.ReplyTo(null)) },
                onCancelEdit = { onEvent(MessagesUiEvent.EditMessage(null)) },
                onMessageSend = { onEvent(MessagesUiEvent.SendMessage(it)) },
                onVoiceSend = { path, duration, waveform ->
                    onEvent(MessagesUiEvent.SendVoiceMessage(path, duration, waveform))
                },
                onMessageSchedule = { text, time ->
                    onEvent(MessagesUiEvent.ScheduleMessage(text, time))
                },
                onAttachClick = { onEvent(MessagesUiEvent.ToggleMediaPicker(true)) },
                sharedImageUri = state.sharedImageUri,
                onCancelSharedImage = { onEvent(MessagesUiEvent.ClearSharedImage) },
                onSharedImageSend = { uri, caption ->
                    onEvent(
                        MessagesUiEvent.SendMediaMessages(
                            uris = listOf(uri),
                            type = "image/*",
                            description = caption.takeIf { it.isNotBlank() }
                        )
                    )
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
            tags = tags,
            listState = messagesScrollState,
            onEditMessage = { onEvent(MessagesUiEvent.EditMessage(it)) },
            onReply = { onEvent(MessagesUiEvent.ReplyTo(it)) },
            onToggleSelect = { onEvent(MessagesUiEvent.ToggleSelection(it)) },
            onDismissRationale = { onEvent(MessagesUiEvent.DismissPermissionRationale) },
            onConfirmDelete = { onEvent(MessagesUiEvent.ConfirmDeleteSelected) },
            onDismissDelete = { onEvent(MessagesUiEvent.DismissDeleteConfirmation) },
            onTagClick = { onEvent(MessagesUiEvent.SelectTag(it)) },
            onNavigatePinned = { onEvent(MessagesUiEvent.NavigatePinned) },
            onTogglePinnedDialog = { onEvent(MessagesUiEvent.TogglePinnedMessagesDialog(it)) },
            onTogglePin = { message, isPinned ->
                onEvent(MessagesUiEvent.TogglePinMessage(message.id, isPinned))
            },
            onScrollToMessage = { id ->
                scrollToAndBlink(id)
                onEvent(MessagesUiEvent.ScrollToMessage(id))
            },
            onToggleImageSelect = { messageId, imagePath ->
                onEvent(MessagesUiEvent.ToggleImageSelection(messageId, imagePath))
            },
            totalDeletableCount = selectionMetrics.totalDeletableCount,
            totalSelectedCount = selectionMetrics.totalSelectedCount
        )

        if (state.showChangelogDialog) {
            ChangelogDialog(
                onDismiss = { onEvent(MessagesUiEvent.DismissChangelog) }
            )
        }
    }
}

private data class SelectionMetrics(
    val selectedMessagesCount: Int,
    val selectedImagesCount: Int,
    val totalSelectedCount: Int,
    val totalDeletableCount: Int
)