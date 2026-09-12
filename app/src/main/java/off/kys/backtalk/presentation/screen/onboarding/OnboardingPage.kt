package off.kys.backtalk.presentation.screen.onboarding

import androidx.annotation.StringRes
import off.kys.backtalk.R

enum class OnboardingPage(
    @get:StringRes val title: Int,
    @get:StringRes val description: Int,
) {
    Welcome(
        title = R.string.onboarding_welcome_title,
        description = R.string.onboarding_welcome_desc,
    ),
    Messaging(
        title = R.string.onboarding_messaging_title,
        description = R.string.onboarding_messaging_desc
    ),
    Voice(
        title = R.string.onboarding_voice_title,
        description = R.string.onboarding_voice_desc
    ),
    Threads(
        title = R.string.onboarding_threads_title,
        description = R.string.onboarding_threads_desc
    ),
    Rules(
        title = R.string.onboarding_rules_title,
        description = R.string.onboarding_rules_desc
    ),
    ReminderInteractive(
        title = R.string.onboarding_reminder_interactive_title,
        description = R.string.onboarding_reminder_interactive_desc
    ),
    SyncBackup(
        title = R.string.onboarding_sync_title,
        description = R.string.onboarding_sync_desc
    ),
    Security(
        title = R.string.onboarding_security_title,
        description = R.string.onboarding_security_desc
    ),
    Permissions(
        title = R.string.onboarding_permissions_title,
        description = R.string.onboarding_permissions_desc
    )
}
