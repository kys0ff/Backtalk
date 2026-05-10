package off.kys.backtalk.presentation.screen.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.presentation.state.OnboardingUiState
import off.kys.backtalk.presentation.theme.BacktalkTheme
import off.kys.backtalk.presentation.viewmodel.OnboardingViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.absoluteValue

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val viewModel = koinViewModel<OnboardingViewModel>()
    val state by viewModel.state.collectAsState()
    
    OnboardingScreenContent(
        state = state,
        onUpdatePermissions = viewModel::updatePermissionStates,
        onFinished = onFinished
    )
}

@Composable
fun OnboardingScreenContent(
    state: OnboardingUiState,
    onUpdatePermissions: () -> Unit,
    onFinished: () -> Unit
) {
    val pagerState = rememberPagerState { OnboardingPage.entries.size }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        onUpdatePermissions()
    }

    Scaffold(
        bottomBar = {
            OnboardingBottomBar(
                currentPage = pagerState.currentPage,
                pageCount = pagerState.pageCount,
                onNext = {
                    if (pagerState.currentPage < pagerState.pageCount - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinished()
                    }
                },
                onSkip = onFinished
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.Top
            ) { pageIndex ->
                val page = OnboardingPage.entries[pageIndex]
                OnboardingPageContent(
                    page = page,
                    state = state,
                    onUpdatePermissions = onUpdatePermissions,
                    modifier = Modifier.graphicsLayer {
                        val pageOffset = (
                                (pagerState.currentPage - pageIndex) + pagerState
                                    .currentPageOffsetFraction
                                )

                        val fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                        
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = fraction
                        )
                        
                        scaleX = lerp(
                            start = 0.9f,
                            stop = 1f,
                            fraction = fraction
                        )
                        scaleY = lerp(
                            start = 0.9f,
                            stop = 1f,
                            fraction = fraction
                        )
                        
                        translationX = pageOffset * size.width * 0.1f
                    }
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    state: OnboardingUiState,
    onUpdatePermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(top = 32.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = scrollState.value * 0.4f
                    alpha = 1f - (scrollState.value.toFloat() / 1000f).coerceIn(0f, 1f)
                    scaleX = 1f - (scrollState.value.toFloat() / 3000f).coerceIn(0f, 0.15f)
                    scaleY = 1f - (scrollState.value.toFloat() / 3000f).coerceIn(0f, 0.15f)
                }
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            page.MockContent()
        }

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(page.title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(page.description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (page == OnboardingPage.Permissions) {
                Spacer(modifier = Modifier.height(32.dp))
                PermissionSection(state, onUpdatePermissions)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PermissionSection(
    state: OnboardingUiState,
    onUpdatePermissions: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        onUpdatePermissions()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PermissionItem(
            title = stringResource(R.string.onboarding_permission_notifications),
            description = stringResource(R.string.onboarding_permission_notifications_desc),
            icon = R.drawable.round_update_24,
            isGranted = state.notificationPermissionGranted,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )

        PermissionItem(
            title = stringResource(R.string.onboarding_permission_microphone),
            description = stringResource(R.string.onboarding_permission_microphone_desc),
            icon = R.drawable.round_keyboard_voice_24,
            isGranted = state.microphonePermissionGranted,
            onRequest = {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionItem(
                title = stringResource(R.string.onboarding_permission_alarms),
                description = stringResource(R.string.onboarding_permission_alarms_desc),
                icon = R.drawable.round_access_alarm_24,
                isGranted = state.exactAlarmPermissionGranted,
                onRequest = {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    icon: Int,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = if (isGranted) 0.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isGranted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isGranted) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            AnimatedContent(
                targetState = isGranted,
                transitionSpec = {
                    (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                },
                label = "PermissionStatusAnimation"
            ) { granted ->
                if (granted) {
                    Icon(
                        painter = painterResource(R.drawable.round_check_24),
                        contentDescription = "Granted",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Button(
                        onClick = onRequest,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.height(36.dp),
                        shape = CircleShape
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_permission_request),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    currentPage: Int,
    pageCount: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = currentPage < pageCount - 1,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TextButton(onClick = onSkip) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        if (currentPage == pageCount - 1) {
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val isSelected = currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "indicator_width"
                )
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(16.dp),
            colors = if (currentPage == pageCount - 1) 
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            else 
                ButtonDefaults.buttonColors()
        ) {
            Text(
                if (currentPage < pageCount - 1) 
                    stringResource(R.string.onboarding_next) 
                else 
                    stringResource(R.string.onboarding_get_started)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    BacktalkTheme {
        OnboardingScreenContent(
            state = OnboardingUiState(),
            onUpdatePermissions = {},
            onFinished = {}
        )
    }
}
