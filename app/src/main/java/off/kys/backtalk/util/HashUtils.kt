package off.kys.backtalk.util

import java.io.InputStream
import java.security.MessageDigest

/**
 * Utility for calculating hashes.
 */
object HashUtils {
    /**
     * Calculates the SHA-256 hash of the given [inputStream].
     * Closes the stream after reading.
     */
    fun calculateSha256(inputStream: InputStream): String {
        return inputStream.use { input ->
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var bytesRead = input.read(buffer)
            while (bytesRead != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = input.read(buffer)
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
