package off.kys.backtalk.presentation.screen.components.changelog

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import off.kys.backtalk.R

@Composable
fun FastScrollHandler(
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    val scrollGeometry by remember(state) {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val visibleItemsInfo = layoutInfo.visibleItemsInfo

            if (totalItems == 0 || visibleItemsInfo.isEmpty()) return@derivedStateOf null

            val firstVisibleIndex = state.firstVisibleItemIndex
            val visibleItemsCount = visibleItemsInfo.size

            if (totalItems <= visibleItemsCount) return@derivedStateOf null

            val thumbHeightRatio = (visibleItemsCount.toFloat() / totalItems).coerceIn(0.15f, 1f)
            val scrollPercent =
                firstVisibleIndex.toFloat() / (totalItems - visibleItemsCount).toFloat()

            Triple(totalItems, thumbHeightRatio, scrollPercent.coerceIn(0f, 1f))
        }
    }

    val (totalItems, thumbHeightRatio, scrollPercent) = scrollGeometry ?: return

    val coroutineScope = rememberCoroutineScope()
    var trackHeight by remember { mutableFloatStateOf(1f) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val thumbHeight = trackHeight * thumbHeightRatio
    val thumbOffset = (trackHeight - thumbHeight) * scrollPercent

    Box(
        modifier = modifier
            .width(6.dp)
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = RoundedCornerShape(3.dp)
            )
            .onGloballyPositioned { coordinates ->
                trackHeight = coordinates.size.height.toFloat()
            }
            .pointerInput(totalItems) {
                detectTapGestures { offset ->
                    val targetPercent = (offset.y / trackHeight).coerceIn(0f, 1f)
                    val targetItem =
                        (targetPercent * totalItems).toInt().coerceIn(0, totalItems - 1)
                    coroutineScope.launch { state.scrollToItem(targetItem) }
                }
            }
            .pointerInput(totalItems) {
                detectDragGestures(
                    onDragStart = { dragAccumulator = thumbOffset },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator =
                            (dragAccumulator + dragAmount.y).coerceIn(0f, trackHeight - thumbHeight)
                        val progress =
                            if (trackHeight > thumbHeight) dragAccumulator / (trackHeight - thumbHeight) else 0f
                        val currentVisibleItems = state.layoutInfo.visibleItemsInfo.size
                        val targetIndex = (progress * (totalItems - currentVisibleItems)).toInt()
                            .coerceIn(0, totalItems - 1)
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
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(3.dp)
                )
        )
    }
}

@Composable
fun getIconForType(type: String): Painter = when (type) {
    "feat" -> painterResource(R.drawable.round_add_24)
    "fix" -> painterResource(R.drawable.round_build_24)
    "refactor" -> painterResource(R.drawable.round_refresh_24)
    "chore" -> painterResource(R.drawable.round_settings_24)
    "docs" -> painterResource(R.drawable.round_edit_24)
    "style", "ui" -> painterResource(R.drawable.round_star_24)
    else -> painterResource(R.drawable.round_info_24)
}

@Composable
fun getLabelForType(type: String): String = when (type.lowercase()) {
    "feat" -> stringResource(R.string.changelog_type_feat)
    "fix" -> stringResource(R.string.changelog_type_fix)
    "refactor" -> stringResource(R.string.changelog_type_refactor)
    "chore" -> stringResource(R.string.changelog_type_chore)
    "docs" -> stringResource(R.string.changelog_type_docs)
    "style" -> stringResource(R.string.changelog_type_style)
    "ui" -> stringResource(R.string.changelog_type_ui)
    else -> stringResource(R.string.changelog_type_unknown)
}
