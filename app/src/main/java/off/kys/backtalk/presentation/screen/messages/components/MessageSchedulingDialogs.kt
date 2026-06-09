package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
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
import java.util.Calendar

/**
 * Controlled state definition for the scheduling flow to eliminate raw MutableState parameters.
 */
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
    val isDateValid by remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }

    val isTimeValid by remember(datePickerState.selectedDateMillis, timePickerState.hour, timePickerState.minute) {
        derivedStateOf {
            val calendar = Calendar.getInstance()
            datePickerState.selectedDateMillis?.let {
                calendar.timeInMillis = it
            }
            calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
            calendar.set(Calendar.MINUTE, timePickerState.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis > System.currentTimeMillis()
        }
    }

    when (stage) {
        SchedulingStage.Hidden -> return

        SchedulingStage.SelectingDate -> {
            DatePickerDialog(
                onDismissRequest = { onStageChange(SchedulingStage.Hidden) },
                confirmButton = {
                    Button(
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
                        .padding(24.dp)
                        .widthIn(max = 328.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.message_scheduling_select_time),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp)
                        )

                        AnimatedContent(
                            targetState = timePickerState,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(400, easing = EaseInOutCubic)) togetherWith
                                        fadeOut(animationSpec = tween(300))
                            },
                            label = "TimePickerExpressiveTransition"
                        ) { targetState ->
                            TimePicker(state = targetState)
                        }

                        if (!isTimeValid) {
                            Text(
                                text = stringResource(R.string.message_scheduling_invalid_time),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

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

                            Button(
                                onClick = {
                                    val calendar = Calendar.getInstance()
                                    datePickerState.selectedDateMillis?.let {
                                        calendar.timeInMillis = it
                                    }
                                    calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                    calendar.set(Calendar.MINUTE, timePickerState.minute)
                                    calendar.set(Calendar.SECOND, 0)
                                    calendar.set(Calendar.MILLISECOND, 0)

                                    if (calendar.timeInMillis > System.currentTimeMillis()) {
                                        onSchedule(calendar.timeInMillis)
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