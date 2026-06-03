@file:Suppress("unused")

package off.kys.backtalk.presentation.screen.components.size_observer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Constraints

/**
 * Tracks the size of the root/parent composable and stores it under [key].
 */
@Composable
fun Modifier.observeSize(key: Any): Modifier {
    val registry = LocalSizeRegistry.current
    return this.onGloballyPositioned { layoutCoordinates ->
        registry.updateSize(key, layoutCoordinates.size)
    }
}

/**
 * Binds a sub-composable to the size tracked by [key].
 */
@Composable
fun Modifier.applySize(key: Any): Modifier {
    val registry = LocalSizeRegistry.current
    return this.layout { measurable, constraints ->
        val savedSize = registry.getSize(key)
        val targetWidth = savedSize?.width ?: constraints.minWidth
        val targetHeight = savedSize?.height ?: constraints.minHeight
        val placeable = measurable.measure(
            Constraints.fixed(targetWidth, targetHeight)
        )

        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

/**
 * Binds a sub-composable's width to the root width tracked by [key].
 */
@Composable
fun Modifier.applyWidth(key: Any): Modifier {
    val registry = LocalSizeRegistry.current
    return this.layout { measurable, constraints ->
        val savedSize = registry.getSize(key)
        val targetWidth = savedSize?.width ?: constraints.minWidth
        val childConstraints = constraints.copy(
            minWidth = targetWidth,
            maxWidth = targetWidth
        )
        val placeable = measurable.measure(childConstraints)

        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

/**
 * Binds a sub-composable's height to the root height tracked by [key].
 */
@Composable
fun Modifier.applyHeight(key: Any): Modifier {
    val registry = LocalSizeRegistry.current
    return this.layout { measurable, constraints ->
        val savedSize = registry.getSize(key)
        val targetHeight = savedSize?.height ?: constraints.minHeight
        val childConstraints = constraints.copy(
            minHeight = targetHeight,
            maxHeight = targetHeight
        )
        val placeable = measurable.measure(childConstraints)

        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}