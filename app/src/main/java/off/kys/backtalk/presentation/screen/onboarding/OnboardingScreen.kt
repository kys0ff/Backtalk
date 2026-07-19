package off.kys.backtalk.presentation.screen.onboarding

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import off.kys.backtalk.presentation.event.OnboardingUiEvent
import off.kys.backtalk.presentation.screen.messages.LocalAudioPlayer
import off.kys.backtalk.presentation.screen.messages.MessagesScreen
import off.kys.backtalk.presentation.screen.onboarding.components.OnboardingScreenContent
import off.kys.backtalk.presentation.state.onboarding.OnboardingUiState
import off.kys.backtalk.presentation.theme.BacktalkTheme
import off.kys.backtalk.presentation.viewmodel.OnboardingViewModel
import off.kys.backtalk.util.AudioPlayer
import org.koin.compose.viewmodel.koinViewModel

class OnboardingScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<OnboardingViewModel>()
        val state by viewModel.state.collectAsState()
        val scope = rememberCoroutineScope()
        val pagerState = rememberPagerState { OnboardingPage.entries.size }

        LaunchedEffect(Unit) {
            viewModel.onEvent(OnboardingUiEvent.UpdatePermissions)
        }

        OnboardingScreenContent(
            pagerState = pagerState,
            state = state,
            onNext = {
                if (pagerState.currentPage < pagerState.pageCount - 1) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    viewModel.onEvent(OnboardingUiEvent.CompleteOnboarding)
                    navigator.replaceAll(MessagesScreen())
                }
            },
            onSkip = {
                viewModel.onEvent(OnboardingUiEvent.CompleteOnboarding)
                navigator.replaceAll(MessagesScreen())
            },
            onUpdatePermissions = { viewModel.onEvent(OnboardingUiEvent.UpdatePermissions) }
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:parent=pixel_5,navigation=buttons",
    showSystemUi = true
)
@Composable
private fun OnboardingScreenPreview() {
    val pagerState = rememberPagerState { OnboardingPage.entries.size }
    val audioPlayer = remember { AudioPlayer() }
    BacktalkTheme {
        CompositionLocalProvider(LocalAudioPlayer provides audioPlayer) {
            OnboardingScreenContent(
                pagerState = pagerState,
                state = OnboardingUiState(),
                onNext = {},
                onSkip = {},
                onUpdatePermissions = {}
            )
        }
    }
}
