package off.kys.backtalk.common.pref.model

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import off.kys.backtalk.common.pref.base.PreferenceItem

/**
 * Base class for preference items.
 */
abstract class BasePreferenceItem<T>(
    protected val prefs: SharedPreferences,
    override val key: String,
    override val defaultValue: T
) : PreferenceItem<T> {
    protected abstract val internalState: MutableState<T>
    override val state: State<T> get() = internalState

    override var value: T
        get() = internalState.value
        set(value) {
            internalState.value = value
            writeToPrefs(value)
        }

    override fun refresh() {
        internalState.value = readFromPrefs()
    }

    abstract fun readFromPrefs(): T
    abstract fun writeToPrefs(value: T)
}