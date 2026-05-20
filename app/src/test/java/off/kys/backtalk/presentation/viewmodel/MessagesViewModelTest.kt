package off.kys.backtalk.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import off.kys.backtalk.common.manager.AlarmScheduler
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import off.kys.backtalk.presentation.event.MessagesUiEvent
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
    private val alarmScheduler: AlarmScheduler = mockk(relaxed = true)
    private val application: android.app.Application = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        coEvery { useCases.getAllMessages() } returns flowOf(emptyList())

        startKoin {
            modules(module {
                single { alarmScheduler }
            })
        }

        viewModel = MessagesViewModel(useCases, application)
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
        viewModel.onEvent(MessagesUiEvent.ToggleSelection(messageId))
        viewModel.onEvent(MessagesUiEvent.DeleteSelected)
        assertTrue(viewModel.uiState.value.showDeleteConfirmation)

        // When
        viewModel.onEvent(MessagesUiEvent.ConfirmDeleteSelected)

        // Then
        coVerify { useCases.deleteMessageById(messageId) }
        assertFalse(viewModel.uiState.value.showDeleteConfirmation)
        assertTrue(viewModel.uiState.value.selectedMessageIds.isEmpty())
    }
}
