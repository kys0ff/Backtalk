package off.kys.backtalk

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import off.kys.backtalk.ui.theme.BacktalkTheme

class MainActivity : BaseBiometricActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BacktalkTheme {
                Navigator(MessagingScreen()) {
                    if (isReady) {
                        CurrentScreen()
                    } else {
                        LockedView()
                    }
                }
            }
        }
    }

    @Composable
    private fun LockedView(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.the_app_is_locked),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}