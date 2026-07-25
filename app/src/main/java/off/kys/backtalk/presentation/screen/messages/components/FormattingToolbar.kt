package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R

@Composable
fun FormattingToolbar(
    onFormattingClick: (String, String) -> Unit,
    onEscapeClick: () -> Unit,
    onCopyClick: () -> Unit,
    onPasteClick: () -> Unit,
    onCutClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text styling group
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

            ToolbarDivider()

            FormattingButton(label = stringResource(R.string.chat_input_format_escape)) {
                onEscapeClick()
            }

            ToolbarDivider()

            // Clipboard group
            FormattingIconButton(
                icon = painterResource(R.drawable.round_content_copy_24),
                contentDescription = stringResource(R.string.common_copy),
                onClick = onCopyClick
            )
            FormattingIconButton(
                icon = painterResource(R.drawable.round_content_paste_24),
                contentDescription = stringResource(R.string.common_paste),
                onClick = onPasteClick
            )
            FormattingIconButton(
                icon = painterResource(R.drawable.round_content_cut_24),
                contentDescription = stringResource(R.string.common_cut),
                onClick = onCutClick
            )
            FormattingIconButton(
                icon = painterResource(R.drawable.round_select_all_24),
                contentDescription = stringResource(R.string.common_select_all),
                onClick = onSelectAllClick
            )

            ToolbarDivider()

            FormattingIconButton(
                icon = painterResource(R.drawable.round_delete_sweep_24),
                contentDescription = stringResource(R.string.common_clear),
                onClick = onClearClick,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ToolbarDivider() {
    VerticalDivider(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .height(20.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
}

/** Shared press-scale + ripple-less tap surface used by both button variants. */
@Composable
private fun PressableSlot(
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    contentDescription: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1f,
        animationSpec = tween(120),
        label = "FormattingButtonScale"
    )

    Surface(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.fillMaxHeight().width(size),
            horizontalArrangement = Alignment.CenterHorizontally.let {
                androidx.compose.foundation.layout.Arrangement.Center
            },
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun FormattingButton(
    label: String,
    onClick: () -> Unit
) {
    PressableSlot(onClick = onClick, contentDescription = label) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Overload retained for call sites still passing a Painter (e.g. custom drawables). */
@Composable
private fun FormattingIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    PressableSlot(onClick = onClick, contentDescription = contentDescription) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}