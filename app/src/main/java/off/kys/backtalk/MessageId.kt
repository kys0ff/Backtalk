package off.kys.backtalk

import kotlin.random.Random

@JvmInline
value class MessageId(val value: Long) {

    operator fun invoke() = value

    companion object {
        fun generate(): MessageId = MessageId(Random.nextLong(10000000L , 99999999L))
    }
}