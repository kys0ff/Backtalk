package off.kys.backtalk.presentation.screen.threads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.backtalk.R
import off.kys.backtalk.presentation.components.HintTooltip
import off.kys.backtalk.presentation.event.ThreadsUiEvent
import off.kys.backtalk.presentation.screen.threads.components.ThreadItem
import off.kys.backtalk.presentation.viewmodel.ThreadsViewModel
import off.kys.backtalk.util.copyToClipboard
import off.kys.backtalk.util.shareText
import org.koin.compose.viewmodel.koinViewModel

class ThreadsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<ThreadsViewModel>()
        val state by viewModel.uiState

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
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = { viewModel.onEvent(ThreadsUiEvent.LoadThreads) }
                ) {
                    if (state.threads.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.threads_empty),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn {
                            items(state.threads, key = { it.root.id() }) { thread ->
                                ThreadItem(
                                    thread = thread,
                                    onClick = { navigator.push(ThreadDetailScreen(thread)) },
                                    onThreadCopy = { text ->
                                        context.copyToClipboard(text)
                                    },
                                    onThreadShare = { text ->
                                        context.shareText(text)
                                    },
                                    onQuoteClick = { message ->
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
                }
            }
        }
    }
}
