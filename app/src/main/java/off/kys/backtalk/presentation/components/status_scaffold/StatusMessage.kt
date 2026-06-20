package off.kys.backtalk.presentation.components.status_scaffold

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * A wrapper class that allows passing either a hardcoded String or a String resource ID.
 */
sealed interface StatusMessage {
    data class Hardcoded(val value: String) : StatusMessage
    data class Resource(@StringRes val resId: Int, val args: List<Any> = emptyList()) :
        StatusMessage

    @Composable
    fun asString(): String = when (this) {
        is Hardcoded -> value
        is Resource -> stringResource(resId, *args.toTypedArray())
    }
}