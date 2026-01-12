package off.kys.backtalk.presentation.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import off.kys.backtalk.R
import off.kys.backtalk.common.base.BaseLockActivity
import off.kys.backtalk.presentation.screen.messages.MessagesScreen
import off.kys.backtalk.presentation.theme.BacktalkTheme
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.state.MainUiState
import off.kys.backtalk.presentation.viewmodel.MainViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.minutes

class MainActivity : BaseLockActivity() {

    override val autoLockTimeout: Long
        get() = 1.minutes.inWholeMilliseconds

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = koinViewModel<MainViewModel>()
            val updateState by viewModel.updateState.collectAsState()

            BacktalkTheme {
                Navigator(MessagesScreen()) {
                    if (isLoggedIn) {
                        CurrentScreen()
                    } else {
                        LockedView()
                    }

                    // Trigger update check
                    LaunchedEffect(key1 = Unit) {
                        viewModel.onEvent(MainUiEvent.CheckUpdate)
                    }

                    // Show dialog if needed
                    if (updateState is MainUiState.UpdateAvailable) {
                        val result = (updateState as MainUiState.UpdateAvailable).result
                        AppUpdateDialog(
                            updateResult = result,
                            onDismissRequest = {
                                viewModel.onEvent(MainUiEvent.DismissDialog)
                            },
                            onUpdateClick = {
                                viewModel.onEvent(MainUiEvent.UpdateNow(result.downloadUrls.first().browserDownloadUrl))
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun LockedView(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.the_app_is_locked),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}