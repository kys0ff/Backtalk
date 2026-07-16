package off.kys.backtalk.presentation.components.status_scaffold

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import off.kys.backtalk.R
import kotlin.time.Duration.Companion.milliseconds

/**
 * Drop this once near app root (e.g. wrapping the NavHost inside
 * MainActivity's setContent), and every screen below it can trigger a
 * status banner with `LocalStatusController.current.error("...")`.
 *
 * ```
 * setContent {
 *     BacktalkTheme {
 *         GlobalStatusHost {
 *             AppNavHost()
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun GlobalStatusHost(
    controller: StatusController = rememberStatusController(),
    content: @Composable () -> Unit
) {
    ProvideStatusController(controller) {
        Box(Modifier.fillMaxSize()) {
            content()
            StatusOverlay(
                controller = controller,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun ProvideStatusController(
    controller: StatusController = rememberStatusController(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalStatusController provides controller,
        content = content
    )
}

@Composable
fun StatusOverlay(
    controller: StatusController,
    modifier: Modifier = Modifier
) {
    val uiState = controller.state

    AnimatedVisibility(
        visible = uiState.status != ScaffoldStatus.None && uiState.message != null,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = tween(220)),
        exit = shrinkVertically(
            animationSpec = tween(280),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(180)),
        modifier = modifier
    ) {
        val message = uiState.message
        if (message != null) {
            StatusBadge(
                status = uiState.status,
                message = message,
                action = uiState.action,
                onDismiss = controller::dismiss
            )

            if (uiState.autoDismissMillis != null) {
                LaunchedEffect(uiState.revision) {
                    delay(uiState.autoDismissMillis.milliseconds)
                    controller.dismiss()
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: ScaffoldStatus,
    message: StatusMessage,
    action: StatusAction? = null,
    onDismiss: (() -> Unit)? = null
) {
    val targetBackgroundColor = when (status) {
        ScaffoldStatus.Info -> MaterialTheme.colorScheme.primaryContainer
        ScaffoldStatus.Warning -> MaterialTheme.colorScheme.tertiaryContainer
        ScaffoldStatus.Error -> MaterialTheme.colorScheme.errorContainer
        ScaffoldStatus.Loading -> MaterialTheme.colorScheme.secondaryContainer
        ScaffoldStatus.None -> Color.Transparent
    }

    val targetTextColor = when (status) {
        ScaffoldStatus.Info -> MaterialTheme.colorScheme.onPrimaryContainer
        ScaffoldStatus.Warning -> MaterialTheme.colorScheme.onTertiaryContainer
        ScaffoldStatus.Error -> MaterialTheme.colorScheme.onErrorContainer
        ScaffoldStatus.Loading -> MaterialTheme.colorScheme.onSecondaryContainer
        ScaffoldStatus.None -> Color.Transparent
    }

    val targetBorderColor = when (status) {
        ScaffoldStatus.Info -> MaterialTheme.colorScheme.primary
        ScaffoldStatus.Warning -> MaterialTheme.colorScheme.tertiary
        ScaffoldStatus.Error -> MaterialTheme.colorScheme.error
        ScaffoldStatus.Loading -> MaterialTheme.colorScheme.secondary
        ScaffoldStatus.None -> Color.Transparent
    }.copy(alpha = 0.25f)

    val backgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 300),
        label = "BadgeBackground"
    )

    val textColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(durationMillis = 300),
        label = "BadgeText"
    )

    val borderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 300),
        label = "BadgeBorder"
    )

    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Surface(
        color = backgroundColor,
        contentColor = textColor,
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Box(
            modifier = Modifier.padding(
                top = statusBarTopPadding + 12.dp,
                bottom = 14.dp,
                start = 16.dp,
                end = 16.dp
            ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = status,
                    transitionSpec = {
                        (scaleIn(
                            initialScale = 0.6f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeIn(tween(200))) togetherWith
                            (scaleOut(targetScale = 0.6f, animationSpec = tween(150)) + fadeOut(tween(150)))
                    },
                    label = "BadgeIcon"
                ) { animatedStatus ->
                    when (animatedStatus) {
                        ScaffoldStatus.Loading -> {
                            CircularProgressIndicator(
                                color = textColor,
                                strokeWidth = 2.dp,
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(18.dp)
                            )
                        }

                        ScaffoldStatus.Info, ScaffoldStatus.Warning, ScaffoldStatus.Error -> {
                            val animatedIcon = when (animatedStatus) {
                                ScaffoldStatus.Info -> R.drawable.round_info_24
                                ScaffoldStatus.Warning -> R.drawable.round_warning_24
                                ScaffoldStatus.Error -> R.drawable.round_error_24
                            }
                            Icon(
                                painter = painterResource(animatedIcon),
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(20.dp)
                            )
                        }

                        else -> {}
                    }
                }

                AnimatedContent(
                    targetState = message.asString(),
                    transitionSpec = {
                        (fadeIn(tween(220)) + expandVertically(tween(220))) togetherWith
                            (fadeOut(tween(120)))
                    },
                    label = "BadgeMessage",
                    modifier = Modifier.weight(1f, fill = false)
                ) { text ->
                    Text(
                        text = text,
                        color = textColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (action != null) {
                    TextButton(onClick = action.onClick) {
                        Text(
                            text = action.label,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                if (onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.round_close_24),
                            contentDescription = stringResource(R.string.common_close),
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}