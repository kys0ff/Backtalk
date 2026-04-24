package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.TextUnit

/**
 * A custom [Text] composable that provides a convenient way to override specific text styles
 * while maintaining defaults from [LocalTextStyle].
 *
 * @param text The text to be displayed.
 * @param modifier The [Modifier] to be applied to the layout.
 * @param fontSize The size of glyphs to use when painting the text.
 * @param color The color to be applied to the text.
 * @param style The style configuration for the text such as color, font, line height etc.
 * @param textDecoration The decorations to paint on the text (e.g., an underline).
 */
@Composable
fun SmartText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    textDecoration: TextDecoration = TextDecoration.None
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(
            color = if (color != Color.Unspecified) color else style.color,
            fontSize = if (fontSize != TextUnit.Unspecified) fontSize else style.fontSize,
            textDecoration = textDecoration,
            textDirection = TextDirection.Content,
            textAlign = TextAlign.Start
        )
    )
}