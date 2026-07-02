package off.kys.backtalk.util

/**
 * Discards the result of an expression and explicitly returns Unit.
 */
inline fun ignoreResult(block: () -> Any?) {
    block()
}