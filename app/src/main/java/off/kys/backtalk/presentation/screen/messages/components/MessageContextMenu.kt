package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.common.Constants
import off.kys.backtalk.data.local.entity.MessageEntity

@Composable
fun MessageContextMenu(
    message: MessageEntity,
    isSelected: Boolean,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    onCopy: () -> Unit
) {
    val isLocked =
        (System.currentTimeMillis() - message.timestamp) >= Constants.MESSAGE_EDIT_DELETE_WINDOW
    val canEdit = message.editedAt == null && !isLocked && message.voicePath == null
    val canDelete = !isLocked
    val animationState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        animationState.targetState = true
    }

    val transition = rememberTransition(animationState, label = "MenuAnimation")
    val scale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 250, easing = LinearOutSlowInEasing) },
        label = "Scale"
    ) { expanded -> if (expanded) 1f else 0.8f }
    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 180, easing = LinearOutSlowInEasing) },
        label = "Alpha"
    ) { expanded -> if (expanded) 1f else 0f }

    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = MenuDefaults.TonalElevation,
        modifier = Modifier
            .widthIn(min = 200.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.common_reply)) },
            onClick = {
                onReply()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.round_reply_24),
                    contentDescription = null
                )
            }
        )
        if (canEdit) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.common_edit)) },
                onClick = {
                    onEdit()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.round_edit_24),
                        contentDescription = null
                    )
                }
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.common_copy)) },
            onClick = {
                onCopy()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.round_content_copy_24),
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    stringResource(
                        if (isSelected) R.string.common_clear else R.string.common_selected
                    )
                )
            },
            onClick = {
                onSelect()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.round_list_24),
                    contentDescription = null
                )
            }
        )
        if (canDelete) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.common_delete)) },
                onClick = {
                    onDelete()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.round_delete_24),
                        contentDescription = null
                    )
                },
                colors = MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.error,
                    leadingIconColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}