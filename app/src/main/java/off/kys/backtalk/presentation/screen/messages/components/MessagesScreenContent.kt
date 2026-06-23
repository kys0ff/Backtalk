package off.kys.backtalk.presentation.screen.messages.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
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
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.components.status_scaffold.StatusScaffold
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.screen.components.changelog.ChangelogDialog
import off.kys.backtalk.presentation.state.MessagesUiState
import off.kys.backtalk.presentation.viewmodel.InputBarViewModel
import off.kys.backtalk.util.compose.rememberScrollToBottomVisibility
import off.kys.backtalk.util.emptyString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreenContent(
    state: MessagesUiState,
    inputBarViewModel: InputBarViewModel,
    onEvent: (event: MessagesUiEvent) -> Unit,
    onSettingsClick: () -> Unit,
    onThreadsClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onStopAudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(canScroll = { false })
    val messagesScrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showScrollToBottom = rememberScrollToBottomVisibility(messagesScrollState)
    val tags = state.hashtags

    fun scrollToAndBlink(id: MessageId) {
        scope.launch {
            val targetIndex = state.filteredMessages.asReversed().indexOfFirst { it.id == id }
            if (targetIndex != -1) {
                messagesScrollState.animateScrollToItem(targetIndex)
                onEvent(MessagesUiEvent.BlinkMessage(id))
            }
        }
    }

    val isBackHandlerActive = remember(state) {
        derivedStateOf {
            state.selectedMessageIds.isNotEmpty() || state.selectedImagePaths.isNotEmpty() ||
                    state.showDeleteConfirmation || state.replyingTo != null ||
                    state.editingMessage != null || state.isSearchActive ||
                    state.showPinnedMessagesDialog || state.showMediaPicker ||
                    state.showSharedMediaSheet || state.messageContextMenuEntity != null
        }
    }

    BackHandler(enabled = isBackHandlerActive.value) {
        when {
            state.messageContextMenuEntity != null -> onEvent(MessagesUiEvent.ShowMessageContextMenu(null))

            state.selectedMessageIds.isNotEmpty() || state.selectedImagePaths.isNotEmpty()
                -> onEvent(MessagesUiEvent.ClearSelection)

            state.showDeleteConfirmation -> onEvent(MessagesUiEvent.DismissDeleteConfirmation)
            state.replyingTo != null -> onEvent(MessagesUiEvent.ReplyTo(null))
            state.editingMessage != null -> onEvent(MessagesUiEvent.EditMessage(null))
            state.isSearchActive -> onEvent(MessagesUiEvent.ToggleSearch(false))
            state.showPinnedMessagesDialog -> onEvent(
                MessagesUiEvent.TogglePinnedMessagesDialog(
                    false
                )
            )

            state.showMediaPicker -> onEvent(MessagesUiEvent.ToggleMediaPicker(false))
            state.showSharedMediaSheet -> onEvent(MessagesUiEvent.ToggleSharedMediaSheet(false))
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

    StatusScaffold(
        status = state.scaffoldStatus,
        message = state.scaffoldMessage,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            MessagesTopBar(
                scrollBehavior = scrollBehavior,
                selectedCount = state.selectionMetrics.totalSelectedCount,
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
                onToggleSearch = { active -> onEvent(MessagesUiEvent.ToggleSearch(active)) },
                onSearchQueryChange = { query -> onEvent(MessagesUiEvent.UpdateSearchQuery(query)) },
                onNavigateSearch = { up -> onEvent(MessagesUiEvent.NavigateSearch(up)) },
                onSharedMedia = { onEvent(MessagesUiEvent.ToggleSharedMediaSheet(true)) },
                isImageSelectionOnly = state.selectionMetrics.selectedMessagesCount == 0 && state.selectionMetrics.selectedImagesCount > 0,
                canDelete = state.selectionMetrics.totalDeletableCount > 0
            )
        },
        bottomBar = {
            InputBar(
                viewModel = inputBarViewModel,
                messageInput = when {
                    state.editingMessage != null -> state.editingMessage.editedText.orEmpty()
                        .ifEmpty {
                            state.editingMessage.text
                        }

                    state.sharedText != null -> state.sharedText
                    else -> emptyString()
                },
                replyingTo = state.replyingTo,
                editingMessage = state.editingMessage,
                sharedImageUris = state.sharedImageUris,
                onCancelSharedImage = { onEvent(MessagesUiEvent.ClearSharedImage) }
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

        if (state.showSharedMediaSheet) {
            SharedMediaSheet(
                onDismiss = { onEvent(MessagesUiEvent.ToggleSharedMediaSheet(false)) },
                onScrollToMessage = { id ->
                    onEvent(MessagesUiEvent.ToggleSharedMediaSheet(false))
                    scrollToAndBlink(id)
                    onEvent(MessagesUiEvent.ScrollToMessage(id))
                }
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
            onDeleteMessage = { onEvent(MessagesUiEvent.DeleteMessage(it)) },
            onCopyMessage = { onEvent(MessagesUiEvent.CopyMessage(it)) },
            onTagClick = { onEvent(MessagesUiEvent.SelectTag(it)) },
            onNavigatePinned = { onEvent(MessagesUiEvent.NavigatePinned) },
            onTogglePinnedDialog = { onEvent(MessagesUiEvent.TogglePinnedMessagesDialog(it)) },
                onTogglePin = { message, isPinned ->
                onEvent(MessagesUiEvent.TogglePinMessage(message.id, isPinned))
            },
            onLongClick = { onEvent(MessagesUiEvent.ShowMessageContextMenu(it)) },
            onScrollToMessage = { id ->
                scrollToAndBlink(id)
                onEvent(MessagesUiEvent.ScrollToMessage(id))
            },
            onToggleImageSelect = { messageId, imagePath ->
                onEvent(MessagesUiEvent.ToggleImageSelection(messageId, imagePath))
            },
            totalDeletableCount = state.selectionMetrics.totalDeletableCount,
            totalSelectedCount = state.selectionMetrics.totalSelectedCount
        )

        if (state.showChangelogDialog) {
            ChangelogDialog(
                onDismiss = { onEvent(MessagesUiEvent.DismissChangelog) }
            )
        }
    }
}