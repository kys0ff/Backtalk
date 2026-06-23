package off.kys.backtalk.presentation.screen.messages

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.backtalk.presentation.activity.MainActivity
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.screen.messages.components.MessagesScreenContent
import off.kys.backtalk.presentation.screen.preferences.SettingsScreen
import off.kys.backtalk.presentation.screen.reminders.RemindersScreen
import off.kys.backtalk.presentation.screen.statistics.StatisticsScreen
import off.kys.backtalk.presentation.screen.threads.ThreadsScreen
import off.kys.backtalk.presentation.viewmodel.InputBarViewModel
import off.kys.backtalk.presentation.viewmodel.MessagesViewModel
import off.kys.backtalk.util.AudioPlayer
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

class MessagesScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenOwner = LocalActivity.currentOrThrow as MainActivity
        val navigator = LocalNavigator.currentOrThrow
        val viewModel =
            koinViewModel<MessagesViewModel>(viewModelStoreOwner = screenOwner)
        val inputBarViewModel = rememberInputBarViewModel(viewModel)
        val audioPlayer = koinInject<AudioPlayer>()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.onEvent(MessagesUiEvent.RefreshSettings)
            }
        }

        MessagesScreenContent(
            state = state,
            inputBarViewModel = inputBarViewModel,
            onEvent = viewModel::onEvent,
            onSettingsClick = { navigator += SettingsScreen() },
            onThreadsClick = { navigator += ThreadsScreen() },
            onRemindersClick = { navigator += RemindersScreen() },
            onStatisticsClick = { navigator += StatisticsScreen() },
            onStopAudio = { audioPlayer.stop() }
        )
    }

    @Composable
    private fun rememberInputBarViewModel(viewModel: MessagesViewModel): InputBarViewModel =
        koinViewModel(
            parameters = {
                parametersOf(
                    { text: String -> viewModel.onEvent(MessagesUiEvent.SendMessage(text)) },
                    { path: String, duration: Long, waveform: List<Float> ->
                        viewModel.onEvent(
                            MessagesUiEvent.SendVoiceMessage(
                                path,
                                duration,
                                waveform
                            )
                        )
                    },
                    { text: String, time: Long ->
                        viewModel.onEvent(
                            MessagesUiEvent.ScheduleMessage(
                                text,
                                time
                            )
                        )
                    },
                    { uris: List<String>, caption: String ->
                        viewModel.onEvent(
                            MessagesUiEvent.SendMediaMessages(
                                uris = uris,
                                type = "image/*",
                                description = caption.takeIf { it.isNotBlank() }
                            )
                        )
                    },
                    { viewModel.onEvent(MessagesUiEvent.ToggleMediaPicker(true)) },
                    { viewModel.onEvent(MessagesUiEvent.ReplyTo(null)) },
                    { viewModel.onEvent(MessagesUiEvent.EditMessage(null)) }
                )
            }
        )
}