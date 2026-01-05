package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.common.VibrationManager
import org.koin.compose.koinInject
import kotlin.math.roundToInt

/**
 * A wrapper that adds swipe-to-trigger functionality.
 * Swiping right reveals a reply icon and triggers the callback.
 */
@Composable
fun SwipeToReplyWrapper(
    onSwipe: () -> Unit,
    content: @Composable () -> Unit
) {
    val vibrationManager = koinInject<VibrationManager>()
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val actionThreshold = with(density) { 60.dp.toPx() }
    val maxDrag = with(density) { 90.dp.toPx() }

    val progress = (offsetX.value / actionThreshold).coerceIn(0f, 1f)
    val isPastThreshold = offsetX.value >= actionThreshold

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {

                // 🔹 Gesture-local state (NOT Compose state)
                var hasVibratedThreshold = false

                detectHorizontalDragGestures(
                    onDragStart = {
                        hasVibratedThreshold = false
                    },
                    onDragEnd = {
                        scope.launch {
                            if (offsetX.value >= actionThreshold) {
                                onSwipe()
                            }
                            offsetX.animateTo(
                                0f,
                                spring(
                                    Spring.DampingRatioLowBouncy,
                                    Spring.StiffnessMediumLow
                                )
                            )
                        }
                    },
                    onDragCancel = {
                        scope.launch { offsetX.animateTo(0f) }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        val newOffset =
                            (offsetX.value + dragAmount).coerceIn(0f, maxDrag)

                        if (newOffset >= actionThreshold && !hasVibratedThreshold) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            vibrationManager.vibrate()
                            hasVibratedThreshold = true
                        } else if (newOffset < actionThreshold) {
                            hasVibratedThreshold = false
                        }

                        scope.launch { offsetX.snapTo(newOffset) }
                        change.consume()
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .alpha(progress)
                .scale(0.6f + (0.4f * progress))
        ) {
            Icon(
                painter = painterResource(R.drawable.round_reply_24),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isPastThreshold)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
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