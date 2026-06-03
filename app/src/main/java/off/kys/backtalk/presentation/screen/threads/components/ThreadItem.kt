package off.kys.backtalk.presentation.screen.threads.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.common.lock.LocalDateFormatter
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.Thread
import off.kys.backtalk.presentation.screen.messages.components.SmartText

@Composable
fun ThreadItem(
    thread: Thread,
    onClick: () -> Unit,
    onThreadCopy: (String) -> Unit,
    onThreadShare: (String) -> Unit,
    modifier: Modifier = Modifier,
    onQuoteClick: ((MessageEntity) -> Unit)? = null,
    getReplyCount: ((MessageEntity) -> Int)? = null
) {
    val dateFormatter = LocalDateFormatter.current
    val text = thread.root.editedText ?: thread.root.text

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_person_24),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.threads_you),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(
                            R.string.threads_at_you_timestamp,
                            dateFormatter.formatThreadDate(thread.root.timestamp)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                SmartText(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )

                thread.repliedTo?.let { repliedTo ->
                    QuotedMessage(
                        message = repliedTo,
                        modifier = Modifier.padding(top = 12.dp),
                        replyCount = getReplyCount?.invoke(repliedTo) ?: 0,
                        onClick = onQuoteClick?.let { { it(repliedTo) } }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ActionIcon(
                        iconRes = R.drawable.round_chat_bubble_outline_24,
                        count = stringResource(R.string.threads_replies_count, thread.size - 1)
                    )
                    ActionIcon(
                        iconRes = R.drawable.round_content_copy_24,
                        count = stringResource(R.string.common_copy),
                        onClick = {
                            onThreadCopy(text)
                        }
                    )
                    ActionIcon(
                        iconRes = R.drawable.round_share_24,
                        count = stringResource(R.string.common_share),
                        onClick = {
                            onThreadShare(text)
                        }
                    )
                }
            }
        }
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    }
}
