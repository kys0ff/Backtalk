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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import off.kys.backtalk.util.emptyString
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

/**
 * A wrapper component that enables swipe-to-action (e.g., swipe-to-reply) functionality
 * for its content. It dynamically adjusts to LTR and RTL layouts natively.
 *
 * @param onSwipeStart Optional callback triggered when swiping from Start-to-End (reveals start action).
 * @param onSwipeEnd Optional callback triggered when swiping from End-to-Start (reveals end action).
 * @param startIconRes The drawable resource ID for the icon displayed during a start-to-end swipe.
 * @param endIconRes The drawable resource ID for the icon displayed during an end-to-start swipe.
 * @param showHint If true, a one-time swipe animation is performed to hint functionality.
 * @param onHintShown Callback invoked once the hint animation has completed.
 * @param content The composable content to be wrapped and made swipeable.
 */
@Composable
fun SwipeToReplyWrapper(
    onSwipeStart: (() -> Unit)? = null,
    onSwipeEnd: (() -> Unit)? = null,
    @DrawableRes startIconRes: Int,
    @DrawableRes endIconRes: Int,
    showHint: Boolean = false,
    onHintShown: () -> Unit = {},
    hapticFeedbackEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val actionThreshold = remember(density) { with(density) { 60.dp.toPx() } }
    val maxDrag = remember(density) { with(density) { 100.dp.toPx() } }

    val directionalOffset = remember { Animatable(0f) }

    LaunchedEffect(showHint) {
        if (showHint && onSwipeStart != null && onSwipeEnd != null) {
            delay(500.milliseconds)
            directionalOffset.animateTo(
                targetValue = actionThreshold * 0.8f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
            )
            delay(800.milliseconds)
            directionalOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
            )
            delay(300.milliseconds)
            directionalOffset.animateTo(
                targetValue = -actionThreshold * 0.8f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
            )
            delay(800.milliseconds)
            directionalOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
            )
            onHintShown()
        } else if (showHint) {
            onHintShown()
        }
    }

    val isPastThreshold by remember {
        derivedStateOf { abs(directionalOffset.value) >= actionThreshold }
    }

    val currentSwipeDirection by remember {
        derivedStateOf {
            when {
                directionalOffset.value > 0 -> SwipeDirection.START
                directionalOffset.value < 0 -> SwipeDirection.END
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
            .pointerInput(onSwipeStart, onSwipeEnd) {
                detectHorizontalDragGestures(
                    onDragStart = { hasVibratedThreshold = false },
                    onDragEnd = {
                        scope.launch {
                            if (isPastThreshold) {
                                if (directionalOffset.value > 0) onSwipeStart?.invoke()
                                else onSwipeEnd?.invoke()
                            }
                            directionalOffset.animateTo(
                                0f,
                                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)
                            )
                        }
                    },
                    onDragCancel = { scope.launch { directionalOffset.animateTo(0f) } },
                    onHorizontalDrag = { change, dragAmount ->
                        val directionalDrag = if (isRtl) -dragAmount else dragAmount

                        val currentX = directionalOffset.value
                        val dragFactor = 1f - (abs(currentX) / (maxDrag * 1.5f))
                        val resistedDrag = directionalDrag * dragFactor.coerceIn(0.2f, 1f)
                        val rawNewOffset = currentX + resistedDrag

                        val newOffset = when {
                            onSwipeStart != null && onSwipeEnd != null -> rawNewOffset.coerceIn(
                                -maxDrag,
                                maxDrag
                            )

                            onSwipeStart != null -> rawNewOffset.coerceIn(0f, maxDrag)
                            onSwipeEnd != null -> rawNewOffset.coerceIn(-maxDrag, 0f)
                            else -> 0f
                        }

                        if (hapticFeedbackEnabled) {
                            if (abs(newOffset) >= actionThreshold && !hasVibratedThreshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                hasVibratedThreshold = true
                            } else if (abs(newOffset) < actionThreshold) {
                                hasVibratedThreshold = false
                            }
                        }

                        scope.launch { directionalOffset.snapTo(newOffset) }
                        change.consume()
                    }
                )
            }
    ) {
        if (currentSwipeDirection != null) {
            val isStartSwipe = currentSwipeDirection == SwipeDirection.START
            val currentIcon = if (isStartSwipe) startIconRes else endIconRes
            val alignment = if (isStartSwipe) Alignment.CenterStart else Alignment.CenterEnd

            Box(
                modifier = Modifier
                    .align(alignment)
                    .padding(horizontal = 16.dp)
                    .offset {
                        val xOffset = if (isStartSwipe) {
                            (directionalOffset.value - actionThreshold).coerceAtMost(0f) / 2f
                        } else {
                            (directionalOffset.value + actionThreshold).coerceAtLeast(0f) / 2f
                        }
                        IntOffset(xOffset.roundToInt(), 0)
                    }
                    .graphicsLayer {
                        alpha = (abs(directionalOffset.value) / actionThreshold).coerceIn(0f, 1f)
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            ) {
                Icon(
                    painter = painterResource(currentIcon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationY = if (isStartSwipe) 0f else 180f },
                    tint = iconTint
                )
            }
        }

        Box(
            modifier = Modifier.offset {
                IntOffset(directionalOffset.value.roundToInt(), 0)
            }
        ) {
            content()
        }
    }
}

private enum class SwipeDirection {
    START, END
}