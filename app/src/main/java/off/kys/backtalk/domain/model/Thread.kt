package off.kys.backtalk.domain.model

import off.kys.backtalk.data.local.entity.MessageEntity

/**
 * Represents a thread of messages.
 *
 * @property root The starting message of the thread.
 * @property replies The subsequent messages in the thread.
 */
data class Thread(
    val root: MessageEntity,
    val replies: List<MessageEntity>,
    val repliedTo: MessageEntity? = null
) : java.io.Serializable {
    /**
     * The total number of messages in the thread.
     */
    val size: Int get() = 1 + replies.size

}
