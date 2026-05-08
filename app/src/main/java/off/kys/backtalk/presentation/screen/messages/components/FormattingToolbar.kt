package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R

@Composable
fun FormattingToolbar(
    onFormattingClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FormattingButton(label = stringResource(R.string.chat_input_format_bold)) {
            onFormattingClick("**", "**")
        }
        FormattingButton(label = stringResource(R.string.chat_input_format_italic)) {
            onFormattingClick("*", "*")
        }
        FormattingButton(label = stringResource(R.string.chat_input_format_underline)) {
            onFormattingClick("__", "__")
        }
        FormattingButton(label = stringResource(R.string.chat_input_format_strikethrough)) {
            onFormattingClick("~~", "~~")
        }
        FormattingButton(label = stringResource(R.string.chat_input_format_monospace)) {
            onFormattingClick("`", "`")
        }
    }
}

@Composable
private fun FormattingButton(
    label: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
