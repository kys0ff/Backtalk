package off.kys.backtalk.presentation.screen.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import off.kys.backtalk.presentation.screen.onboarding.components.OnboardingScreenContent
import off.kys.backtalk.presentation.state.OnboardingUiState
import off.kys.backtalk.presentation.theme.BacktalkTheme
import off.kys.backtalk.presentation.viewmodel.OnboardingViewModel
import org.koin.compose.viewmodel.koinViewModel

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
