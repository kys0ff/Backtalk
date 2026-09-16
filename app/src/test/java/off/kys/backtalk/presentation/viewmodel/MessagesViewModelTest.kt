package off.kys.backtalk.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import off.kys.backtalk.common.manager.AlarmScheduler
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.common.registry.CaptionWordsRegistry
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import off.kys.backtalk.presentation.event.MessagesUiEvent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesViewModelTest : KoinTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: MessagesViewModel
    private val useCases: MessagesUseCases = mockk(relaxed = true)
    private val preferences: BacktalkPreferences = mockk(relaxed = true)
    private val alarmScheduler: AlarmScheduler = mockk(relaxed = true)
    private val application: Application = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { application.cacheDir } returns File("/tmp/cache")
        every { application.filesDir } returns File("/tmp/files")
        every { useCases.getAllMessages() } returns flowOf(emptyList())

        startKoin {
            modules(module {
                single { alarmScheduler }
                single { mockk<CaptionWordsRegistry>(relaxed = true) }
            })
        }

        viewModel = MessagesViewModel(useCases, preferences, application)
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
        stopKoin()
    }

    private fun createMessageEntity(id: MessageId, text: String): MessageEntity {
        return MessageEntity(
            id = id,
            text = text,
            timestamp = System.currentTimeMillis(),
            repliedToId = null,
            editedText = null,
            editedAt = null,
            voicePath = null,
            voiceDuration = null,
            waveformData = null,
            isReminder = false,
            originalCreationTimestamp = null,
            scheduledTimestamp = null,
            isPinned = false,
            mediaPath = null,
            mediaPaths = null,
            mediaType = null
        )
    }

    @Test
    fun `DeleteSelected event should show confirmation dialog`() {
        // Given
        val messageId = MessageId.generate()
        viewModel.onEvent(MessagesUiEvent.ToggleSelection(messageId))

        // When
        viewModel.onEvent(MessagesUiEvent.DeleteSelected)

        // Then
        assertTrue(viewModel.uiState.value.showDeleteConfirmation)
    }

    @Test
    fun `DismissDeleteConfirmation event should hide confirmation dialog`() {
        // Given
        viewModel.onEvent(MessagesUiEvent.DeleteSelected)
        assertTrue(viewModel.uiState.value.showDeleteConfirmation)

        // When
        viewModel.onEvent(MessagesUiEvent.DismissDeleteConfirmation)

        // Then
        assertFalse(viewModel.uiState.value.showDeleteConfirmation)
    }

    @Test
    fun `ConfirmDeleteSelected event should delete messages and hide dialog`() {
        // Given
        val messageId = MessageId.generate()
        val message = createMessageEntity(messageId, "test")
        
        every { useCases.getAllMessages() } returns flowOf(listOf(message))
        coEvery { useCases.getMessageById(messageId) } returns message
        viewModel.onEvent(MessagesUiEvent.LoadMessages)

        viewModel.onEvent(MessagesUiEvent.ToggleSelection(messageId))
        viewModel.onEvent(MessagesUiEvent.DeleteSelected)
        assertTrue(viewModel.uiState.value.showDeleteConfirmation)

        // When
        val deleteUseCase = useCases.deleteMessageById
        viewModel.onEvent(MessagesUiEvent.ConfirmDeleteSelected)

        // Then
        coVerify { deleteUseCase(messageId) }
        assertFalse(viewModel.uiState.value.showDeleteConfirmation)
        assertTrue(viewModel.uiState.value.selectedMessageIds.isEmpty())
    }

    @Test
    fun `ToggleImageSelection event should update selectedImagePaths`() {
        // Given
        val messageId = MessageId.generate()
        val path = "/path/to/image.jpg"

        // When
        viewModel.onEvent(MessagesUiEvent.ToggleImageSelection(messageId, path))

        // Then
        val selected = viewModel.uiState.value.selectedImagePaths[messageId]
        assertTrue(selected?.contains(path) == true)

        // When toggle again
        viewModel.onEvent(MessagesUiEvent.ToggleImageSelection(messageId, path))

        // Then
        assertFalse(viewModel.uiState.value.selectedImagePaths.containsKey(messageId))
    }

    @Test
    fun `DeleteSelectedImages event should call use case and clear selection`() {
        // Given
        val messageId = MessageId.generate()
        val path1 = "/path/to/image1.jpg"
        val path2 = "/path/to/image2.jpg"
        val message = createMessageEntity(messageId, "test")

        every { useCases.getAllMessages() } returns flowOf(listOf(message))
        coEvery { useCases.getMessageById(messageId) } returns message
        viewModel.onEvent(MessagesUiEvent.LoadMessages)

        viewModel.onEvent(MessagesUiEvent.ToggleImageSelection(messageId, path1))
        viewModel.onEvent(MessagesUiEvent.ToggleImageSelection(messageId, path2))

        // When - Should show confirmation first
        viewModel.onEvent(MessagesUiEvent.DeleteSelectedImages)

        // Then confirm
        val removeUseCase = useCases.removeImagesFromMessage
        viewModel.onEvent(MessagesUiEvent.ConfirmDeleteSelected)

        // Then verify
        coVerify(timeout = 2000) { removeUseCase(messageId, match { it.contains(path1) && it.contains(path2) }) }
    }

    @Test
    fun `ClearImageSelection event should clear all image selections`() {
        // Given
        val m1 = MessageId.generate()
        val m2 = MessageId.generate()
        viewModel.onEvent(MessagesUiEvent.ToggleImageSelection(m1, "/p1"))
        viewModel.onEvent(MessagesUiEvent.ToggleImageSelection(m2, "/p2"))

        // When
        viewModel.onEvent(MessagesUiEvent.ClearImageSelection)

        // Then
        assertTrue(viewModel.uiState.value.selectedImagePaths.isEmpty())
    }

    @Test
    fun `UndoDeleteMessages should restore deleted messages`() {
        // Given
        val messageId = MessageId.generate()
        val message = createMessageEntity(messageId, "test to undo")
        
        every { useCases.getAllMessages() } returns flowOf(listOf(message))
        coEvery { useCases.getMessageById(messageId) } returns message
        viewModel.onEvent(MessagesUiEvent.LoadMessages)

        viewModel.onEvent(MessagesUiEvent.ToggleSelection(messageId))
        viewModel.onEvent(MessagesUiEvent.ConfirmDeleteSelected)

        coVerify { useCases.deleteMessageById(messageId) }
        assertTrue(viewModel.uiState.value.recentlyDeletedMessages.isNotEmpty())

        // When
        viewModel.onEvent(MessagesUiEvent.UndoDeleteMessages)

        // Then
        coVerify { useCases.insertMessage(message) }
        assertTrue(viewModel.uiState.value.recentlyDeletedMessages.isEmpty())
    }

    @Test
    fun `ToggleStartNewThread event should update the flag`() {
        // Given
        assertFalse(viewModel.uiState.value.startNewThread)

        // When
        viewModel.onEvent(MessagesUiEvent.ToggleStartNewThread(true))

        // Then
        assertTrue(viewModel.uiState.value.startNewThread)

        // When toggled back off
        viewModel.onEvent(MessagesUiEvent.ToggleStartNewThread(false))

        // Then
        assertFalse(viewModel.uiState.value.startNewThread)
    }

    @Test
    fun `switching reply mode clears a pending startNewThread`() {
        // Given the toggle was switched on
        viewModel.onEvent(MessagesUiEvent.ToggleStartNewThread(true))
        assertTrue(viewModel.uiState.value.startNewThread)

        // When the user changes their mind and replies to a message instead
        viewModel.onEvent(MessagesUiEvent.ReplyTo(null))

        // Then the pending flag is dropped, so the reply joins the thread it replies to rather than
        // silently starting a new one while the toggle is hidden.
        assertFalse(viewModel.uiState.value.startNewThread)
    }

    @Test
    fun `switching edit mode clears a pending startNewThread`() {
        // Given the toggle was switched on
        viewModel.onEvent(MessagesUiEvent.ToggleStartNewThread(true))
        assertTrue(viewModel.uiState.value.startNewThread)

        // When the user starts editing instead
        viewModel.onEvent(MessagesUiEvent.EditMessage(null))

        // Then the flag does not survive into the next composed message.
        assertFalse(viewModel.uiState.value.startNewThread)
    }

    @Test
    fun `a message sent with startNewThread enabled becomes its own thread root`() {
        // Given an existing message that forms the current thread
        val existingId = MessageId(100)
        every { useCases.getAllMessages() } returns flowOf(listOf(createMessageEntity(existingId, "first")))
        viewModel.onEvent(MessagesUiEvent.LoadMessages)
        viewModel.onEvent(MessagesUiEvent.ToggleStartNewThread(true))

        val inserted = mutableListOf<MessageEntity>()
        coEvery { useCases.insertMessage(capture(inserted)) } returns Unit

        // When
        viewModel.onEvent(MessagesUiEvent.SendMessage("brand new thought"))

        // Then the new message is a root: threadId is null, so it is grouped on its own.
        assertTrue(inserted.isNotEmpty())
        assertEquals(null, inserted.first().threadId)
        // And the toggle resets so the next message does not silently start another thread.
        assertFalse(viewModel.uiState.value.startNewThread)
    }

    @Test
    fun `replying inherits the thread of the message being replied to`() {
        // Given a root and one of its replies
        val rootId = MessageId(200)
        val replyId = MessageId(201)
        val root = createMessageEntity(rootId, "root")
        val reply = MessageEntity(
            id = replyId,
            text = "reply",
            timestamp = System.currentTimeMillis() + 1_000,
            repliedToId = rootId,
            threadId = rootId
        )
        every { useCases.getAllMessages() } returns flowOf(listOf(root, reply))
        viewModel.onEvent(MessagesUiEvent.LoadMessages)

        val inserted = mutableListOf<MessageEntity>()
        coEvery { useCases.insertMessage(capture(inserted)) } returns Unit

        // When replying to the reply (i.e. to a deep message, not the root)
        viewModel.onEvent(MessagesUiEvent.ReplyTo(viewModel.uiState.value.messages[1]))
        viewModel.onEvent(MessagesUiEvent.SendMessage("nested reply"))

        // Then it stays inside the original thread instead of being promoted to a root.
        assertTrue(inserted.isNotEmpty())
        assertEquals(rootId, inserted.first().threadId)
        assertEquals(replyId, inserted.first().repliedToId)
    }
}
