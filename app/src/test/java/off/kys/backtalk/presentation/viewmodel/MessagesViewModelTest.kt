package off.kys.backtalk.presentation.viewmodel

import androidx.compose.runtime.MutableState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import off.kys.backtalk.common.manager.AlarmScheduler
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.state.MessagesUiState
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesViewModelTest : KoinTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: MessagesViewModel
    private val useCases: MessagesUseCases = mockk(relaxed = true)
    private val preferences: BacktalkPreferences = mockk(relaxed = true)
    private val alarmScheduler: AlarmScheduler = mockk(relaxed = true)
    private val application: android.app.Application = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { useCases.getAllMessages() } returns flowOf(emptyList())

        startKoin {
            modules(module {
                single { alarmScheduler }
            })
        }

        viewModel = MessagesViewModel(useCases, preferences, application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
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
        val message = MessageEntity(
            id = messageId,
            text = "test",
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
        
        // Inject message into state
        val stateField = MessagesViewModel::class.java.getDeclaredField("_uiState")
        stateField.isAccessible = true
        val state = stateField.get(viewModel) as MutableState<MessagesUiState>
        state.value = state.value.copy(messages = listOf(message))

        viewModel.onEvent(MessagesUiEvent.ToggleSelection(messageId))
        viewModel.onEvent(MessagesUiEvent.DeleteSelected)
        assertTrue(viewModel.uiState.value.showDeleteConfirmation)

        // When
        viewModel.onEvent(MessagesUiEvent.ConfirmDeleteSelected)

        // Then
        io.mockk.coVerify { useCases.deleteMessageById(messageId) }
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
        val message = MessageEntity(
            id = messageId,
            text = "test",
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

        // Inject message into state
        val stateField = MessagesViewModel::class.java.getDeclaredField("_uiState")
        stateField.isAccessible = true
        val state = stateField.get(viewModel) as MutableState<MessagesUiState>
        state.value = state.value.copy(messages = listOf(message))

        viewModel.onEvent(MessagesUiEvent.ToggleImageSelection(messageId, path1))
        viewModel.onEvent(MessagesUiEvent.ToggleImageSelection(messageId, path2))

        // When - Should show confirmation first
        viewModel.onEvent(MessagesUiEvent.DeleteSelectedImages)

        // Then confirm
        viewModel.onEvent(MessagesUiEvent.ConfirmDeleteSelected)

        // Then verify
        io.mockk.coVerify(timeout = 2000) { useCases.removeImagesFromMessage(messageId, match { it.contains(path1) && it.contains(path2) }) }
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
}
