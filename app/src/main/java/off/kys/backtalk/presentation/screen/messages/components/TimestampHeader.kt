package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composable function that displays a timestamp header.
 *
 * @param timestamp The timestamp to display.
 */
@Composable
fun TimestampHeader(timestamp: Long) {
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    Text(
        text = sdf.format(Date(timestamp)),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}