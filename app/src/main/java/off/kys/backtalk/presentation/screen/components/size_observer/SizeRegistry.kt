package off.kys.backtalk.presentation.screen.components.size_observer

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.unit.IntSize

class SizeRegistry {
    private val sizes = mutableStateMapOf<Any, IntSize>()

    fun updateSize(key: Any, size: IntSize) {
        if (sizes[key] != size) {
            sizes[key] = size
        }
    }

    fun getSize(key: Any): IntSize? = sizes[key]
}