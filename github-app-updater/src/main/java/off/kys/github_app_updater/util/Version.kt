package off.kys.github_app_updater.util

/**
 * Represents a version with optional pre-release information.
 *
 * @param major The major version number.
 * @param minor The minor version number.
 * @param patch The patch version number.
 * @param preRelease The pre-release information, if any.
 */
internal data class Version(
    private val major: Int,
    private val minor: Int,
    private val patch: Int,
    private val preRelease: String? = null // e.g., "alpha", "beta", "rc1"
) : Comparable<Version> {

    /**
     * Constructs a Version object from a string representation.
     *
     * @param version The string representation of the version.
     */
    constructor(version: String) : this(
        major = version.split(".").getOrNull(0)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0,
        minor = version.split(".").getOrNull(1)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0,
        patch = version.split(".").getOrNull(2)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0,
        preRelease = version.split(".").getOrNull(2)?.let {
            it.dropWhile { c -> c.isDigit() }.takeIf { s -> s.isNotEmpty() }
        }
    )

    /**
     * Compares this version to another version.
     *
     * @param other The version to compare with.
     * @return A negative value if this version is less than the other version,
     *         zero if they are equal, and a positive value if this version is
     *         greater than the other version.
     */
    override fun compareTo(other: Version): Int {
        if (major != other.major) return major - other.major
        if (minor != other.minor) return minor - other.minor
        if (patch != other.patch) return patch - other.patch

        // Handle pre-release comparison: null > alpha/beta/rc
        return comparePreRelease(preRelease, other.preRelease)
    }

    /**
     * Compares two pre-release strings.
     *
     * @param a The first pre-release string.
     * @param b The second pre-release string.
     * @return A negative value if a is less than b, zero if they are equal,
     *         and a positive value if a is greater than b
     */
    private fun comparePreRelease(a: String?, b: String?): Int {
        if (a == b) return 0
        if (a == null) return 1       // release > pre-release
        if (b == null) return -1      // pre-release < release

        // Define order: alpha < beta < rc < anything else
        val order = listOf("alpha", "beta", "rc")
        val aIndex = order.indexOfFirst { a.startsWith(it, ignoreCase = true) }.let { if (it == -1) Int.MAX_VALUE else it }
        val bIndex = order.indexOfFirst { b.startsWith(it, ignoreCase = true) }.let { if (it == -1) Int.MAX_VALUE else it }

        return aIndex - bIndex
    }

    /**
     * Returns a string representation of the version.
     *
     * @return The string representation of the version.
     */
    override fun toString(): String =
        "$major.$minor.$patch${preRelease?.let { "-$it" } ?: ""}"
}