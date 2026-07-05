package off.kys.backtalk.presentation.viewmodel

import android.app.Application
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.R
import off.kys.backtalk.common.Constants
import off.kys.backtalk.common.manager.AlarmScheduler
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.common.registry.CaptionWordsRegistry
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import off.kys.backtalk.presentation.components.status_scaffold.ScaffoldStatus
import off.kys.backtalk.presentation.components.status_scaffold.StatusMessage
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.model.MessageUiModel
import off.kys.backtalk.presentation.state.messages.MessagesUiState
import off.kys.backtalk.presentation.state.messages.SelectionMetrics
import off.kys.backtalk.util.HashUtils
import off.kys.backtalk.util.MediaUtils
import off.kys.backtalk.util.WorkScheduler
import off.kys.backtalk.util.emptyString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for the Messages screen.
 */
class MessagesViewModel(
    private val useCases: MessagesUseCases,
    private val preferences: BacktalkPreferences,
    private val application: Application
) : AndroidViewModel(application), KoinComponent {

    private val alarmScheduler: AlarmScheduler by inject()
    private val captionRegistry: CaptionWordsRegistry by inject()

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private var blinkJob: Job? = null

    init {
        onEvent(MessagesUiEvent.LoadMessages)
        updateChangelog()
    }

    private fun updateChangelog() {
        _uiState.update { it.copy(showChangelogDialog = preferences.lastSeenChangelogVersion != BuildConfig.VERSION_NAME) }
    }

    fun onEvent(event: MessagesUiEvent) {
        when (event) {
            is MessagesUiEvent.LoadMessages -> loadMessages()
            is MessagesUiEvent.SendMessage -> sendMessage(event.text)
            is MessagesUiEvent.SendVoiceMessage -> sendVoiceMessage(
                event.path,
                event.duration,
                event.waveform
            )

            is MessagesUiEvent.ReplyTo -> updateReply(event.message)
            is MessagesUiEvent.EditMessage -> updateEditingMessage(event.message)

            is MessagesUiEvent.ToggleSelection -> toggleSelection(event.id)
            is MessagesUiEvent.ClearSelection -> clearSelection()

            is MessagesUiEvent.DeleteSelected -> {
                _uiState.update { it.copy(showDeleteConfirmation = true) }
            }

            is MessagesUiEvent.ConfirmDeleteSelected -> {
                deleteSelected()
                deleteSelectedImages()
                _uiState.update { it.copy(showDeleteConfirmation = false) }
            }

            is MessagesUiEvent.DismissDeleteConfirmation -> {
                _uiState.update { it.copy(showDeleteConfirmation = false) }
            }

            is MessagesUiEvent.CopySelected -> copySelected()

            is MessagesUiEvent.ToggleSearch -> toggleSearch(event.active)
            is MessagesUiEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is MessagesUiEvent.NavigateSearch -> navigateSearch(event.up)
            is MessagesUiEvent.ScheduleMessage -> scheduleMessage(event.text, event.scheduledTime)
            MessagesUiEvent.DismissPermissionRationale -> {
                _uiState.update { it.copy(showPermissionRationale = false) }
            }

            is MessagesUiEvent.SelectTag -> {
                val newTag = if (_uiState.value.selectedTag == event.tag) null else event.tag
                _uiState.update { it.copy(selectedTag = newTag) }
                updateFilteredMessages()
            }

            is MessagesUiEvent.TogglePinMessage -> togglePinMessage(event.id, event.isPinned)
            is MessagesUiEvent.NavigatePinned -> navigatePinned()
            is MessagesUiEvent.TogglePinnedMessagesDialog -> {
                _uiState.update { it.copy(showPinnedMessagesDialog = event.show) }
            }

            is MessagesUiEvent.ScrollToMessage -> {
                val state = _uiState.value
                val pinnedIndex = state.pinnedMessages.indexOfFirst { it.id == event.id }
                _uiState.update {
                    it.copy(
                        showPinnedMessagesDialog = false,
                        selectedTag = null,
                        activePinnedMessageIndex = if (pinnedIndex != -1) pinnedIndex else state.activePinnedMessageIndex
                    )
                }
                updateFilteredMessages()
            }

            is MessagesUiEvent.BlinkMessage -> {
                blinkMessage(event.id)
            }

            is MessagesUiEvent.ToggleMediaPicker -> {
                _uiState.update { it.copy(showMediaPicker = event.show) }
            }

            is MessagesUiEvent.ToggleSharedMediaSheet -> {
                _uiState.update { it.copy(showSharedMediaSheet = event.show) }
            }

            is MessagesUiEvent.SendMediaMessages -> {
                sendMediaMessages(event.uris, event.type, event.description)
            }

            MessagesUiEvent.ConsumedScrollToBottom -> {
                _uiState.update { it.copy(shouldScrollToBottom = false) }
            }

            MessagesUiEvent.ConsumedScrollToPinned -> {
                _uiState.update { it.copy(shouldScrollToPinned = false) }
            }

            is MessagesUiEvent.RemoveImageFromMessage -> removeImageFromMessage(
                event.messageId,
                event.imagePath
            )

            is MessagesUiEvent.ToggleImageSelection -> toggleImageSelection(
                event.messageId,
                event.imagePath
            )

            is MessagesUiEvent.DeleteSelectedImages -> {
                _uiState.update { it.copy(showDeleteConfirmation = true) }
            }

            is MessagesUiEvent.ClearImageSelection -> clearImageSelection()
            MessagesUiEvent.DismissChangelog -> {
                preferences.lastSeenChangelogVersion = BuildConfig.VERSION_NAME
                _uiState.update { it.copy(showChangelogDialog = false) }
            }

            MessagesUiEvent.RefreshSettings -> {
                _uiState.update {
                    it.copy(
                        showTagsBar = preferences.showTagsBar,
                        hapticFeedbackEnabled = preferences.hapticFeedbackEnabled,
                        swipeHintShown = preferences.swipeHintShown,
                        externalLinkWarningEnabled = preferences.externalLinkWarningEnabled,
                        disableContextMenuOnLongClick = preferences.disableContextMenuOnLongClick
                    )
                }
            }

            is MessagesUiEvent.SetSharedText -> {
                _uiState.update {
                    it.copy(
                        sharedText = event.text,
                        editingMessage = null,
                        replyingTo = null
                    )
                }
            }

            MessagesUiEvent.ClearSharedText -> {
                _uiState.update { it.copy(sharedText = null) }
            }

            is MessagesUiEvent.SetSharedImage -> {
                _uiState.update {
                    it.copy(
                        sharedImageUris = event.uris.toPersistentList(),
                        editingMessage = null,
                        replyingTo = null
                    )
                }
            }

            MessagesUiEvent.ClearSharedImage -> {
                _uiState.update { it.copy(sharedImageUris = persistentListOf()) }
            }

            is MessagesUiEvent.ShowMessageContextMenu -> {
                _uiState.update { it.copy(messageContextMenuEntity = event.message) }
            }

            is MessagesUiEvent.CopyMessage -> {
                viewModelScope.launch {
                    useCases.copyMessagesByIds(setOf(event.message.id))
                }
                _uiState.update { it.copy(messageContextMenuEntity = null) }
            }

            is MessagesUiEvent.DeleteMessage -> {
                clearSelection()
                toggleSelection(event.message.id)
                _uiState.update {
                    it.copy(
                        showDeleteConfirmation = true,
                        messageContextMenuEntity = null
                    )
                }
            }

            MessagesUiEvent.MarkSwipeHintShown -> {
                preferences.swipeHintShown = true
                _uiState.update { it.copy(swipeHintShown = true) }
            }
        }
    }

    private fun sendMediaMessages(uris: List<String>, type: String, description: String?) {
        val replyTo = _uiState.value.replyingTo
        val smartPointing = preferences.smartImagePointingEnabled

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                var actualMediaType = type
                val mediaPaths = uris.mapNotNull { uri ->
                    val sourceUri = uri.toUri()
                    val currentType = if (type == "image/*") {
                        application.contentResolver.getType(sourceUri) ?: type
                    } else type
                    actualMediaType = currentType
                    val extension =
                        MimeTypeMap.getSingleton().getExtensionFromMimeType(currentType) ?: "jpg"
                    val mediaDir = File(application.filesDir, "media").apply { mkdirs() }
                    val fileName = if (smartPointing) {
                        application.contentResolver.openInputStream(sourceUri)?.use {
                            HashUtils.calculateSha256(it)
                        } ?: System.currentTimeMillis().toString()
                    } else {
                        "media_${System.currentTimeMillis()}_${sourceUri.lastPathSegment}.$extension"
                    }
                    val destFile = File(mediaDir, fileName)
                    if (smartPointing && destFile.exists()) return@mapNotNull destFile.absolutePath
                    application.contentResolver.openInputStream(sourceUri)?.use { input ->
                        destFile.outputStream().use { input.copyTo(it) }
                    }
                    if (preferences.removeImageMetadataEnabled && currentType.startsWith("image/") && currentType != "image/gif") {
                        MediaUtils.stripImageMetadata(destFile)
                    }
                    destFile.absolutePath
                }

                if (mediaPaths.isNotEmpty()) {
                    mediaPaths.chunked(4).forEachIndexed { index, chunk ->
                        val isLastChunk = index == (mediaPaths.size / 4)
                        useCases.insertMessage(
                            MessageEntity(
                                id = MessageId.generate(),
                                text = if (isLastChunk) description
                                    ?: emptyString() else emptyString(),
                                timestamp = System.currentTimeMillis() + index,
                                repliedToId = replyTo?.id,
                                mediaPaths = chunk,
                                mediaType = actualMediaType
                            )
                        )
                    }
                    WorkScheduler.scheduleReminders(application, preferences, forceReplace = true)
                }
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(shouldScrollToBottom = true)
                }
            }
        }
        updateReply(null)
        _uiState.value = _uiState.value.copy(showMediaPicker = false)
    }

    private fun blinkMessage(id: MessageId?) {
        blinkJob?.cancel()
        blinkJob = viewModelScope.launch {
            _uiState.update { it.copy(blinkMessageId = id) }
            if (id != null) {
                delay(1920.milliseconds)
                _uiState.update { it.copy(blinkMessageId = null) }
            }
        }
    }

    private fun togglePinMessage(id: MessageId, isPinned: Boolean) {
        viewModelScope.launch {
            useCases.togglePinMessage(id, isPinned)
        }
    }

    private fun navigatePinned() {
        val state = _uiState.value
        if (state.pinnedMessages.isEmpty()) return
        val nextIndex = (state.activePinnedMessageIndex + 1) % state.pinnedMessages.size
        _uiState.update {
            it.copy(
                activePinnedMessageIndex = nextIndex,
                shouldScrollToPinned = true,
                selectedTag = null
            )
        }
        updateFilteredMessages()
    }

    private fun scheduleMessage(text: String, scheduledTime: Long) {
        if (!alarmScheduler.canScheduleExactAlarms()) {
            _uiState.update { it.copy(showPermissionRationale = true) }
            return
        }
        val trimmedText = if (preferences.trimMessagesEnabled) text.trim() else text
        viewModelScope.launch {
            useCases.scheduleMessage(
                text = trimmedText,
                scheduledTime = scheduledTime,
                repliedToId = _uiState.value.replyingTo?.id
            )
            showScaffoldMessage(
                StatusMessage.Resource(R.string.message_scheduled_success),
                ScaffoldStatus.Info
            )
        }
        updateReply(null)
    }

    private var scaffoldMessageJob: Job? = null

    private fun showScaffoldMessage(message: StatusMessage, status: ScaffoldStatus) {
        scaffoldMessageJob?.cancel()
        scaffoldMessageJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    scaffoldMessage = message,
                    scaffoldStatus = status
                )
            }
            delay(3000)
            _uiState.update {
                it.copy(
                    scaffoldMessage = null,
                    scaffoldStatus = ScaffoldStatus.None
                )
            }
        }
    }

    private var isMigrationDone = false

    private fun loadMessages() {
        _uiState.update {
            it.copy(
                showTagsBar = preferences.showTagsBar,
                hapticFeedbackEnabled = preferences.hapticFeedbackEnabled,
                swipeHintShown = preferences.swipeHintShown,
                externalLinkWarningEnabled = preferences.externalLinkWarningEnabled,
                disableContextMenuOnLongClick = preferences.disableContextMenuOnLongClick
            )
        }
        viewModelScope.launch {
            useCases.getAllMessages().collect { messages ->
                if (!isMigrationDone) {
                    migrateVoiceMessages(messages)
                    isMigrationDone = true
                }

                withContext(Dispatchers.Default) {
                    val uiModels =
                        messages.sortedBy { it.timestamp }.map { it.toUiModel() }.toPersistentList()
                    val pinned = uiModels.filter { it.isPinned }.reversed().toPersistentList()
                    val hashtags = extractHashtags(uiModels).toPersistentList()
                    val repliedMessagesMap = resolveRepliedMessages(uiModels).toPersistentMap()

                    withContext(Dispatchers.Main) {
                        _uiState.update { state ->
                            val newState = state.copy(
                                messages = uiModels,
                                pinnedMessages = pinned,
                                hashtags = hashtags,
                                repliedMessagesMap = repliedMessagesMap,
                                isLoading = false,
                                activePinnedMessageIndex = if (pinned.isEmpty()) 0 else state.activePinnedMessageIndex % pinned.size
                            )
                            newState.copy(selectionMetrics = calculateSelectionMetrics(newState))
                        }
                        updateFilteredMessages()
                    }
                }
            }
        }
    }

    private fun extractHashtags(messages: List<MessageUiModel>): List<String> {
        val hashtagRegex = Regex("""#(\w+)""")
        return messages.flatMap { message ->
            hashtagRegex.findAll(message.visibleText).map { it.groupValues[1] }.toList()
        }.distinct().sorted()
    }

    private fun resolveRepliedMessages(uiModels: List<MessageUiModel>): Map<MessageId, MessageUiModel> {
        val allMessages = uiModels.associateBy { it.id }
        return uiModels.mapNotNull { message ->
            message.repliedToId?.let { repliedId ->
                val repliedMessage = allMessages[repliedId]
                if (repliedMessage != null) {
                    message.id to repliedMessage
                } else null
            }
        }.toMap()
    }

    private fun calculateSelectionMetrics(state: MessagesUiState): SelectionMetrics {
        val selectedMessagesCount = state.selectedMessageIds.size
        val selectedImagesCount = state.selectedImagePaths.values.sumOf { it.size }

        val totalSelectedCount = selectedMessagesCount + state.selectedImagePaths.filterKeys {
            it !in state.selectedMessageIds
        }.values.sumOf { it.size }

        val deletableMessagesCount = state.messages.count {
            it.id in state.selectedMessageIds && it.isLocked.not()
        }

        val deletableImagesCount =
            state.selectedImagePaths.filterKeys { it !in state.selectedMessageIds }.entries.sumOf { (messageId, paths) ->
                val message = state.messages.find { it.id == messageId }
                if (message != null && message.isLocked.not()) {
                    paths.size
                } else 0
            }

        return SelectionMetrics(
            selectedMessagesCount = selectedMessagesCount,
            selectedImagesCount = selectedImagesCount,
            totalSelectedCount = totalSelectedCount,
            totalDeletableCount = deletableMessagesCount + deletableImagesCount
        )
    }

    private fun MessageEntity.toUiModel(): MessageUiModel {
        val currentTime = System.currentTimeMillis()
        val visibleText = editedText ?: text
        val isDefaultCaption = captionRegistry.isRestricted(visibleText)
        val hasImages = !mediaPath.isNullOrEmpty() || !mediaPaths.isNullOrEmpty()
        val hasVoice = voicePath != null
        val hasText = visibleText.isNotEmpty() && !((hasImages || hasVoice) && isDefaultCaption)

        return MessageUiModel(
            id = id,
            text = text,
            timestamp = timestamp,
            repliedToId = repliedToId,
            editedText = editedText,
            editedAt = editedAt,
            voicePath = voicePath,
            voiceDuration = voiceDuration,
            waveformData = waveformData?.toPersistentList(),
            isReminder = isReminder,
            originalCreationTimestamp = originalCreationTimestamp,
            scheduledTimestamp = scheduledTimestamp,
            isPinned = isPinned,
            mediaPath = mediaPath,
            mediaPaths = mediaPaths?.toPersistentList(),
            mediaType = mediaType,
            isDefaultCaption = isDefaultCaption,
            isLocked = (currentTime - timestamp) >= Constants.MESSAGE_EDIT_DELETE_WINDOW,
            canEdit = editedAt == null && (currentTime - timestamp) < Constants.MESSAGE_EDIT_DELETE_WINDOW && voicePath == null,
            hasImages = hasImages,
            hasVoice = hasVoice,
            hasText = hasText,
            hasRepliedMessage = repliedToId != null,
            hasTags = isReminder || isPinned,
            isImageOnly = hasImages && !hasText && !hasVoice && repliedToId == null && !isReminder && !isPinned,
            visibleText = visibleText
        )
    }

    private fun migrateVoiceMessages(messages: List<MessageEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            val cachePath = application.cacheDir.absolutePath
            val voiceDir = File(application.filesDir, "voice").apply { mkdirs() }
            messages.filter { it.voicePath?.startsWith(cachePath) == true }.forEach { message ->
                val oldFile = File(message.voicePath!!)
                if (oldFile.exists()) {
                    val newFile = File(voiceDir, oldFile.name)
                    runCatching {
                        oldFile.copyTo(newFile, overwrite = true)
                        useCases.insertMessage(message.copy(voicePath = newFile.absolutePath))
                        oldFile.delete()
                    }
                }
            }
        }
    }

    private fun updateFilteredMessages() {
        val state = _uiState.value
        val filtered = if (state.selectedTag == null) state.messages
        else state.messages.filter {
            it.visibleText.contains(
                "#${state.selectedTag}",
                ignoreCase = true
            )
        }.ifEmpty { state.messages }.toPersistentList()
        _uiState.update { it.copy(filteredMessages = filtered) }
    }

    private fun sendMessage(text: String) {
        val editingMessage = _uiState.value.editingMessage
        val trimmedText = if (preferences.trimMessagesEnabled) text.trim() else text

        if (editingMessage != null) {
            if (editingMessage.isLocked) {
                updateEditingMessage(null)
                return
            }
            viewModelScope.launch {
                val entity = useCases.getMessageById(editingMessage.id) ?: return@launch
                useCases.insertMessage(
                    entity.copy(
                        editedText = trimmedText,
                        editedAt = System.currentTimeMillis()
                    )
                )
            }
            updateEditingMessage(null)
            return
        }

        viewModelScope.launch {
            useCases.insertMessage(
                MessageEntity(
                    id = MessageId.generate(),
                    text = trimmedText,
                    timestamp = System.currentTimeMillis(),
                    repliedToId = _uiState.value.replyingTo?.id
                )
            )
            WorkScheduler.scheduleReminders(application, preferences, forceReplace = true)
            _uiState.update { it.copy(shouldScrollToBottom = true, sharedText = null) }
        }
        updateReply(null)
    }

    private fun removeImageFromMessage(messageId: MessageId, imagePath: String) {
        val message = _uiState.value.messages.find { it.id == messageId }
        if (message != null && !message.isLocked) {
            viewModelScope.launch { useCases.removeImageFromMessage(messageId, imagePath) }
        }
    }

    private fun toggleImageSelection(messageId: MessageId, imagePath: String) {
        _uiState.update { state ->
            val currentMap = state.selectedImagePaths
            val currentSet = currentMap[messageId] ?: persistentSetOf()
            val newSet =
                if (imagePath in currentSet) currentSet.removing(imagePath) else currentSet.adding(
                    imagePath
                )
            val newMap =
                if (newSet.isEmpty()) currentMap.removing(messageId) else currentMap.putting(
                    messageId,
                    newSet
                )
            val newState = state.copy(selectedImagePaths = newMap)
            newState.copy(selectionMetrics = calculateSelectionMetrics(newState))
        }
    }

    private fun deleteSelected() {
        val ids = _uiState.value.selectedMessageIds
        val messages = _uiState.value.messages
        viewModelScope.launch {
            ids.forEach { id ->
                val message = messages.find { it.id == id }
                if (message != null && !message.isLocked) {
                    useCases.deleteMessageById(id)
                }
            }
        }
        _uiState.update { state ->
            val newState = state.copy(selectedMessageIds = persistentSetOf())
            newState.copy(selectionMetrics = calculateSelectionMetrics(newState))
        }
    }

    private fun deleteSelectedImages() {
        val selectedImagePaths = _uiState.value.selectedImagePaths
        val messages = _uiState.value.messages
        viewModelScope.launch {
            selectedImagePaths.forEach { (messageId, paths) ->
                val message = messages.find { it.id == messageId }
                if (messageId !in _uiState.value.selectedMessageIds && message != null && !message.isLocked) {
                    useCases.removeImagesFromMessage(messageId, paths)
                }
            }
        }
        _uiState.update { state ->
            val newState = state.copy(selectedImagePaths = persistentMapOf())
            newState.copy(selectionMetrics = calculateSelectionMetrics(newState))
        }
    }

    private fun clearImageSelection() {
        _uiState.update { state ->
            val newState = state.copy(selectedImagePaths = persistentMapOf())
            newState.copy(selectionMetrics = calculateSelectionMetrics(newState))
        }
    }

    private fun sendVoiceMessage(path: String, duration: Long, waveform: List<Float>) {
        val replyTo = _uiState.value.replyingTo
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val voiceDir = File(application.filesDir, "voice").apply { mkdirs() }
                val sourceFile = File(path)
                val destFile = File(voiceDir, "voice_${System.currentTimeMillis()}.m4a")
                sourceFile.inputStream()
                    .use { input -> destFile.outputStream().use { input.copyTo(it) } }
                sourceFile.delete()

                useCases.insertMessage(
                    MessageEntity(
                        id = MessageId.generate(),
                        text = application.getString(R.string.chat_media_voice),
                        timestamp = System.currentTimeMillis(),
                        repliedToId = replyTo?.id,
                        voicePath = destFile.absolutePath,
                        voiceDuration = duration,
                        waveformData = waveform
                    )
                )
                WorkScheduler.scheduleReminders(application, preferences, forceReplace = true)
                withContext(Dispatchers.Main) { _uiState.update { it.copy(shouldScrollToBottom = true) } }
            }
        }
        updateReply(null)
    }

    private fun updateEditingMessage(message: MessageUiModel?) {
        _uiState.update { it.copy(editingMessage = message, replyingTo = null) }
    }

    private fun updateReply(message: MessageUiModel?) {
        _uiState.update { it.copy(replyingTo = message, editingMessage = null) }
    }

    private fun toggleSelection(id: MessageId) {
        _uiState.update { state ->
            val current = state.selectedMessageIds
            val newSet = if (id in current) current.removing(id) else current.adding(id)
            val newState = state.copy(selectedMessageIds = newSet)
            newState.copy(selectionMetrics = calculateSelectionMetrics(newState))
        }
    }

    private fun clearSelection() {
        _uiState.update { state ->
            val newState = state.copy(
                selectedMessageIds = persistentSetOf(),
                selectedImagePaths = persistentMapOf()
            )
            newState.copy(selectionMetrics = calculateSelectionMetrics(newState))
        }
    }

    private fun copySelected() {
        viewModelScope.launch { useCases.copyMessagesByIds(_uiState.value.selectedMessageIds) }
        clearSelection()
    }

    private fun toggleSearch(active: Boolean) {
        _uiState.update {
            it.copy(
                isSearchActive = active,
                searchQuery = if (active) it.searchQuery else emptyString(),
                searchResults = if (active) it.searchResults else persistentListOf(),
                currentSearchResultIndex = if (active) it.currentSearchResultIndex else -1
            )
        }
    }

    private fun updateSearchQuery(query: String) {
        if (query == _uiState.value.searchQuery) return
        viewModelScope.launch(Dispatchers.Default) {
            val results = if (query.isBlank()) persistentListOf()
            else {
                val terms = query.trim().lowercase().split(Regex("\\s+"))
                _uiState.value.messages.asSequence()
                    .filter { message ->
                        terms.all { term ->
                            message.visibleText.lowercase().contains(term)
                        }
                    }
                    .map { it.id }.toList().reversed().toPersistentList()
            }
            _uiState.update {
                it.copy(
                    searchQuery = query,
                    searchResults = results,
                    currentSearchResultIndex = if (results.isNotEmpty()) 0 else -1
                )
            }
        }
    }

    private fun navigateSearch(up: Boolean) {
        _uiState.update {
            if (it.searchResults.isEmpty()) return@update it
            val newIndex = if (up) (it.currentSearchResultIndex + 1) % it.searchResults.size
            else (it.currentSearchResultIndex - 1 + it.searchResults.size) % it.searchResults.size
            it.copy(
                currentSearchResultIndex = newIndex,
                scrollToSearchTrigger = it.scrollToSearchTrigger + 1
            )
        }
    }
}
