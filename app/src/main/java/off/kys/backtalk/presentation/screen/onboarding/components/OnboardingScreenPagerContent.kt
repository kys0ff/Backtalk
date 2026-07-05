package off.kys.backtalk.presentation.screen.onboarding.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import off.kys.backtalk.presentation.screen.onboarding.OnboardingPage
import off.kys.backtalk.presentation.state.onboarding.OnboardingUiState
import kotlin.math.absoluteValue

@Composable
fun OnboardingScreenPagerContent(
    pagerState: PagerState,
    state: OnboardingUiState,
    onUpdatePermissions: () -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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