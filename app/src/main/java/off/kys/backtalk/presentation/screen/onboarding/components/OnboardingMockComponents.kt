package off.kys.backtalk.presentation.screen.onboarding.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.presentation.event.InputBarEvent
import off.kys.backtalk.presentation.screen.messages.components.MessageBubble
import off.kys.backtalk.presentation.screen.messages.components.StatelessInputBar
import off.kys.backtalk.presentation.screen.threads.components.ThreadItem
import off.kys.backtalk.presentation.state.messages.InputBarEffect
import off.kys.backtalk.presentation.state.messages.InputBarUiState
import off.kys.backtalk.presentation.status.SchedulingStage

@Composable
fun WelcomeMock() {
    Box(
        modifier = Modifier.height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.round_chat_bubble_outline_24),
            contentDescription = null,
            modifier = Modifier.size(160.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun MessagingMock() {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MessageBubble(
                message = OnboardingMocks.messageUi1,
                repliedMessage = null,
                blinkMessageId = null,
                isTop = true,
                isBottom = true,
                selectMode = false,
                isSelected = false,
                onReplyPreviewClick = {},
                onClick = {},
                onLongClick = {},
                hapticFeedbackEnabled = false
            )

            MessageBubble(
                message = OnboardingMocks.messageUi2,
                repliedMessage = OnboardingMocks.messageUi1,
                blinkMessageId = null,
                isTop = true,
                isBottom = true,
                selectMode = false,
                isSelected = false,
                onReplyPreviewClick = {},
                onClick = {},
                onLongClick = {},
                hapticFeedbackEnabled = false
            )

            MessageBubble(
                message = OnboardingMocks.reminderMessageUi,
                repliedMessage = null,
                blinkMessageId = null,
                isTop = true,
                isBottom = true,
                selectMode = false,
                isSelected = false,
                onReplyPreviewClick = {},
                onClick = {},
                onLongClick = {},
                hapticFeedbackEnabled = false
            )

            MessageBubble(
                message = OnboardingMocks.imageMessageUi,
                repliedMessage = null,
                blinkMessageId = null,
                isTop = true,
                isBottom = true,
                selectMode = false,
                isSelected = false,
                onReplyPreviewClick = {},
                onClick = {},
                onLongClick = {},
                hapticFeedbackEnabled = false
            )
        }

        // Visual hint that there is more content below
        if (scrollState.canScrollForward) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        )
                    )
            )

            Icon(
                painterResource(R.drawable.round_keyboard_arrow_down_24),
                contentDescription = "Scroll for more",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun VoiceMock() {
    Box(
        modifier = Modifier
            .height(320.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        MessageBubble(
            message = OnboardingMocks.voiceMessageUi,
            repliedMessage = null,
            blinkMessageId = null,
            isTop = true,
            isBottom = true,
            selectMode = false,
            isSelected = false,
            onReplyPreviewClick = {},
            onClick = {},
            onLongClick = {},
            hapticFeedbackEnabled = false
        )
    }
}

@Composable
fun ThreadsMock() {
    Box(
        modifier = Modifier.height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        ThreadItem(
            thread = OnboardingMocks.threadMock,
            onClick = {},
            onThreadCopy = {},
            onThreadShare = {}
        )
    }
}

@Composable
fun RulesMock() {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        RuleItem(
            icon = R.drawable.round_calendar_clock_24,
            title = R.string.onboarding_rules_reminders_title,
            description = R.string.onboarding_rules_reminders_desc,
            color = MaterialTheme.colorScheme.primary
        )
        RuleItem(
            icon = R.drawable.round_update_24,
            title = R.string.onboarding_rules_edit_delete_title,
            description = R.string.onboarding_rules_edit_delete_desc,
            color = MaterialTheme.colorScheme.secondary
        )
        RuleItem(
            icon = R.drawable.round_edit_24,
            title = R.string.onboarding_rules_one_edit_title,
            description = R.string.onboarding_rules_one_edit_desc,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun ReminderInteractiveMock() {
    var success by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf(InputBarUiState()) }
    var showLongPressHint by remember { mutableStateOf(false) }
    val effect = remember { MutableSharedFlow<InputBarEffect>() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .height(320.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(
                visible = !success,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedVisibility(
                        visible = showLongPressHint,
                        enter = fadeIn() + slideInVertically { it },
                        exit = fadeOut() + slideOutVertically { it }
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.round_info_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.onboarding_rules_reminders_desc),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    StatelessInputBar(
                        state = state,
                        onEvent = { event ->
                            when (event) {
                                is InputBarEvent.SendMessage -> {
                                    if (state.textFieldState.text.isNotBlank()) {
                                        showLongPressHint = true
                                        coroutineScope.launch {
                                            effect.emit(InputBarEffect.TriggerShake)
                                        }
                                    }
                                }
                                is InputBarEvent.RequestSchedule -> {
                                    showLongPressHint = false
                                    state = state.copy(schedulingStage = SchedulingStage.SelectingDate)
                                }
                                is InputBarEvent.ChangeSchedulingStage -> {
                                    state = state.copy(schedulingStage = event.stage)
                                }
                                is InputBarEvent.ScheduleMessage -> {
                                    success = true
                                    state = state.copy(schedulingStage = SchedulingStage.Hidden)
                                }
                                is InputBarEvent.UpdateOffsetX -> {
                                    state = state.copy(offsetX = event.x)
                                }
                                else -> {}
                            }
                        },
                        effect = effect
                    )
                }
            }

            AnimatedVisibility(
                visible = success,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_check_24),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.onboarding_reminder_success),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SyncMock() {
    Box(
        modifier = Modifier.height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.round_sync_24),
            contentDescription = null,
            modifier = Modifier.size(140.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun SecurityMock() {
    Box(
        modifier = Modifier.height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.round_lock_24),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PermissionsMock() {
    Box(
        modifier = Modifier.height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.round_security_24),
            contentDescription = null,
            modifier = Modifier.size(140.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun RuleItem(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes description: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
