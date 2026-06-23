package off.kys.backtalk.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import off.kys.backtalk.presentation.model.MessageUiModel

/**
 * Extracts a distinct, sorted list of hashtags from a list of messages.
 * Recomputes only when the underlying messages list changes.
 */
@Composable
fun rememberHashtags(messages: List<MessageUiModel>): List<String> {
    return remember(messages) {
        val hashtagRegex = Regex("""#(\w+)""")
        messages.flatMap { message ->
            hashtagRegex.findAll(message.visibleText).map { it.groupValues[1] }.toList()
        }.distinct().sorted()
    }
}
