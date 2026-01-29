package off.kys.preferences.util

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun StatefulSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    // Define your active colors
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    // Define your disabled colors (M3 Defaults)
    disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    content: @Composable () -> Unit
) {
    // 1. Determine the current background and content color
    val backgroundColor = if (enabled) containerColor else disabledContainerColor
    val textColor = if (enabled) contentColor else disabledContentColor

    Surface(
        onClick = { onClick?.invoke() },
        enabled = enabled,
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        contentColor = textColor // This sets LocalContentColor for the children
    ) {
        content()
    }
}