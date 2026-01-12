package off.kys.github_app_updater.common

/**
 * Specifies the source for the changelog.
 */
enum class ChangelogSource {
    /**
     * Use the latest release notes/body.
     */
    RELEASE_BODY,

    /**
     * Use commits between current version and latest release.
     */
    COMMITS
}
