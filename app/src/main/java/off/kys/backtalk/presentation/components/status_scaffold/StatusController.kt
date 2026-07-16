package off.kys.backtalk.presentation.components.status_scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * An action rendered inline in the status badge, e.g. "Retry" on an Error,
 * or "Undo" on an Info. Optional — most calls won't need it.
 */
data class StatusAction(
    val label: String,
    val onClick: () -> Unit
)

/**
 * Immutable snapshot of what the global status overlay should currently show.
 */
data class StatusUiState(
    val status: ScaffoldStatus = ScaffoldStatus.None,
    val message: StatusMessage? = null,
    val action: StatusAction? = null,
    val autoDismissMillis: Long? = null,
    // bumped on every show() call so re-showing the *same* status/message
    // (e.g. two errors in a row) still restarts the enter animation + auto-dismiss timer.
    val revision: Int = 0
)

/**
 * The single source of truth for the app-wide status banner.
 *
 * Obtain one with [rememberStatusController], provide it once near the root
 * with [ProvideStatusController], then call it from anywhere via
 * `LocalStatusController.current`.
 */
@Stable
class StatusController internal constructor() {

    var state: StatusUiState by mutableStateOf(StatusUiState())
        private set

    fun show(
        status: ScaffoldStatus,
        message: StatusMessage,
        action: StatusAction? = null,
        autoDismissMillis: Long? = defaultAutoDismissFor(status)
    ) {
        state = StatusUiState(
            status = status,
            message = message,
            action = action,
            autoDismissMillis = autoDismissMillis,
            revision = state.revision + 1
        )
    }

    /** Quiet, low-priority heads-up. Auto-dismisses by default. */
    fun info(
        message: StatusMessage,
        action: StatusAction? = null,
        autoDismissMillis: Long? = 3_500L
    ) = show(ScaffoldStatus.Info, message, action, autoDismissMillis)

    /** Needs attention but isn't blocking. Auto-dismisses a bit slower than info. */
    fun warning(
        message: StatusMessage,
        action: StatusAction? = null,
        autoDismissMillis: Long? = 5_000L
    ) = show(ScaffoldStatus.Warning, message, action, autoDismissMillis)

    /** Something failed. Stays until the user dismisses it, or you call [dismiss], unless overridden. */
    fun error(
        message: StatusMessage,
        action: StatusAction? = null,
        autoDismissMillis: Long? = null
    ) = show(ScaffoldStatus.Error, message, action, autoDismissMillis)

    /** Indicates an ongoing operation. Stays until dismissed. */
    fun loading(
        message: StatusMessage,
        action: StatusAction? = null,
        autoDismissMillis: Long? = null
    ) = show(ScaffoldStatus.Loading, message, action, autoDismissMillis)

    fun dismiss() {
        state = state.copy(status = ScaffoldStatus.None)
    }

    private fun defaultAutoDismissFor(status: ScaffoldStatus): Long? = when (status) {
        ScaffoldStatus.Info -> 3_500L
        ScaffoldStatus.Warning -> 5_000L
        ScaffoldStatus.Error -> null
        ScaffoldStatus.None -> null
        ScaffoldStatus.Loading -> null
    }
}

@Composable
fun rememberStatusController(): StatusController = remember { StatusController() }

/**
 * CompositionLocal for [StatusController]. Accessing this without wrapping
 * your tree in [ProvideStatusController] (or [GlobalStatusHost]) is a
 * programming error and fails fast on purpose.
 */
val LocalStatusController = staticCompositionLocalOf<StatusController> {
    error(
        "No StatusController found. Wrap your app root in ProvideStatusController { ... } " +
            "or GlobalStatusHost { ... } before calling LocalStatusController.current."
    )
}

// ---- Convenience overloads for plain strings, so call sites don't need to
// ---- know about StatusMessage for the common case. Adjust the StatusMessage
// ---- construction below to match your actual sealed type if it differs. ----

fun StatusController.info(text: String, action: StatusAction? = null, autoDismissMillis: Long? = 3_500L) =
    info(StatusMessage.Text(text), action, autoDismissMillis)

fun StatusController.warning(text: String, action: StatusAction? = null, autoDismissMillis: Long? = 5_000L) =
    warning(StatusMessage.Text(text), action, autoDismissMillis)

fun StatusController.error(text: String, action: StatusAction? = null, autoDismissMillis: Long? = null) =
    error(StatusMessage.Text(text), action, autoDismissMillis)

fun StatusController.loading(text: String, action: StatusAction? = null, autoDismissMillis: Long? = null) =
    loading(StatusMessage.Text(text), action, autoDismissMillis)
