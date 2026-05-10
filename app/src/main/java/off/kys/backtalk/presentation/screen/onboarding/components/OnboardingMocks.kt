package off.kys.backtalk.presentation.screen.onboarding.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import off.kys.backtalk.R
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.model.Thread
import off.kys.backtalk.util.emptyString

object OnboardingMocks {
    val message1
        @Composable get() = MessageEntity(
            id = MessageId(1),
            text = stringResource(R.string.onboarding_mock_message_1),
            timestamp = System.currentTimeMillis() - 600000,
            repliedToId = null
        )

    val message2
        @Composable get() = MessageEntity(
            id = MessageId(2),
            text = stringResource(R.string.onboarding_mock_message_2),
            timestamp = System.currentTimeMillis() - 300000,
            repliedToId = MessageId(1)
        )

    val voiceMessage
        @Composable get() = MessageEntity(
            id = MessageId(3),
            text = emptyString(),
            timestamp = System.currentTimeMillis() - 120000,
            repliedToId = null,
            voicePath = stringResource(R.string.onboarding_mock_voice_path),
            voiceDuration = 5000L,
            waveformData = listOf(0.1f, 0.5f, 0.3f, 0.8f, 0.4f, 0.6f, 0.2f)
        )

    val reminderMessage
        @Composable get() = MessageEntity(
            id = MessageId(4),
            text = stringResource(R.string.onboarding_mock_reminder),
            timestamp = System.currentTimeMillis() - 60000,
            repliedToId = null,
            isReminder = true,
            originalCreationTimestamp = System.currentTimeMillis() - 3600000,
            scheduledTimestamp = System.currentTimeMillis()
        )

    val threadMock
        @Composable get() = Thread(
            root = message1,
            replies = listOf(message2)
        )
}
