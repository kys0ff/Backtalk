package off.kys.backtalk.presentation.screen.preferences.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.common.AppDateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DateFormatSelectionDialog(
    selectedFormat: AppDateFormat,
    customPattern: String,
    onFormatSelected: (AppDateFormat) -> Unit,
    onCustomPatternChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCustomEdit by remember { mutableStateOf(false) }

    if (showCustomEdit) {
        CustomDateFormatDialog(
            initialPattern = customPattern,
            onConfirm = {
                onCustomPatternChanged(it)
                onFormatSelected(AppDateFormat.CUSTOM)
                showCustomEdit = false
                onDismiss()
            },
            onDismiss = { showCustomEdit = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_date_format)) },
        text = {
            LazyColumn {
                items(AppDateFormat.entries) { format ->
                    val isSelected = selectedFormat == format
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (format == AppDateFormat.CUSTOM) {
                                    showCustomEdit = true
                                } else {
                                    onFormatSelected(format)
                                    onDismiss()
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null // Row handles click
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(format.titleResId),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            val previewPattern = when (format) {
                                AppDateFormat.SYSTEM -> null
                                AppDateFormat.CUSTOM -> customPattern
                                else -> format.pattern
                            }
                            if (previewPattern != null) {
                                val preview = try {
                                    SimpleDateFormat(previewPattern, Locale.getDefault()).format(Date())
                                } catch (e: Exception) {
                                    stringResource(R.string.date_format_invalid)
                                }
                                Text(
                                    text = preview,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
fun CustomDateFormatDialog(
    initialPattern: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pattern by remember { mutableStateOf(initialPattern) }
    val preview = remember(pattern) {
        try {
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
        } catch (e: Exception) {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.date_format_custom_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text(stringResource(R.string.date_format_custom_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = preview == null
                )
                if (preview != null) {
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = stringResource(R.string.date_format_invalid),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pattern) },
                enabled = preview != null && pattern.isNotEmpty()
            ) {
                Text(stringResource(R.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
