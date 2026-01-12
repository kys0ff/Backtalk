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

/**
 * Composable function that displays the messages top bar.
 *
 * @param scrollBehavior The scroll behavior of the top bar.
 * @param selectedCount The number of selected messages.
 * @param onCloseSelection The callback function to handle closing the selection.
 * @param onDelete The callback function to handle deleting the selected message.
 * @param onCopy The callback function to handle copying the selected message.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    selectedCount: Int,
    onCloseSelection: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    val selectionActive = selectedCount > 0

    TopAppBar(
        title = {
            Text(
                text = if (selectionActive) {
                    stringResource(R.string.messages_selected_count, selectedCount)
                } else {
                    stringResource(R.string.messages)
                }
            )
        },
        navigationIcon = {
            if (selectionActive) {
                IconButton(onClick = onCloseSelection) {
                    Icon(
                        painter = painterResource(R.drawable.round_close_24),
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }
        },
        actions = {
            if (selectionActive) {
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
                        contentDescription = stringResource(R.string.copy)
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (selectionActive) {
                TopAppBarDefaults.topAppBarColors().scrolledContainerColor
            } else {
                TopAppBarDefaults.topAppBarColors().containerColor
            }
        )
    )
}
