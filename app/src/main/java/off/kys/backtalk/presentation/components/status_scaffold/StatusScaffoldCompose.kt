package off.kys.backtalk.presentation.components.status_scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R

@Composable
fun StatusScaffold(
    status: ScaffoldStatus,
    modifier: Modifier = Modifier,
    message: StatusMessage? = null,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets
    ) { innerPadding ->

        val layoutDirection = LocalLayoutDirection.current

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedVisibility(
                visible = status != ScaffoldStatus.None && message != null,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(
                    animationSpec = tween(
                        300
                    )
                ),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(
                    animationSpec = tween(
                        300
                    )
                )
            ) {
                Box(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
                    if (message != null) {
                        StatusBadge(status = status, message = message)
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                content(
                    PaddingValues(
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection),
                        top = if (status == ScaffoldStatus.None || message == null) innerPadding.calculateTopPadding() else 0.dp,
                        bottom = innerPadding.calculateBottomPadding()
                    )
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ScaffoldStatus, message: StatusMessage) {
    val targetBackgroundColor = when (status) {
        ScaffoldStatus.Info -> MaterialTheme.colorScheme.primaryContainer
        ScaffoldStatus.Warning -> MaterialTheme.colorScheme.errorContainer
        ScaffoldStatus.Error -> MaterialTheme.colorScheme.error
        ScaffoldStatus.None -> Color.Transparent
    }

    val targetTextColor = when (status) {
        ScaffoldStatus.Info -> MaterialTheme.colorScheme.onPrimaryContainer
        ScaffoldStatus.Warning -> MaterialTheme.colorScheme.onErrorContainer
        ScaffoldStatus.Error -> MaterialTheme.colorScheme.onError
        ScaffoldStatus.None -> Color.Transparent
    }

    val icon = when (status) {
        ScaffoldStatus.Info -> R.drawable.round_info_24
        ScaffoldStatus.Warning -> R.drawable.round_warning_24
        ScaffoldStatus.Error -> R.drawable.round_error_24
        ScaffoldStatus.None -> null
    }

    val backgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 250),
        label = "BadgeBackground"
    )

    val textColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(durationMillis = 250),
        label = "BadgeText"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 10.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(18.dp)
                )
            }
            Text(
                text = message.asString(),
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}