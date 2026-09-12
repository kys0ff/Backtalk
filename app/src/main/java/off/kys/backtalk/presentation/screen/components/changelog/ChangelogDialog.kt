package off.kys.backtalk.presentation.screen.components.changelog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.domain.model.ChangelogEntry
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
    onSeeOnboarding: (() -> Unit)?
) {
    val viewModel = koinViewModel<ChangelogViewModel>()
    val state by viewModel.state.collectAsState()

    ChangelogDialogContent(
        entries = state.entries,
        isLoading = state.isLoading,
        onDismiss = onDismiss,
        onSeeOnboarding = onSeeOnboarding
    )
}

/**
 * The internal content of the changelog dialog.
 *
 * @param entries The list of parsed changelog entries to display.
 * @param isLoading Whether the changelog is currently loading.
 * @param onDismiss Callback executed when the dialog should be dismissed.
 * @param onSeeOnboarding Callback executed when the "See Onboarding" button is clicked.
 */
@Composable
private fun ChangelogDialogContent(
    entries: List<ChangelogEntry>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSeeOnboarding: (() -> Unit)?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.round_update_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (entries.isEmpty()) {
                Text(
                    text = stringResource(R.string.settings_changelog_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                val listState = rememberLazyListState()

                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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

                    Spacer(modifier = Modifier.height(24.dp))

                    if (onSeeOnboarding != null) Surface(
                        onClick = onSeeOnboarding,
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.round_info_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stringResource(R.string.settings_changelog_see_onboarding),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    RecommendedBadge()
                                }
                                Text(
                                    text = "Revisit the app features and guides",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                painter = painterResource(R.drawable.round_arrow_back_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(180f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.common_ok),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

@Composable
private fun RecommendedBadge() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.common_recommended).uppercase(),
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
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
