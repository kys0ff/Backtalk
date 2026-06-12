package off.kys.backtalk.presentation.screen.messages

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import off.kys.backtalk.presentation.viewmodel.MessagesViewModel
import off.kys.backtalk.util.AudioPlayer
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

class MessagesScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val viewModel =
            koinViewModel<MessagesViewModel>(viewModelStoreOwner = context as MainActivity)
        val audioPlayer = koinInject<AudioPlayer>()
        val state by viewModel.uiState

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.onEvent(MessagesUiEvent.RefreshSettings)
            }
        }

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