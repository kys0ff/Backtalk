package off.kys.backtalk.presentation.screen.messages.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.components.imeClosedBottomInset
import off.kys.backtalk.presentation.components.status_scaffold.StatusScaffold
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.screen.components.changelog.ChangelogDialog
import off.kys.backtalk.presentation.screen.messages.LocalMessagesActions
import off.kys.backtalk.presentation.state.messages.MessagesUiState
import off.kys.backtalk.presentation.viewmodel.InputBarViewModel
import off.kys.backtalk.util.compose.rememberScrollToBottomVisibility
import off.kys.backtalk.util.emptyString
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MessagesScreenContent(
    state: MessagesUiState,
    inputBarViewModel: InputBarViewModel,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(canScroll = { false })
    val messagesScrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val baseActions = LocalMessagesActions.current

    // region Navigation & Actions
    fun scrollToAndBlink(id: MessageId) {
        scope.launch {
            val targetIndex = state.filteredMessages.asReversed().indexOfFirst { it.id == id }
            if (targetIndex != -1) {
                messagesScrollState.animateScrollToItem(targetIndex)
                baseActions.onEvent(MessagesUiEvent.BlinkMessage(id))
            }
        }
    }

    val actions = remember(baseActions, state, messagesScrollState) {
        baseActions.copy(
            onScrollToMessage = { id ->
                scrollToAndBlink(id)
                baseActions.onEvent(MessagesUiEvent.ScrollToMessage(id))
            }
        )
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
            state.messageContextMenuEntity != null -> actions.onEvent(
                MessagesUiEvent.ShowMessageContextMenu(null)
            )

            state.selectedMessageIds.isNotEmpty() || state.selectedImagePaths.isNotEmpty()
            -> actions.onCloseSelection()

            state.showDeleteConfirmation -> actions.onEvent(MessagesUiEvent.DismissDeleteConfirmation)
            state.replyingTo != null -> actions.onEvent(MessagesUiEvent.ReplyTo(null))
            state.editingMessage != null -> actions.onEvent(MessagesUiEvent.EditMessage(null))
            state.isSearchActive -> actions.onEvent(MessagesUiEvent.ToggleSearch(false))
            state.showPinnedMessagesDialog -> actions.onEvent(
                MessagesUiEvent.TogglePinnedMessagesDialog(false)
            )

            state.showMediaPicker -> actions.onEvent(MessagesUiEvent.ToggleMediaPicker(false))
            state.showSharedMediaSheet -> actions.onEvent(MessagesUiEvent.ToggleSharedMediaSheet(false))
        }
    }
    // endregion

    // region Effects
    LaunchedEffect(state.scrollToSearchTrigger) {
        if (state.isSearchActive && state.currentSearchResultIndex != -1 && state.scrollToSearchTrigger > 0) {
            val targetId = state.searchResults[state.currentSearchResultIndex]
            scrollToAndBlink(targetId)
        }
    }

    LaunchedEffect(state.shouldScrollToPinned) {
        if (state.shouldScrollToPinned) {
            state.pinnedMessages.getOrNull(state.activePinnedMessageIndex)?.let {
                scrollToAndBlink(it.id)
            }
            actions.onEvent(MessagesUiEvent.ConsumedScrollToPinned)
        }
    }

    LaunchedEffect(state.shouldScrollToBottom) {
        if (state.shouldScrollToBottom) {
            delay(50.milliseconds)
            messagesScrollState.animateScrollToItem(0)
            actions.onEvent(MessagesUiEvent.ConsumedScrollToBottom)
        }
    }

    DisposableEffect(Unit) {
        onDispose { actions.onStopAudio() }
    }

    val isKeyboardVisible = WindowInsets.isImeVisible
    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible && messagesScrollState.firstVisibleItemIndex <= 1) {
            messagesScrollState.animateScrollToItem(0)
        }
    }
    // endregion

    CompositionLocalProvider(LocalMessagesActions provides actions) {
        var inputBarHeight by remember { mutableStateOf(0.dp) }

        StatusScaffold(
            status = state.scaffoldStatus,
            message = state.scaffoldMessage,
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            topBar = {
                MessagesTopBarSection(state, scrollBehavior)
            }
        ) { scaffoldPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = scaffoldPadding.calculateTopPadding())
                    .imePadding()
            ) {
                MessagesListSection(
                    state = state,
                    messagesScrollState = messagesScrollState,
                    inputBarHeight = inputBarHeight
                )

                MessageInputSection(
                    state = state,
                    inputBarViewModel = inputBarViewModel,
                    scaffoldPadding = scaffoldPadding,
                    onInputHeightChanged = { inputBarHeight = it }
                )

                MessagesOverlaySection(state)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessagesTopBarSection(
    state: MessagesUiState,
    scrollBehavior: TopAppBarScrollBehavior
) {
    MessagesTopBar(
        scrollBehavior = scrollBehavior,
        selectedCount = state.selectionMetrics.totalSelectedCount,
        isSearchActive = state.isSearchActive,
        searchQuery = state.searchQuery,
        searchResultsCount = state.searchResults.size,
        currentSearchIndex = state.currentSearchResultIndex,
        isImageSelectionOnly = state.selectionMetrics.selectedMessagesCount == 0 && state.selectionMetrics.selectedImagesCount > 0,
        canDelete = state.selectionMetrics.totalDeletableCount > 0
    )
}

@Composable
private fun BoxScope.MessagesListSection(
    state: MessagesUiState,
    messagesScrollState: LazyListState,
    inputBarHeight: Dp
) {
    val scope = rememberCoroutineScope()
    val showScrollToBottom = rememberScrollToBottomVisibility(messagesScrollState)

    MessagesContent(
        modifier = Modifier.fillMaxSize(),
        state = state,
        tags = state.hashtags,
        listState = messagesScrollState,
        totalDeletableCount = state.selectionMetrics.totalDeletableCount,
        totalSelectedCount = state.selectionMetrics.totalSelectedCount,
        bottomPadding = inputBarHeight
    )

    ScrollToBottomFab(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = inputBarHeight + 8.dp, end = 16.dp),
        isVisible = showScrollToBottom,
        onClick = {
            scope.launch {
                messagesScrollState.animateScrollToItem(0)
            }
        }
    )
}

@Composable
private fun BoxScope.MessageInputSection(
    state: MessagesUiState,
    inputBarViewModel: InputBarViewModel,
    scaffoldPadding: PaddingValues,
    onInputHeightChanged: (Dp) -> Unit
) {
    val actions = LocalMessagesActions.current
    val density = LocalDensity.current

    InputBar(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .imeClosedBottomInset(bottom = 16.dp)
            .padding(bottom = scaffoldPadding.calculateBottomPadding())
            .onGloballyPositioned { coordinates ->
                onInputHeightChanged(with(density) { coordinates.size.height.toDp() })
            },
        viewModel = inputBarViewModel,
        messageInput = when {
            state.editingMessage != null -> state.editingMessage.editedText.orEmpty()
                .ifEmpty { state.editingMessage.text }
            state.sharedText != null -> state.sharedText
            else -> emptyString()
        },
        replyingTo = state.replyingTo,
        editingMessage = state.editingMessage,
        sharedImageUris = state.sharedImageUris,
        onCancelSharedImage = { actions.onEvent(MessagesUiEvent.ClearSharedImage) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessagesOverlaySection(
    state: MessagesUiState
) {
    val actions = LocalMessagesActions.current

    if (state.showMediaPicker) {
        MediaPickerSheet(
            onMediaSelected = { uris, type, description ->
                actions.onEvent(
                    MessagesUiEvent.SendMediaMessages(
                        uris = uris.map { it.toString() },
                        type = type,
                        description = description
                    )
                )
            },
            onDismiss = { actions.onEvent(MessagesUiEvent.ToggleMediaPicker(false)) }
        )
    }

    if (state.showSharedMediaSheet) {
        SharedMediaSheet(
            onDismiss = { actions.onEvent(MessagesUiEvent.ToggleSharedMediaSheet(false)) },
            onScrollToMessage = { id ->
                actions.onEvent(MessagesUiEvent.ToggleSharedMediaSheet(false))
                actions.onScrollToMessage(id)
            }
        )
    }

    if (state.showChangelogDialog) {
        ChangelogDialog(
            onDismiss = { actions.onEvent(MessagesUiEvent.DismissChangelog) }
        )
    }
}
