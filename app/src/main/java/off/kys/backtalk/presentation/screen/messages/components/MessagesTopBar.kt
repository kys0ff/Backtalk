package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import off.kys.backtalk.R
import off.kys.backtalk.domain.model.MessageId

/**
 * Composable function that displays the messages top bar.
 *
 * @param selectedMessageId The ID of the currently selected message.
 * @param onCloseSelection The callback function to handle closing the selection.
 * @param onDelete The callback function to handle deleting the selected message.
 * @param onCopy The callback function to handle copying the selected message.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    selectedMessageId: MessageId?,
    onCloseSelection: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = if (selectedMessageId != null)
                    stringResource(R.string.message_selected)
                else
                    stringResource(R.string.messages)
            )
        },
        navigationIcon = {
            if (selectedMessageId != null) {
                IconButton(onClick = onCloseSelection) {
                    Icon(
                        painterResource(R.drawable.round_close_24),
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            if (selectedMessageId != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        painter = painterResource(R.drawable.round_delete_24),
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(onClick = onCopy) {
                    Icon(
                        painter = painterResource(R.drawable.round_content_copy_24),
                        contentDescription = stringResource(R.string.copy),
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (selectedMessageId != null) {
                TopAppBarDefaults.topAppBarColors().scrolledContainerColor
            } else {
                TopAppBarDefaults.topAppBarColors().containerColor
            }
        )
    )
}
