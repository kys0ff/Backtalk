package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import off.kys.backtalk.common.lock.LocalDateFormatter

/**
 * Composable function that displays a timestamp header.
 *
 * @param timestamp The timestamp to display.
 */
@Composable
fun TimestampHeader(timestamp: Long) {
    val dateFormatter = LocalDateFormatter.current
    Text(
        text = dateFormatter.formatDateTime(timestamp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}
