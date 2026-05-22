package off.kys.backtalk.presentation.screen.threads.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.model.Thread

@Preview(showBackground = true)
@Composable
fun ThreadItemWithQuotePreview() {
    val originalMessage = MessageEntity(
        id = MessageId(1),
        text = "This is the original message that is being quoted.",
        timestamp = System.currentTimeMillis() - 3600000,
        repliedToId = null
    )
    
    val replyMessage = MessageEntity(
        id = MessageId(2),
        text = "This is a reply that quotes the original message.",
        timestamp = System.currentTimeMillis(),
        repliedToId = MessageId(1)
    )
    
    val thread = Thread(
        root = replyMessage,
        replies = emptyList(),
        repliedTo = originalMessage
    )
    
    MaterialTheme {
        Surface {
            ThreadItem(
                thread = thread,
                onClick = {},
                onThreadCopy = {},
                onThreadShare = {},
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainThreadItemWithQuotePreview() {
    val originalMessage = MessageEntity(
        id = MessageId(1L),
        text = "This is the original message that is being quoted.",
        timestamp = System.currentTimeMillis() - 3600000,
        repliedToId = null
    )
    
    val replyMessage = MessageEntity(
        id = MessageId(2L),
        text = "This is a reply that quotes the original message in the detail view.",
        timestamp = System.currentTimeMillis(),
        repliedToId = MessageId(1L)
    )
    
    MaterialTheme {
        Surface {
            ThreadDetailContent(
                modifier = Modifier.padding(8.dp),
                thread = Thread(replyMessage, emptyList(), originalMessage),
                onCopy = {},
                onShare = {},
                onReplyClick = {},
                getReplyCount = { 0 }
            )
        }
    }
}
