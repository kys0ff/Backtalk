package off.kys.backtalk.presentation.screen.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import off.kys.backtalk.R
import off.kys.backtalk.presentation.screen.messages.components.MessageBubble
import off.kys.backtalk.presentation.screen.onboarding.components.OnboardingMocks
import off.kys.backtalk.presentation.screen.threads.components.ThreadItem

enum class OnboardingPage(
    @get:StringRes val title: Int,
    @get:StringRes val description: Int,
) {
    Welcome(
        title = R.string.onboarding_welcome_title,
        description = R.string.onboarding_welcome_desc,
    ) {
        @Composable
        override fun MockContent() {
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
    },
    Messaging(
        title = R.string.onboarding_messaging_title,
        description = R.string.onboarding_messaging_desc
    ) {
        @Composable
        override fun MockContent() {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.85f),
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
        }
    },
    Voice(
        title = R.string.onboarding_voice_title,
        description = R.string.onboarding_voice_desc
    ) {
        @Composable
        override fun MockContent() {
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
    },
    Threads(
        title = R.string.onboarding_threads_title,
        description = R.string.onboarding_threads_desc
    ) {
        @Composable
        override fun MockContent() {
            Box(contentAlignment = Alignment.Center) {
                ThreadItem(
                    thread = OnboardingMocks.threadMock,
                    onClick = {},
                    onThreadCopy = {},
                    onThreadShare = {}
                )
            }
        }
    },
    Rules(
        title = R.string.onboarding_rules_title,
        description = R.string.onboarding_rules_desc
    ) {
        @Composable
        override fun MockContent() {
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
    },
    ReminderInteractive(
        title = R.string.onboarding_reminder_interactive_title,
        description = R.string.onboarding_reminder_interactive_desc
    ) {
        @OptIn(ExperimentalFoundationApi::class)
        @Composable
        override fun MockContent() {
            var text by remember { mutableStateOf("") }
            var showDialog by remember { mutableStateOf(false) }
            var success by remember { mutableStateOf(false) }

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
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = text,
                                onValueChange = { text = it },
                                placeholder = { Text(stringResource(R.string.onboarding_reminder_hint)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .combinedClickable(
                                            onClick = { },
                                            onLongClick = {
                                                if (text.isNotBlank()) showDialog = true
                                            }
                                        ),
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(R.drawable.round_send_24),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
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

                if (showDialog) {
                    Dialog(onDismissRequest = { showDialog = false }) {
                        Card(
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.message_scheduling_select_time),
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Tomorrow at 9:00 AM",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showDialog = false }) {
                                        Text(stringResource(R.string.common_cancel))
                                    }
                                    TextButton(onClick = {
                                        showDialog = false
                                        success = true
                                    }) {
                                        Text(stringResource(R.string.common_ok))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    },
    SyncBackup(
        title = R.string.onboarding_sync_title,
        description = R.string.onboarding_sync_desc
    ) {
        @Composable
        override fun MockContent() {
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
    },
    Security(
        title = R.string.onboarding_security_title,
        description = R.string.onboarding_security_desc
    ) {
        @Composable
        override fun MockContent() {
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
    },
    Permissions(
        title = R.string.onboarding_permissions_title,
        description = R.string.onboarding_permissions_desc
    ) {
        @Composable
        override fun MockContent() {
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
    };

    @Composable
    abstract fun MockContent()
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
