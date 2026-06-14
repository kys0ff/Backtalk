package off.kys.backtalk.presentation.screen.threads.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import off.kys.backtalk.common.lock.LocalDateFormatter
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.common.registry.CaptionWordsRegistry
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.model.Thread
import off.kys.backtalk.util.AudioPlayer
import off.kys.backtalk.util.DateFormatter
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

@Composable
private fun PreviewWrapper(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val preferences = remember { BacktalkPreferences(context) }
    val dateFormatter = remember { DateFormatter(context, preferences) }
    val captionWordsRegistry = remember { CaptionWordsRegistry(context) }
    val audioPlayer = remember { AudioPlayer() }

    KoinApplication(
        configuration = koinConfiguration(
            declaration = {
                modules(module {
                    single { preferences }
                    single { dateFormatter }
                    single { captionWordsRegistry }
                    single { audioPlayer }
                })
            }
        )
    ) {
        MaterialTheme {
            CompositionLocalProvider(LocalDateFormatter provides dateFormatter) {
                Surface {
                    content()
                }
            }
        }
    }
}

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
    
    PreviewWrapper {
        ThreadItem(
            thread = thread,
            onClick = {},
            onThreadCopy = {},
            onThreadShare = {},
            modifier = Modifier.padding(8.dp)
        )
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
    
    PreviewWrapper {
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
