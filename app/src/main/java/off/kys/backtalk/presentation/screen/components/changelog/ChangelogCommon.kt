package off.kys.backtalk.presentation.screen.components.changelog

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.util.emptyString

@Composable
fun ChangelogTag(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(containerColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun formatChangelogMessage(
    message: String,
    type: String = emptyString()
): Pair<AnnotatedString, Map<String, InlineTextContent>> {
    val issueRegex = Regex("""#\d+""")
    val branchRegex = Regex("""\b[\w.-]{2,}/[\w.-]{2,}\b""")
    val isMerge = type.lowercase() == "merge"

    val inlineContentMap = mutableMapOf<String, InlineTextContent>()

    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        val matches = (issueRegex.findAll(message) +
                (if (isMerge) branchRegex.findAll(message) else emptySequence()))
            .sortedBy { it.range.first }

        matches.forEach { match ->
            if (match.range.first >= lastIndex) {
                append(message.substring(lastIndex, match.range.first))
                val isIssue = match.value.startsWith("#")

                if (isIssue) {
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(match.value)
                    }
                } else {
                    val inlineId = "branch_${match.range.first}"

                    appendInlineContent(
                        id = inlineId,
                        alternateText = match.value
                    )

                    inlineContentMap[inlineId] = InlineTextContent(
                        Placeholder(
                            width = (match.value.length * 7).sp,
                            height = 18.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                    shape = MaterialTheme.shapes.extraSmall
                                )
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = match.value,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                lastIndex = match.range.last + 1
            }
        }
        append(message.substring(lastIndex))
    }

    return Pair(annotatedString, inlineContentMap)
}

@Composable
fun getColorsForType(type: String, hasIssue: Boolean = false): Pair<Color, Color> {
    if (hasIssue) {
        return MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    return when (type.lowercase()) {
        "feat" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "fix" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "merge" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "refactor" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "docs" -> Color(0xFFD1E4FF) to Color(0xFF001D36)
        "chore" -> Color(0xFFE2E2E6) to Color(0xFF45464F)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
}

fun extractIssueFromMessage(message: String): Pair<String?, String> {
    val issueRegex = Regex("""^(#\d+)\s+(.*)$""", RegexOption.DOT_MATCHES_ALL)
    val match = issueRegex.find(message)
    return if (match != null) {
        match.groupValues[1] to match.groupValues[2]
    } else {
        null to message
    }
}

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
fun getIconForType(type: String): Painter = when (type.lowercase()) {
    "feat" -> painterResource(R.drawable.round_add_box_24)
    "fix" -> painterResource(R.drawable.round_construction_24)
    "refactor" -> painterResource(R.drawable.round_refresh_24)
    "chore" -> painterResource(R.drawable.round_cleaning_services_24)
    "docs" -> painterResource(R.drawable.round_docs_24)
    "style", "ui" -> painterResource(R.drawable.round_star_24)
    "build" -> painterResource(R.drawable.round_build_24)
    "merge" -> painterResource(R.drawable.round_merge_24)
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
    "build" -> stringResource(R.string.changelog_type_build)
    "merge" -> stringResource(R.string.changelog_type_merge)
    else -> stringResource(R.string.changelog_type_unknown)
}
