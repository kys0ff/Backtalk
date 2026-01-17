package off.kys.preferences.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Returns the result of [block] if [value] is not null, otherwise returns null.
 *
 * @param value The value to check.
 * @param block The block to execute if [value] is not null.
 * @return The result of [block] if [value] is not null, otherwise null.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <T, R> putIfNotNullOrNull(value: T?, block: (T) -> R?): R? {
    contract {
        // If the function returns anything that is NOT null, 
        // it implies 'value' is not null.
        returnsNotNull() implies (value != null)
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return if (value != null) block(value) else null
}