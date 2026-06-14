package off.kys.backtalk.presentation.screen.threads.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import off.kys.backtalk.common.registry.CaptionWordsRegistry
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.Thread
import off.kys.backtalk.presentation.screen.messages.components.SmartText
import off.kys.backtalk.util.emptyString
import org.koin.compose.koinInject

@Composable
fun ThreadDetailContent(
    modifier: Modifier,
    thread: Thread,
    listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    },
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onReplyClick: (MessageEntity) -> Unit,
    getReplyCount: (MessageEntity) -> Int
) {
    val allMessages = listOf(thread.root) + thread.replies

    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        itemsIndexed(
            items = allMessages,
            key = { _, message -> message.id() }
        ) { index, message ->
            val threadsSize = allMessages.size - 1
            if (index == 0) {
                MainThreadItem(
                    message = message,
                    repliesCount = threadsSize,
                    onCopy = onCopy,
                    onShare = onShare,
                    repliedTo = thread.repliedTo,
                    onReplyClick = onReplyClick,
                    getReplyCount = getReplyCount
                )
            } else {
                ThreadMessageItem(
                    message = message,
                    showConnectionLine = index < threadsSize,
                    onCopy = onCopy,
                    onShare = onShare,
                    replyCount = getReplyCount(message),
                    onReplyClick = { onReplyClick(message) }
                )
            }
        }
    }
}

@Composable
private fun MainThreadItem(
    message: MessageEntity,
    repliesCount: Int,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    repliedTo: MessageEntity? = null,
    onReplyClick: (MessageEntity) -> Unit,
    getReplyCount: (MessageEntity) -> Int
) {
    val dateFormatter = LocalDateFormatter.current
    val textToCopyOrShare = message.editedText ?: message.text
    val captionsRegistry = koinInject<CaptionWordsRegistry>()

    val isDefaultCaption = captionsRegistry.isRestricted(textToCopyOrShare)

    val images = remember(message) {
        val list = mutableListOf<String>()
        message.mediaPath?.let { list.add(it) }
        message.mediaPaths?.let { list.addAll(it) }
        list
    }

    val hasImages = images.isNotEmpty()
    val hasVoice = message.voicePath != null
    val shouldShowText = textToCopyOrShare.isNotEmpty() && !((hasImages || hasVoice) && isDefaultCaption)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_person_24),
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.threads_you),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.threads_at_you),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (shouldShowText) {
            SmartText(
                text = textToCopyOrShare,
                style = MaterialTheme.typography.headlineSmall,
                lineHeight = MaterialTheme.typography.headlineSmall.lineHeight,
            )
        }

        ThreadMediaContent(message = message)

        repliedTo?.let {
            QuotedMessage(
                message = it,
                modifier = Modifier.padding(top = 16.dp),
                replyCount = getReplyCount(it),
                onClick = { onReplyClick(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = dateFormatter.formatDateTime(message.timestamp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ActionIcon(
                R.drawable.round_chat_bubble_outline_24,
                stringResource(R.string.threads_replies_count, repliesCount)
            )
            ActionIcon(
                iconRes = R.drawable.round_content_copy_24,
                count = stringResource(R.string.common_copy)
            ) {
                onCopy(textToCopyOrShare)
            }
            ActionIcon(
                iconRes = R.drawable.round_share_24,
                count = stringResource(R.string.common_share)
            ) {
                onShare(textToCopyOrShare)
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun ThreadMessageItem(
    message: MessageEntity,
    showConnectionLine: Boolean,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    replyCount: Int,
    onReplyClick: () -> Unit
) {
    val dateFormatter = LocalDateFormatter.current
    val textToCopyOrShare = message.editedText ?: message.text
    val captionsRegistry = koinInject<CaptionWordsRegistry>()

    val isDefaultCaption = captionsRegistry.isRestricted(textToCopyOrShare)

    val images = remember(message) {
        val list = mutableListOf<String>()
        message.mediaPath?.let { list.add(it) }
        message.mediaPaths?.let { list.addAll(it) }
        list
    }

    val hasImages = images.isNotEmpty()
    val hasVoice = message.voicePath != null
    val shouldShowText = textToCopyOrShare.isNotEmpty() && !((hasImages || hasVoice) && isDefaultCaption)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_person_24),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            if (showConnectionLine) {
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .padding(vertical = 2.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.threads_you),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${stringResource(R.string.threads_at_you)} · ${
                        dateFormatter.formatMessageTime(message.timestamp)
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (shouldShowText) {
                SmartText(
                    text = textToCopyOrShare,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            ThreadMediaContent(message = message)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionIcon(
                    iconRes = R.drawable.round_chat_bubble_outline_24,
                    count = stringResource(R.string.threads_replies_count, replyCount),
                    onClick = if (replyCount > 0) onReplyClick else null
                )
                ActionIcon(
                    iconRes = R.drawable.round_content_copy_24,
                    count = emptyString()
                ) {
                    onCopy(textToCopyOrShare)
                }
                ActionIcon(
                    iconRes = R.drawable.round_share_24,
                    count = emptyString()
                ) {
                    onShare(textToCopyOrShare)
                }
            }
        }
    }
}
