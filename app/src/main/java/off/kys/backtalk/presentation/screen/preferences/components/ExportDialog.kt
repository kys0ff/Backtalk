package off.kys.backtalk.presentation.screen.preferences.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.util.emptyString

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var useEncryption by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf(emptyString()) }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.round_file_upload_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = stringResource(R.string.backup_export_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(R.string.backup_password_prompt),
                    style = MaterialTheme.typography.bodyMedium
                )

                // Encryption Toggle Row
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    onClick = { useEncryption = !useEncryption }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(if (useEncryption) R.drawable.round_lock_24 else R.drawable.round_lock_open_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (useEncryption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.settings_security_title),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = useEncryption,
                            onCheckedChange = { useEncryption = it },
                            thumbContent = if (useEncryption) {
                                {
                                    Icon(
                                        painter = painterResource(R.drawable.round_check_24),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            } else null
                        )
                    }
                }

                // Animated visibility for the password field
                AnimatedVisibility(visible = useEncryption) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.backup_enter_password)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(if (passwordVisible) R.drawable.round_visibility_24 else R.drawable.round_visibility_off_24),
                                    contentDescription = null
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
            }
        },
        confirmButton = {
            val isReady = !useEncryption || password.isNotBlank()
            Button(
                onClick = { onConfirm(if (useEncryption) password else null) },
                enabled = isReady
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