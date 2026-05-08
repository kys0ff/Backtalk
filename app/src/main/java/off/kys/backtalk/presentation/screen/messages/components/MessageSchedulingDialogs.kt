package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import off.kys.backtalk.R
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageSchedulingDialogs(
    showDatePicker: MutableState<Boolean>,
    showTimePicker: MutableState<Boolean>,
    datePickerState: DatePickerState,
    timePickerState: TimePickerState,
    onSchedule: (Long) -> Unit
) {
    // Material 3 Date Picker Dialog
    if (showDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker.value = false
                    showTimePicker.value = true
                }) {
                    Text(stringResource(R.string.common_send))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker.value = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Material 3 Time Picker Dialog
    if (showTimePicker.value) {
        Dialog(
            onDismissRequest = { showTimePicker.value = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.message_scheduling_select_time),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker.value = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                        TextButton(onClick = {
                            showTimePicker.value = false
                            val calendar = Calendar.getInstance()
                            datePickerState.selectedDateMillis?.let {
                                calendar.timeInMillis = it
                            }
                            calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            calendar.set(Calendar.MINUTE, timePickerState.minute)
                            calendar.set(Calendar.SECOND, 0)

                            if (calendar.timeInMillis > System.currentTimeMillis()) {
                                onSchedule(calendar.timeInMillis)
                            }
                        }) {
                            Text(stringResource(R.string.common_ok))
                        }
                    }
                }
            }
        }
    }
}
