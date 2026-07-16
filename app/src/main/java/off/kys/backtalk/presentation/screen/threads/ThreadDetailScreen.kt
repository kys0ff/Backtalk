package off.kys.backtalk.presentation.screen.threads

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.backtalk.R
import off.kys.backtalk.domain.model.Thread
import off.kys.backtalk.presentation.components.HintTooltip
import off.kys.backtalk.presentation.screen.threads.components.ThreadDetailContent
import off.kys.backtalk.presentation.viewmodel.ThreadsViewModel
import off.kys.backtalk.util.AudioPlayer
import off.kys.backtalk.util.copyToClipboard
import off.kys.backtalk.util.shareText
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

class ThreadDetailScreen(val thread: Thread) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val viewModel = koinViewModel<ThreadsViewModel>()
        val audioPlayer = koinInject<AudioPlayer>()

        DisposableEffect(Unit) {
            onDispose {
                audioPlayer.stop()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.threads_title)) },
                    navigationIcon = {
                        HintTooltip(stringResource(R.string.common_back)) {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(
                                    painter = painterResource(R.drawable.round_arrow_back_24),
                                    contentDescription = stringResource(R.string.common_back)
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            ThreadDetailContent(
                modifier = Modifier.padding(padding).fillMaxSize(),
                thread = thread,
                onCopy = { text ->
                    context.copyToClipboard(text)
                },
                onShare = { text ->
                    context.shareText(text)
                },
                onReplyClick = { message ->
                    val subThread = viewModel.getSubThread(message)
                    navigator.push(ThreadDetailScreen(subThread))
                },
                getReplyCount = { message ->
                    viewModel.getSubThread(message).size - 1
                }
            )
        }
    }
}
