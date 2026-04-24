package off.kys.backtalk.domain.model

import kotlin.random.Random

/**
 * Represents a unique identifier for a message.
 *
 * @property value The underlying long value of the message ID.
 */
@JvmInline
value class MessageId(val value: Long) {

    /**
     * Returns the underlying long value.
     */
    operator fun invoke() = value

    companion object {
        /**
         * Generates a new random [MessageId] within the range of 10,000,000 to 99,999,999.
         *
         * @return A randomly generated [MessageId].
         */
        fun generate(): MessageId = MessageId(Random.nextLong(10000000L , 99999999L))
    }
}
