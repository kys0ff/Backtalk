package off.kys.backtalk.presentation.screen.components.changelog

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.domain.model.ChangelogEntry
import off.kys.backtalk.presentation.theme.BacktalkTheme
import off.kys.backtalk.presentation.viewmodel.ChangelogViewModel
import off.kys.backtalk.util.capitalize
import org.koin.compose.viewmodel.koinViewModel

/**
 * A dialog that displays a changelog loaded from an asset file.
 *
 * @param onDismiss Callback executed when the dialog should be dismissed.
 */
@Composable
fun ChangelogDialog(
    onDismiss: () -> Unit,
) {
    val viewModel = koinViewModel<ChangelogViewModel>()
    val state by viewModel.state.collectAsState()

    ChangelogDialogContent(
        entries = state.entries,
        isLoading = state.isLoading,
        onDismiss = onDismiss
    )
}

/**
 * The internal content of the changelog dialog.
 *
 * @param entries The list of parsed changelog entries to display.
 * @param isLoading Whether the changelog is currently loading.
 * @param onDismiss Callback executed when the dialog should be dismissed.
 */
@Composable
private fun ChangelogDialogContent(
    entries: List<ChangelogEntry>,
    isLoading: Boolean,
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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (entries.isEmpty()) {
                Text(
                    text = stringResource(R.string.settings_changelog_error),
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
                        items(entries) { entry ->
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

/**
 * Represents a single row in the changelog list in the dialog.
 *
 * @param entry The changelog entry to display.
 */
@Composable
private fun ChangelogRow(entry: ChangelogEntry) {
    val (issue, messageText) = extractIssueFromMessage(entry.message)
    val (containerColor, contentColor) = if (entry.isParsedSuccessfully) {
        getColorsForType(entry.type, issue != null)
    } else {
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        if (entry.isParsedSuccessfully) {
            Icon(
                painter = getIconForType(entry.type),
                contentDescription = getLabelForType(entry.type),
                tint = if (issue != null || entry.type.lowercase() == "merge") contentColor else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            val (annotatedString, inlineContent) = formatChangelogMessage(
                message = messageText.capitalize(),
                type = entry.type
            )

            if (entry.isParsedSuccessfully) {
                val label = getLabelForType(entry.type)
                val tagText = if (issue != null) "$label $issue" else label
                
                ChangelogTag(
                    text = tagText,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = annotatedString,
                inlineContent = inlineContent,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
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
                ChangelogDialogContent(
                    entries = emptyList(),
                    isLoading = false,
                    onDismiss = {},
                )
            }
        }
    }
}
