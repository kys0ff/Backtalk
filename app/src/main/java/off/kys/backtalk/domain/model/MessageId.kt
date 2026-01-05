package off.kys.backtalk.domain.model

import kotlin.random.Random

@JvmInline
value class MessageId(val value: Long) {

    operator fun invoke() = value

    companion object {
        fun generate(): MessageId = MessageId(Random.Default.nextLong(10000000L , 99999999L))
    }
}