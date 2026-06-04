package off.kys.backtalk.presentation.screen.preferences.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.presentation.theme.BacktalkTheme

private data class ChangelogEntry(
    val hash: String,
    val type: String,
    val message: String,
    val isParsedSuccessfully: Boolean = true
)

@Composable
fun ChangelogDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    val entries = remember {
        try {
            context.assets.open("changelog.txt").bufferedReader().use { reader ->
                reader.lineSequence()
                    .filter { it.isNotBlank() }
                    .map { parseChangelogLine(it) }
                    .toList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    ChangelogDialogContent(
        changelogEntries = entries,
        onDismiss = onDismiss
    )
}

@Composable
private fun ChangelogDialogContent(
    changelogEntries: List<ChangelogEntry>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.round_update_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.settings_changelog_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            if (changelogEntries.isEmpty()) {
                Text(
                    text = "Error loading changelog or no changes found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                val listState = rememberLazyListState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(changelogEntries) { entry ->
                            ChangelogRow(entry = entry)
                        }
                    }

                    FastScrollHandler(
                        state = listState,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    )
}

@Composable
private fun FastScrollHandler(
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    val scrollGeometry by remember(state) {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val visibleItemsInfo = layoutInfo.visibleItemsInfo

            if (totalItems == 0 || visibleItemsInfo.isEmpty()) {
                return@derivedStateOf null
            }

            val firstVisibleIndex = state.firstVisibleItemIndex
            val visibleItemsCount = visibleItemsInfo.size

            if (totalItems <= visibleItemsCount) {
                return@derivedStateOf null
            }

            val thumbHeightRatio = (visibleItemsCount.toFloat() / totalItems).coerceIn(0.15f, 1f)
            val scrollPercent = firstVisibleIndex.toFloat() / (totalItems - visibleItemsCount).toFloat()

            Triple(totalItems, thumbHeightRatio, scrollPercent.coerceIn(0f, 1f))
        }
    }

    // Safely exit if scrolling isn't necessary or possible yet
    val (totalItems, thumbHeightRatio, scrollPercent) = scrollGeometry ?: return

    val coroutineScope = rememberCoroutineScope()
    var trackHeight by remember { mutableFloatStateOf(1f) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val thumbHeight = trackHeight * thumbHeightRatio
    val thumbOffset = (trackHeight - thumbHeight) * scrollPercent

    Box(
        modifier = modifier
            .width(8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp)
            )
            .onGloballyPositioned { coordinates ->
                trackHeight = coordinates.size.height.toFloat()
            }
            .pointerInput(totalItems) {
                detectTapGestures { offset ->
                    val targetPercent = (offset.y / trackHeight).coerceIn(0f, 1f)
                    val targetItem = (targetPercent * totalItems).toInt().coerceIn(0, totalItems - 1)
                    coroutineScope.launch { state.scrollToItem(targetItem) }
                }
            }
            .pointerInput(totalItems) {
                detectDragGestures(
                    onDragStart = { dragAccumulator = thumbOffset },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator = (dragAccumulator + dragAmount.y).coerceIn(0f, trackHeight - thumbHeight)
                        val progress = if (trackHeight > thumbHeight) dragAccumulator / (trackHeight - thumbHeight) else 0f
                        val currentVisibleItems = state.layoutInfo.visibleItemsInfo.size
                        val targetIndex = (progress * (totalItems - currentVisibleItems)).toInt().coerceIn(0, totalItems - 1)
                        coroutineScope.launch {
                            state.scrollToItem(targetIndex)
                        }
                    }
                )
            }
    ) {
        Spacer(
            modifier = Modifier
                .offset { IntOffset(0, thumbOffset.toInt()) }
                .fillMaxWidth()
                .height(with(LocalDensity.current) { thumbHeight.toDp() })
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                )
        )
    }
}

@Composable
private fun ChangelogRow(entry: ChangelogEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        if (entry.isParsedSuccessfully) {
            Icon(
                painter = getIconForType(entry.type),
                contentDescription = entry.type,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    if (entry.isParsedSuccessfully) {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            append(entry.type)
                        }
                        append(": ")
                        append(entry.message)
                    } else {
                        append(entry.message)
                    }
                },
                style = MaterialTheme.typography.bodyMedium
            )
            if (entry.hash.isNotEmpty()) {
                Text(
                    text = entry.hash,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun parseChangelogLine(line: String): ChangelogEntry {
    val firstSpace = line.indexOf(' ')
    if (firstSpace == -1) return ChangelogEntry("", "", line, isParsedSuccessfully = false)

    val hash = line.substring(0, firstSpace)
    val remainder = line.substring(firstSpace + 1)
    val colonIndex = remainder.indexOf(": ")

    return if (colonIndex != -1) {
        val type = remainder.substring(0, colonIndex).lowercase().trim()
        val message = remainder.substring(colonIndex + 2)
        ChangelogEntry(hash = hash, type = type, message = message)
    } else {
        ChangelogEntry(hash = hash, type = "", message = remainder, isParsedSuccessfully = false)
    }
}

@Composable
private fun getIconForType(type: String): Painter = when (type) {
    "feat" -> painterResource(R.drawable.round_add_24)
    "fix" -> painterResource(R.drawable.round_build_24)
    "refactor" -> painterResource(R.drawable.round_refresh_24)
    "chore" -> painterResource(R.drawable.round_settings_24)
    "docs" -> painterResource(R.drawable.round_edit_24)
    "style", "ui" -> painterResource(R.drawable.round_star_24)
    else -> painterResource(R.drawable.round_info_24)
}

@Preview(showSystemUi = true)
@Composable
fun ChangelogDialogPreview() {
    BacktalkTheme(dynamicColor = false) {
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ChangelogDialog(
                    onDismiss = {},
                )
            }
        }
    }
}
