package off.kys.backtalk.common.state

sealed class SelectState<out T> {

    object Idle : SelectState<Nothing>()

    data class Selecting<T>(val selected: Set<T>) : SelectState<T>()
    data class Confirming<T>(val selected: Set<T>) : SelectState<T>()
    data class Performing<T>(val selected: Set<T>) : SelectState<T>() // e.g., deleting, processing
    object Finished : SelectState<Nothing>()

    // Convenience helper to get current selected items if applicable
    val currentSelection: Set<T>
        get() = when (this) {
            is Selecting -> this.selected
            is Confirming -> this.selected
            is Performing -> this.selected
            else -> emptySet()
        }
}
