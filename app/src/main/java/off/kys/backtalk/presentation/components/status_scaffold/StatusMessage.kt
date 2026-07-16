package off.kys.backtalk.presentation.components.status_scaffold

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * NOTE: your original file already calls `message.asString()`, implying
 * StatusMessage exists somewhere in your codebase already. If so, DELETE
 * this file and just make sure your existing type has a `Text(String)`
 * (or equivalent) variant — the extension overloads in StatusController.kt
 * construct `StatusMessage.Text(...)` for the plain-string convenience calls.
 *
 * If you don't have one yet, this minimal version covers both plain
 * strings and string resources with args.
 */
sealed interface StatusMessage {

    data class Text(val value: String) : StatusMessage

    data class Res(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : StatusMessage

    @Composable
    fun asString(): String = when (this) {
        is Text -> value
        is Res -> stringResource(resId, *args.toTypedArray())
    }
}

fun Int.toStatusMessageRes(): StatusMessage.Res = StatusMessage.Res(this)