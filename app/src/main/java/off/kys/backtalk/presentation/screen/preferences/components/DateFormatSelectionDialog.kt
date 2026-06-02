package off.kys.backtalk.presentation.screen.preferences.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.common.AppDateFormat
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun DateFormatSelectionDialog(
    selectedFormat: AppDateFormat,
    customPattern: String,
    onFormatSelected: (AppDateFormat) -> Unit,
    onCustomPatternChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val showCustomEdit = remember { mutableStateOf(false) }

    if (showCustomEdit.value) {
        CustomDateFormatDialog(
            initialPattern = customPattern,
            onConfirm = { pattern ->
                onCustomPatternChanged(pattern)
                onFormatSelected(AppDateFormat.CUSTOM)
                showCustomEdit.value = false
                onDismiss()
            },
            onDismiss = { showCustomEdit.value = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.round_calendar_today_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.settings_date_format),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            val platformLocale = LocalLocale.current.platformLocale
            val now = remember { Date() }

            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(AppDateFormat.entries) { format ->
                    val isSelected = selectedFormat == format

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                if (format == AppDateFormat.CUSTOM) {
                                    showCustomEdit.value = true
                                } else {
                                    onFormatSelected(format)
                                    onDismiss()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(format.titleResId),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val previewPattern = when (format) {
                                AppDateFormat.SYSTEM -> null
                                AppDateFormat.CUSTOM -> customPattern
                                else -> format.pattern
                            }
                            if (previewPattern != null) {
                                val preview = try {
                                    SimpleDateFormat(previewPattern, platformLocale).format(now)
                                } catch (_: Exception) {
                                    stringResource(R.string.date_format_invalid)
                                }
                                Text(
                                    text = preview,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun CustomDateFormatDialog(
    initialPattern: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pattern by remember { mutableStateOf(initialPattern) }
    val platformLocale = LocalLocale.current.platformLocale
    val now = remember { Date() }

    val preview = remember(pattern) {
        try {
            if (pattern.isBlank()) null else SimpleDateFormat(pattern, platformLocale).format(now)
        } catch (_: Exception) {
            null
        }
    }
    val isError = preview == null && pattern.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.round_calendar_clock_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.date_format_custom_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text(stringResource(R.string.date_format_custom_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(text = stringResource(R.string.date_format_invalid))
                        } else if (preview != null) {
                            Text(text = preview)
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pattern) },
                enabled = preview != null && pattern.isNotBlank()
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