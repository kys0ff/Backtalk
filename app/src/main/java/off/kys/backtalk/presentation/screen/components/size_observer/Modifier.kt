@file:Suppress("unused")

package off.kys.backtalk.presentation.screen.components.size_observer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.max

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
 *
 * @param key The key to look up the size in the [SizeRegistry].
 * @param minWidth Optional minimum width to enforce.
 * @param minHeight Optional minimum height to enforce.
 */
@Composable
fun Modifier.applySize(
    key: Any,
    minWidth: Dp = Dp.Unspecified,
    minHeight: Dp = Dp.Unspecified
): Modifier {
    val registry = LocalSizeRegistry.current
    val density = LocalDensity.current
    return this.layout { measurable, constraints ->
        val savedSize = registry.getSize(key)
        val minWidthPx = if (minWidth != Dp.Unspecified) with(density) { minWidth.roundToPx() } else 0
        val minHeightPx = if (minHeight != Dp.Unspecified) with(density) { minHeight.roundToPx() } else 0

        val targetWidth = max(savedSize?.width ?: 0, minWidthPx)
            .coerceIn(constraints.minWidth, constraints.maxWidth)

        val targetHeight = max(savedSize?.height ?: 0, minHeightPx)
            .coerceIn(constraints.minHeight, constraints.maxHeight)

        val childConstraints = if (savedSize != null) {
            Constraints.fixed(targetWidth, targetHeight)
        } else {
            constraints.copy(
                minWidth = targetWidth,
                minHeight = targetHeight
            )
        }

        val placeable = measurable.measure(childConstraints)

        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

/**
 * Binds a sub-composable's width to the root width tracked by [key].
 *
 * @param key The key to look up the width in the [SizeRegistry].
 * @param minWidth Optional minimum width to enforce.
 */
@Composable
fun Modifier.applyWidth(key: Any, minWidth: Dp = Dp.Unspecified): Modifier {
    val registry = LocalSizeRegistry.current
    val density = LocalDensity.current
    return this.layout { measurable, constraints ->
        val savedSize = registry.getSize(key)
        val minWidthPx = if (minWidth != Dp.Unspecified) with(density) { minWidth.roundToPx() } else 0

        val targetWidth = max(savedSize?.width ?: 0, minWidthPx)
            .coerceIn(constraints.minWidth, constraints.maxWidth)

        val childConstraints = constraints.copy(
            minWidth = targetWidth,
            maxWidth = if (savedSize != null) targetWidth else constraints.maxWidth
        )
        val placeable = measurable.measure(childConstraints)

        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

/**
 * Binds a sub-composable's height to the root height tracked by [key].
 *
 * @param key The key to look up the height in the [SizeRegistry].
 * @param minHeight Optional minimum height to enforce.
 */
@Composable
fun Modifier.applyHeight(key: Any, minHeight: Dp = Dp.Unspecified): Modifier {
    val registry = LocalSizeRegistry.current
    val density = LocalDensity.current
    return this.layout { measurable, constraints ->
        val savedSize = registry.getSize(key)
        val minHeightPx = if (minHeight != Dp.Unspecified) with(density) { minHeight.roundToPx() } else 0

        val targetHeight = max(savedSize?.height ?: 0, minHeightPx)
            .coerceIn(constraints.minHeight, constraints.maxHeight)

        val childConstraints = constraints.copy(
            minHeight = targetHeight,
            maxHeight = if (savedSize != null) targetHeight else constraints.maxHeight
        )
        val placeable = measurable.measure(childConstraints)

        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}
