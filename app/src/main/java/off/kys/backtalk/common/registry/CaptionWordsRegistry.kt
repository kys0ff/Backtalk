package off.kys.backtalk.common.registry

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class CaptionWordsRegistry(
    private val context: Context
) {
    private var cachedWords: Set<String>? = null

    init {
        runCatching {
            context.assets.open("caption_strings.txt").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    cachedWords = reader.lineSequence()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .toSet()
                }
            }
        }.onFailure {
            it.printStackTrace()
            cachedWords = emptySet()
        }
    }

    fun isRestricted(input: String): Boolean = cachedWords?.contains(input.trim()) ?: false
}
