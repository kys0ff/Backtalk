package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.event.SharedMediaUiEvent
import off.kys.backtalk.presentation.state.messages.LinkItemUiModel
import off.kys.backtalk.presentation.state.messages.MediaItemUiModel
import off.kys.backtalk.presentation.state.messages.SharedMediaUiState
import off.kys.backtalk.presentation.state.messages.VoiceItemUiModel
import off.kys.backtalk.presentation.viewmodel.SharedMediaViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedMediaSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onScrollToMessage: (MessageId) -> Unit,
    viewModel: SharedMediaViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SharedMediaSheetContent(
        uiState = uiState,
        onDismiss = onDismiss,
        onScrollToMessage = onScrollToMessage,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SharedMediaSheetContent(
    uiState: SharedMediaUiState,
    onDismiss: () -> Unit,
    onScrollToMessage: (MessageId) -> Unit,
    onEvent: (SharedMediaUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    val tabs = remember {
        listOf(
            SharedMediaTab.Media,
            SharedMediaTab.Voice,
            SharedMediaTab.Links
        )
    }
    val pagerState = rememberPagerState { tabs.size }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()
                Text(
                    text = stringResource(R.string.shared_media_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SecondaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                            text = {
                                Text(
                                    text = stringResource(tab.titleRes),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) { pageIndex ->
            when (tabs[pageIndex]) {
                SharedMediaTab.Media -> MediaTab(uiState.media, onScrollToMessage)
                SharedMediaTab.Voice -> VoicesTab(
                    uiState.voices,
                    onScrollToMessage,
                    onEvent
                )

                SharedMediaTab.Links -> LinksTab(uiState.links, onScrollToMessage)
            }
        }
    }
}

@Composable
private fun MediaTab(
    mediaItems: List<MediaItemUiModel>,
    onScrollToMessage: (MessageId) -> Unit
) {
    if (mediaItems.isEmpty()) {
        EmptyState(
            icon = R.drawable.round_image_24,
            message = stringResource(R.string.shared_media_empty_media)
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(110.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(1.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(
            items = mediaItems,
            key = { "${it.id.value}_${it.path.hashCode()}" }
        ) { item ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onScrollToMessage(item.id) }
            ) {
                AsyncImage(
                    model = item.path,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun VoicesTab(
    voiceItems: List<VoiceItemUiModel>,
    onScrollToMessage: (MessageId) -> Unit,
    onEvent: (SharedMediaUiEvent) -> Unit
) {
    if (voiceItems.isEmpty()) {
        EmptyState(
            icon = R.drawable.round_keyboard_voice_24,
            message = stringResource(R.string.shared_media_empty_voice)
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = voiceItems,
            key = { it.id.value }
        ) { item ->
            Surface(
                onClick = { onScrollToMessage(item.id) },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                VoiceMessageBubbleContent(
                    modifier = Modifier.padding(8.dp),
                    duration = item.duration,
                    waveformData = item.waveformData,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    isPlaying = item.isPlaying,
                    progress = item.progress,
                    onTogglePlay = { onEvent(SharedMediaUiEvent.ToggleVoicePlay(item)) }
                )
            }
        }
    }
}

@Composable
private fun LinksTab(
    linkItems: List<LinkItemUiModel>,
    onScrollToMessage: (MessageId) -> Unit
) {
    if (linkItems.isEmpty()) {
        EmptyState(
            icon = R.drawable.round_add_link_24,
            message = stringResource(R.string.shared_media_empty_links)
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = linkItems,
            key = { it.id.value }
        ) { item ->
            LinkPreviewCard(
                url = item.url,
                onClick = { onScrollToMessage(item.id) }
            )
        }
    }
}

@Composable
private fun EmptyState(
    icon: Int,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

private enum class SharedMediaTab(val titleRes: Int) {
    Media(R.string.shared_media_tab_media),
    Voice(R.string.shared_media_tab_voice),
    Links(R.string.shared_media_tab_links)
}