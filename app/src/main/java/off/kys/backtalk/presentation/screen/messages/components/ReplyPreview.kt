package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R

/**
 * Composable function that displays a reply preview.
 *
 * @param text The text to display in the reply preview.
 * @param voicePath Optional path to the voice message audio file.
 * @param onPreviewClick The callback function to handle clicks on the reply preview.
 */
@Composable
fun ReplyPreview(
    text: String,
    voicePath: String? = null,
    onPreviewClick: () -> Unit
) {
    val cornerShape = RoundedCornerShape(4.dp)

    Row(
        modifier = Modifier
            .padding(start = 8.dp, end = 12.dp)
            .height(IntrinsicSize.Min)
            .clip(cornerShape)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            .clickable {
                onPreviewClick()
            },
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        Column(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            verticalArrangement = Arrangement.Center
        ) {
            if (voicePath != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_keyboard_voice_24),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            } else {
                SmartText(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    maxLines = 2
                )
            }
        }
    }
}
