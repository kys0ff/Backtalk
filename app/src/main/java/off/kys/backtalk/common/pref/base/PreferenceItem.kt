package off.kys.backtalk.common.pref.base

import androidx.compose.runtime.State

/**
 * Interface for a structured preference item that handles persistence and UI observation.
 */
interface PreferenceItem<T> {
    val key: String
    val defaultValue: T
    val state: State<T>
    var value: T
    fun refresh()
    fun serialize(): String?
    fun deserialize(value: String)
}