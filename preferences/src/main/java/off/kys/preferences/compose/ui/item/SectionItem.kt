package off.kys.preferences.compose.ui.item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SectionItem(@StringRes titleRes: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .padding(8.dp)
            .padding(start = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview
@Composable
private fun SectionItemPreview() {
    MaterialTheme {
        Surface {
            SectionItem(titleRes = android.R.string.untitled)
        }
    }
}