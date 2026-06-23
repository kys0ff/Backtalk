package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.presentation.model.MessageUiModel

@Composable
fun MessageContextMenu(
    message: MessageUiModel,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    val canEdit = message.canEdit
    val canDelete = !message.isLocked
    val animationState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        animationState.targetState = true
    }

    val transition = rememberTransition(animationState, label = "MenuAnimation")
    val scale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 250, easing = LinearOutSlowInEasing) },
        label = "Scale"
    ) { expanded -> if (expanded) 1f else 0.8f }
    val menuAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 180, easing = LinearOutSlowInEasing) },
        label = "Alpha"
    ) { expanded -> if (expanded) 1f else 0f }

    DropdownMenu(
        expanded = animationState.targetState,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = menuAlpha
            }
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 4.dp)
    ) {
        ContextMenuItem(
            text = stringResource(R.string.common_reply),
            icon = painterResource(R.drawable.round_reply_24),
            onClick = {
                onReply()
                onDismiss()
            }
        )

        if (canEdit) {
            ContextMenuItem(
                text = stringResource(R.string.common_edit),
                icon = painterResource(R.drawable.round_edit_24),
                onClick = {
                    onEdit()
                    onDismiss()
                }
            )
        }

        ContextMenuItem(
            text = stringResource(R.string.common_copy),
            icon = painterResource(R.drawable.round_content_copy_24),
            onClick = {
                onCopy()
                onDismiss()
            }
        )

        if (canDelete) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            ContextMenuItem(
                text = stringResource(R.string.common_delete),
                icon = painterResource(R.drawable.round_delete_24),
                color = MaterialTheme.colorScheme.error,
                onClick = {
                    onDelete()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun ContextMenuItem(
    text: String,
    icon: Painter,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = color
            )
        },
        onClick = onClick,
        leadingIcon = {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = color
            )
        }
    )
}
