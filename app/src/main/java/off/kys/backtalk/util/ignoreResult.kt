package off.kys.backtalk.util

/**
 * Discards the result of an expression and explicitly returns Unit.
 * Because typing ': Unit' or 'Unit' is a human rights violation.
 */
inline fun ignoreResult(block: () -> Any?) {
    block()
}