package off.kys.backtalk.presentation.activity.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.github_app_updater_lib.model.updater.UpdateResult

/**
 * Composable for displaying an app update dialog.
 *
 * @param updateResult The result of the update check.
 * @param onDismissRequest The callback to be invoked when the dialog is dismissed.
 * @param onUpdateClick The callback to be invoked when the update button is clicked.
 */
@Composable
fun AppUpdateDialog(
    updateResult: UpdateResult,
    onDismissRequest: () -> Unit,
    onUpdateClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                painter = painterResource(R.drawable.round_system_update_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.update_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            val listState = rememberLazyListState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 12.dp)
                        .scrollbar(
                            state = listState,
                            horizontal = false,
                            scrollbarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                ) {
                    item {
                        Text(
                            text = stringResource(
                                R.string.update_dialog_message,
                                updateResult.latestVersion,
                                updateResult.changeLog
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpdateClick
            ) {
                Text(stringResource(R.string.update_dialog_action_now))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = stringResource(R.string.common_later),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    )
}

/**
 * A clean Material 3 modifier extension to draw a custom scrollbar for LazyLists.
 */
fun Modifier.scrollbar(
    state: LazyListState,
    horizontal: Boolean = false,
    scrollbarColor: Color,
    trackColor: Color,
    thumbWidth: Dp = 4.dp
): Modifier = drawWithContent {
    drawContent()

    val layoutInfo = state.layoutInfo
    val totalItemsCount = layoutInfo.totalItemsCount

    if (totalItemsCount > 0) {
        val visibleItemsInfo = layoutInfo.visibleItemsInfo
        if (visibleItemsInfo.isNotEmpty()) {
            val totalSize = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset

            val visibleItemsSize = visibleItemsInfo.sumOf { it.size }
            val estimatedTotalSize =
                (visibleItemsSize.toFloat() / visibleItemsInfo.size) * totalItemsCount

            if (estimatedTotalSize > totalSize) {
                val scrollOffset = state.firstVisibleItemScrollOffset
                val firstVisibleIndex = state.firstVisibleItemIndex

                val scrollProgress = if (estimatedTotalSize > 0) {
                    (firstVisibleIndex * (visibleItemsSize / visibleItemsInfo.size) + scrollOffset) / estimatedTotalSize
                } else 0f

                val thumbSizeFraction = totalSize / estimatedTotalSize
                val thumbSize = (totalSize * thumbSizeFraction).coerceAtLeast(32f)

                val startOffset = scrollProgress * (totalSize - thumbSize)

                drawRect(
                    color = trackColor,
                    topLeft = if (horizontal) Offset(
                        0f,
                        size.height - thumbWidth.toPx()
                    ) else Offset(size.width - thumbWidth.toPx(), 0f),
                    size = if (horizontal) Size(
                        size.width,
                        thumbWidth.toPx()
                    ) else Size(thumbWidth.toPx(), size.height),
                    alpha = 0.3f
                )

                drawRoundRect(
                    color = scrollbarColor,
                    topLeft = if (horizontal) Offset(
                        startOffset,
                        size.height - thumbWidth.toPx()
                    ) else Offset(size.width - thumbWidth.toPx(), startOffset),
                    size = if (horizontal) Size(
                        thumbSize,
                        thumbWidth.toPx()
                    ) else Size(thumbWidth.toPx(), thumbSize),
                    cornerRadius = CornerRadius(thumbWidth.toPx() / 2)
                )
            }
        }
    }
}