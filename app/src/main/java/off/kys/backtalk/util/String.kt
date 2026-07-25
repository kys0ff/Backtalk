@file:Suppress("NOTHING_TO_INLINE", "SameReturnValue")

package off.kys.backtalk.util

/**
 * Returns an empty string.
 *
 * This is a utility function to provide a more descriptive way of creating an empty string
 * than using `""`.
 *
 * @return An empty string ("").
 */
inline fun emptyString(): String = ""

/**
 * Capitalizes the first character of this string.
 *
 * If the string is empty, an empty string is returned. Otherwise, returns a copy of this string
 * with the first character converted to uppercase using the default locale.
 *
 * @return The capitalized string.
 */
inline fun String.capitalize(): String = this.replaceFirstChar { it.uppercase() }

/**
 * Extracts the first URL found within the receiver string.
 *
 * @return The first detected link as a [String], or `null` if no link is found.
 */
fun String.getFirstLinkOrNull(): String? {
    val mLink = Regex("""\[([^]]+)]\(([^)]+)\)""").find(this)
    val nLink = Regex("""(https?://[^\s)\]]+)""").find(this)

    return when {
        mLink != null && nLink != null -> {
            if (mLink.range.first < nLink.range.first) mLink.groupValues[2] else nLink.value
        }

        mLink != null -> mLink.groupValues[2]
        nLink != null -> nLink.value
        else -> null
    }
}

/**
 * Escapes Markdown special characters by prepending a backslash.
 */
fun String.escapeMarkdown(): String {
    val specialChars = setOf('*', '_', '~', '`', '[', ']', '(', ')', '#', '+', '-', '.', '!', '\\', '@')
    val builder = StringBuilder()
    for (char in this) {
        if (char in specialChars) {
            builder.append('\\')
        }
        builder.append(char)
    }
    return builder.toString()
}

/**
 * Unescapes Markdown special characters by removing the preceding backslash.
 */
fun String.unescapeMarkdown(): String {
    val specialChars = setOf('*', '_', '~', '`', '[', ']', '(', ')', '#', '+', '-', '.', '!', '\\', '@')
    val builder = StringBuilder()
    var i = 0
    while (i < this.length) {
        val char = this[i]
        if (char == '\\' && i + 1 < this.length && this[i + 1] in specialChars) {
            builder.append(this[i + 1])
            i += 2
        } else {
            builder.append(char)
            i++
        }
    }
    return builder.toString()
}
