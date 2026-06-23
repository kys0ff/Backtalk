package off.kys.backtalk.presentation.screen.onboarding.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import off.kys.backtalk.R
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.model.Thread
import off.kys.backtalk.util.emptyString
import off.kys.backtalk.util.getAssetFile

import kotlinx.collections.immutable.toPersistentList
import off.kys.backtalk.common.Constants
import off.kys.backtalk.presentation.model.MessageUiModel

object OnboardingMocks {
    private fun MessageEntity.toMockUiModel(): MessageUiModel {
        val visibleText = editedText ?: text
        return MessageUiModel(
            id = id,
            text = text,
            timestamp = timestamp,
            repliedToId = repliedToId,
            editedText = editedText,
            editedAt = editedAt,
            voicePath = voicePath,
            voiceDuration = voiceDuration,
            waveformData = waveformData?.toPersistentList(),
            isReminder = isReminder,
            originalCreationTimestamp = originalCreationTimestamp,
            scheduledTimestamp = scheduledTimestamp,
            isPinned = isPinned,
            mediaPath = mediaPath,
            mediaPaths = mediaPaths?.toPersistentList(),
            mediaType = mediaType,
            isDefaultCaption = false,
            isLocked = false,
            canEdit = voicePath == null,
            hasImages = !mediaPath.isNullOrEmpty() || !mediaPaths.isNullOrEmpty(),
            hasVoice = voicePath != null,
            hasText = visibleText.isNotEmpty(),
            hasRepliedMessage = repliedToId != null,
            hasTags = isReminder || isPinned,
            isImageOnly = false,
            visibleText = visibleText
        )
    }

    val messageUi1 @Composable get() = message1.toMockUiModel()
    val messageUi2 @Composable get() = message2.toMockUiModel()
    val voiceMessageUi @Composable get() = voiceMessage.toMockUiModel()
    val reminderMessageUi @Composable get() = reminderMessage.toMockUiModel()
    val imageMessageUi @Composable get() = imageMessage.toMockUiModel()

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

    val imageMessage: MessageEntity
        @Composable get() {
            val context = LocalContext.current
            return MessageEntity(
                id = MessageId(5),
                text = "Check out this photo!",
                timestamp = System.currentTimeMillis() - 30000,
                repliedToId = null,
                mediaPath = context.getAssetFile("duck.jpg").absolutePath
            )
        }

    val threadMock
        @Composable get() = Thread(
            root = message1,
            replies = listOf(message2)
        )
}
