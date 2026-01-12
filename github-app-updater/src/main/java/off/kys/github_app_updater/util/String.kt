package off.kys.github_app_updater.util

/**
 * Extension functions for String handling.
 *
 * @param this The string to check.
 * @return True if the string is not null or blank, false otherwise.
 */
internal fun String?.isNotNullOrBlank(): Boolean = !isNullOrBlank()

/**
 * Extension functions for String handling.
 *
 * @param this The string to normalize.
 * @return The normalized string.
 */
internal fun String.normalize(): String =
    removePrefix("v").trim()

/**
 * Normalizes a tag string.
 *
 * @param this The tag string to normalize.
 * @return The normalized tag string.
 */
internal fun String.normalizeTag(): String =
    if (startsWith("v")) this else "v$this"
