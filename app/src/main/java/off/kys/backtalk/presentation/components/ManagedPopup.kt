package off.kys.backtalk.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun ManagedPopup(
    state: PopupState,
    modifier: Modifier = Modifier,
    // Changed to TopStart to align left edges by default (Standard dropdown behavior)
    popupAlignment: Alignment = Alignment.TopStart,
    // Default offset: 0 x, 4dp y (small gap below anchor)
    popupOffset: IntOffset = IntOffset(0, 0),
    anchor: @Composable (PopupState) -> Unit,
    content: @Composable ColumnScope.(PopupState) -> Unit
) {
    // We need to measure the anchor to know where to place the popup below it
    var anchorSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    // Optional: Calculate a default vertical gap (e.g., 8dp) in pixels
    val verticalGapPx = with(density) { 8.dp.roundToPx() }

    Box(
        modifier = modifier
            .wrapContentSize()
            .onSizeChanged { anchorSize = it } // Capture the anchor's size
    ) {
        // 1. The Trigger View
        anchor(state)

        // 2. The Popup Logic
        if (state.isVisible) {
            // Calculate position: Start at Top-Left of Box + Height of Box + User Offset
            val effectiveOffset = if (popupAlignment == Alignment.TopStart || popupAlignment == Alignment.TopEnd) {
                IntOffset(
                    x = popupOffset.x - anchorSize.width,
                    y = anchorSize.height - (verticalGapPx * 3)
                )
            } else {
                popupOffset
            }

            Popup(
                alignment = popupAlignment,
                offset = effectiveOffset,
                onDismissRequest = { state.hide() },
                properties = PopupProperties(focusable = true)
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .widthIn(min = 150.dp),
                    // Removed top padding here as we now handle spacing via the Popup offset
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        content(state)
                    }
                }
            }
        }
    }
}