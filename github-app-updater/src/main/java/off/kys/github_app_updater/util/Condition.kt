package off.kys.github_app_updater.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Runs the given block if the condition is true.
 *
 * @param condition The condition to check.
 * @param block The block to execute if the condition is true.
 * @return The receiver object.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <T> T.runIf(condition: Boolean, block: T.() -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (condition) block()
    return this
}