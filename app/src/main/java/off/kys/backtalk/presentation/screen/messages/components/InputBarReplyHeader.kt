package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.presentation.model.MessageUiModel

@Composable
fun InputBarReplyHeader(
    replyingTo: MessageUiModel?,
    editingMessage: MessageUiModel?,
    onCancelReply: () -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = replyingTo != null || editingMessage != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                // Top padding creates breathing room below the top edge of the main Surface container
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent indicator track on the leading edge (replaces the box look)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp)
            ) {
                Text(
                    text = stringResource(
                        if (editingMessage != null) R.string.common_edit
                        else R.string.chat_replying_to
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                val messageToShow = editingMessage ?: replyingTo
                if (messageToShow != null) {
                    Text(
                        text = messageToShow.visibleText.ifEmpty {
                            if (messageToShow.hasVoice) stringResource(R.string.chat_voice_message)
                            else if (messageToShow.hasImages) stringResource(R.string.chat_media_image)
                            else stringResource(R.string.chat_media_general)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = {
                    if (editingMessage != null) onCancelEdit()
                    else onCancelReply()
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_close_24),
                    contentDescription = stringResource(R.string.common_cancel),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}