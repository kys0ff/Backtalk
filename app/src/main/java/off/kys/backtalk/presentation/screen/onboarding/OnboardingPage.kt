package off.kys.backtalk.presentation.screen.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
                    messageEntity = OnboardingMocks.message1,
                    repliedMessageEntity = null,
                    blinkMessageId = null,
                    isTop = true,
                    isBottom = true,
                    selectMode = false,
                    isSelected = false,
                    onReplyPreviewClick = {},
                    onClick = {},
                    onLongClick = {}
                )
                MessageBubble(
                    messageEntity = OnboardingMocks.message2,
                    repliedMessageEntity = OnboardingMocks.message1,
                    blinkMessageId = null,
                    isTop = true,
                    isBottom = true,
                    selectMode = false,
                    isSelected = false,
                    onReplyPreviewClick = {},
                    onClick = {},
                    onLongClick = {}
                )
                MessageBubble(
                    messageEntity = OnboardingMocks.reminderMessage,
                    repliedMessageEntity = null,
                    blinkMessageId = null,
                    isTop = true,
                    isBottom = true,
                    selectMode = false,
                    isSelected = false,
                    onReplyPreviewClick = {},
                    onClick = {},
                    onLongClick = {}
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
                    messageEntity = OnboardingMocks.voiceMessage,
                    repliedMessageEntity = null,
                    blinkMessageId = null,
                    isTop = true,
                    isBottom = true,
                    selectMode = false,
                    isSelected = false,
                    onReplyPreviewClick = {},
                    onClick = {},
                    onLongClick = {}
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
