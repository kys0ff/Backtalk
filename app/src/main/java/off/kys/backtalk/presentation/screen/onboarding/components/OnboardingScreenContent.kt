package off.kys.backtalk.presentation.screen.onboarding.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import off.kys.backtalk.presentation.state.onboarding.OnboardingUiState

@Composable
fun OnboardingScreenContent(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    state: OnboardingUiState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onUpdatePermissions: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            OnboardingBottomBar(
                currentPage = pagerState.currentPage,
                pageCount = pagerState.pageCount,
                onNext = onNext,
                onSkip = onSkip
            )
        }
    ) { paddingValues ->
        OnboardingScreenPagerContent(
            modifier = Modifier.fillMaxSize(),
            pagerState = pagerState,
            state = state,
            onUpdatePermissions = onUpdatePermissions,
            paddingValues = paddingValues
        )
    }
}