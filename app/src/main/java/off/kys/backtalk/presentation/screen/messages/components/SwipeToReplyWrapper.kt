package off.kys.backtalk.presentation.screen.messages.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import off.kys.backtalk.common.pref.BacktalkPreferences
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A wrapper component that enables swipe-to-action (e.g., swipe-to-reply) functionality
 * for its content. It supports both left and right swipes with customizable icons,
 * resistance effects, and haptic feedback upon reaching the activation threshold.
 *
 * @param onSwipeLeft Optional callback triggered when the user swipes left beyond the threshold.
 * @param onSwipeRight Optional callback triggered when the user swipes right beyond the threshold.
 * @param leftIconRes The drawable resource ID for the icon displayed during a left swipe.
 * @param rightIconRes The drawable resource ID for the icon displayed during a right swipe.
 * @param content The composable content to be wrapped and made swipeable.
 */
@Composable
fun SwipeToReplyWrapper(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    @DrawableRes leftIconRes: Int,
    @DrawableRes rightIconRes: Int,
    content: @Composable () -> Unit
) {
    val preferences = koinInject<BacktalkPreferences>()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val actionThreshold = remember(density) { with(density) { 60.dp.toPx() } }
    val maxDrag = remember(density) { with(density) { 100.dp.toPx() } }

    val offsetX = remember { Animatable(0f) }

    val isPastThreshold by remember {
        derivedStateOf { abs(offsetX.value) >= actionThreshold }
    }

    val currentSwipeDirection by remember {
        derivedStateOf {
            when {
                offsetX.value > 0 -> SwipeDirection.RIGHT
                offsetX.value < 0 -> SwipeDirection.LEFT
                else -> null
            }
        }
    }

    val iconScale by animateFloatAsState(
        targetValue = if (isPastThreshold) 1.2f else 0.8f,
        animationSpec = spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMedium),
        label = "IconScale"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isPastThreshold)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        label = "IconTint"
    )

    var hasVibratedThreshold = remember { false }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(onSwipeLeft, onSwipeRight) {
                detectHorizontalDragGestures(
                    onDragStart = { hasVibratedThreshold = false },
                    onDragEnd = {
                        scope.launch {
                            if (isPastThreshold) {
                                if (offsetX.value > 0) onSwipeRight?.invoke()
                                else onSwipeLeft?.invoke()
                            }
                            offsetX.animateTo(
                                0f,
                                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)
                            )
                        }
                    },
                    onDragCancel = { scope.launch { offsetX.animateTo(0f) } },
                    onHorizontalDrag = { change, dragAmount ->
                        val currentX = offsetX.value
                        val dragFactor = 1f - (abs(currentX) / (maxDrag * 1.5f))
                        val resistedDrag = dragAmount * dragFactor.coerceIn(0.2f, 1f)
                        val rawNewOffset = currentX + resistedDrag

                        val newOffset = when {
                            onSwipeRight != null && onSwipeLeft != null -> rawNewOffset.coerceIn(
                                -maxDrag,
                                maxDrag
                            )

                            onSwipeRight != null -> rawNewOffset.coerceIn(0f, maxDrag)
                            onSwipeLeft != null -> rawNewOffset.coerceIn(-maxDrag, 0f)
                            else -> 0f
                        }

                        if (preferences.hapticFeedbackEnabled) {
                            if (abs(newOffset) >= actionThreshold && !hasVibratedThreshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                hasVibratedThreshold = true
                            } else if (abs(newOffset) < actionThreshold) {
                                hasVibratedThreshold = false
                            }
                        }

                        scope.launch { offsetX.snapTo(newOffset) }
                        change.consume()
                    }
                )
            }
    ) {
        if (currentSwipeDirection != null) {
            val isRightSwipe = currentSwipeDirection == SwipeDirection.RIGHT
            val currentIcon = if (isRightSwipe) rightIconRes else leftIconRes

            Box(
                modifier = Modifier
                    .align(if (isRightSwipe) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        alpha = (abs(offsetX.value) / actionThreshold).coerceIn(0f, 1f)
                        scaleX = iconScale
                        scaleY = iconScale
                        translationX = if (isRightSwipe) {
                            (offsetX.value - actionThreshold).coerceAtMost(0f) / 2f
                        } else {
                            (offsetX.value + actionThreshold).coerceAtLeast(0f) / 2f
                        }
                    }
            ) {
                Icon(
                    painter = painterResource(currentIcon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            rotationY = if (isRightSwipe) 0f else 180f
                        },
                    tint = iconTint
                )
            }
        }

        Box(
            modifier = Modifier.offset {
                IntOffset(offsetX.value.roundToInt(), 0)
            }
        ) {
            content()
        }
    }
}

/**
 * Internal enum to track the direction of the current swipe gesture.
 */
private enum class SwipeDirection {
    LEFT, RIGHT
}