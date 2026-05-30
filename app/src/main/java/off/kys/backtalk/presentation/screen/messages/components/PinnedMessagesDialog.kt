package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.data.local.entity.MessageEntity

/**
 * A dialog that displays a list of all pinned messages using Material 3 specifications.
 */
@Composable
fun PinnedMessagesDialog(
    pinnedMessages: List<MessageEntity>,
    onMessageClick: (MessageEntity) -> Unit,
    onUnpinClick: (MessageEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.round_push_pin_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Pinned Messages",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            if (pinnedMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No pinned messages",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pinnedMessages, key = { it.id() }) { message ->
                        PinnedMessageItem(
                            message = message,
                            onClick = { onMessageClick(message) },
                            onUnpinClick = { onUnpinClick(message) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_close))
            }
        }
    )
}

@Composable
private fun PinnedMessageItem(
    message: MessageEntity,
    onClick: () -> Unit,
    onUnpinClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
            .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 40.dp),
            verticalArrangement = Arrangement.Center
        ) {
            SmartText(
                text = message.editedText ?: message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onUnpinClick,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.round_keep_off_24),
                contentDescription = "Unpin message",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}