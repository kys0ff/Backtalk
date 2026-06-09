package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import off.kys.backtalk.R
import off.kys.backtalk.common.lock.LocalDateFormatter
import java.util.Calendar

enum class SchedulingStage {
    Hidden,
    SelectingDate,
    SelectingTime
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageSchedulingDialogs(
    stage: SchedulingStage,
    datePickerState: DatePickerState,
    timePickerState: TimePickerState,
    onStageChange: (SchedulingStage) -> Unit,
    onSchedule: (Long) -> Unit
) {
    val formatter = LocalDateFormatter.current

    val isDateValid by remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }

    val targetTimestampAndValidity by remember(datePickerState.selectedDateMillis, timePickerState.hour, timePickerState.minute) {
        derivedStateOf {
            val baseMillis = datePickerState.selectedDateMillis
            if (baseMillis == null) {
                Pair(0L, false)
            } else {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = baseMillis
                    set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    set(Calendar.MINUTE, timePickerState.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val finalTime = calendar.timeInMillis
                Pair(finalTime, finalTime > System.currentTimeMillis())
            }
        }
    }

    val (scheduledTimestamp, isTimeValid) = targetTimestampAndValidity

    when (stage) {
        SchedulingStage.Hidden -> return

        SchedulingStage.SelectingDate -> {
            DatePickerDialog(
                onDismissRequest = { onStageChange(SchedulingStage.Hidden) },
                confirmButton = {
                    TextButton(
                        onClick = { onStageChange(SchedulingStage.SelectingTime) },
                        enabled = isDateValid
                    ) {
                        Text(stringResource(R.string.common_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onStageChange(SchedulingStage.Hidden) }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                DatePicker(state = datePickerState)
            }
        }

        SchedulingStage.SelectingTime -> {
            Dialog(
                onDismissRequest = { onStageChange(SchedulingStage.Hidden) },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = true
                )
            ) {
                Surface(
                    modifier = Modifier
                        .padding(EdgeInsetsDialogDimensions)
                        .widthIn(max = TimePickerDialogMaxWidth),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Title Layout mirroring Material 3 design spec
                        Text(
                            text = stringResource(R.string.message_scheduling_select_time),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        if (isDateValid) {
                            Text(
                                text = formatter.formatDateTime(scheduledTimestamp),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp)
                            )
                        }

                        TimePicker(state = timePickerState)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (!isTimeValid) {
                                Text(
                                    text = stringResource(R.string.message_scheduling_invalid_time),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { onStageChange(SchedulingStage.SelectingDate) }) {
                                Text(stringResource(R.string.common_back))
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            TextButton(onClick = { onStageChange(SchedulingStage.Hidden) }) {
                                Text(stringResource(R.string.common_cancel))
                            }

                            TextButton(
                                onClick = {
                                    if (isTimeValid) {
                                        onSchedule(scheduledTimestamp)
                                        onStageChange(SchedulingStage.Hidden)
                                    }
                                },
                                enabled = isTimeValid
                            ) {
                                Text(stringResource(R.string.common_ok))
                            }
                        }
                    }
                }
            }
        }
    }
}

private val EdgeInsetsDialogDimensions = 24.dp
private val TimePickerDialogMaxWidth = 328.dp