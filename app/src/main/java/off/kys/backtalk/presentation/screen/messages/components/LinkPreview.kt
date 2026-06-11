package off.kys.backtalk.presentation.screen.messages.components

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import off.kys.backtalk.R
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.MessageDigest

private const val CONNECT_TIMEOUT_MS = 8_000
private const val READ_TIMEOUT_MS = 8_000
private const val MAX_HTML_BYTES = 512 * 1_024
private const val CACHE_DIR_NAME = "link_previews"
private const val CACHE_TTL_MS = 24L * 60 * 60 * 1_000

private val jsonConfig = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
}

@Serializable
@Immutable
data class LinkMetadata(
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val faviconUrl: String? = null,
    val siteName: String? = null,
    val domain: String? = null,
    val cachedAt: Long = System.currentTimeMillis(),
)

sealed interface LinkPreviewState {
    data object Loading : LinkPreviewState
    data class Success(val metadata: LinkMetadata) : LinkPreviewState
    data class Error(val message: String) : LinkPreviewState
}

private val memCache = mutableMapOf<String, LinkMetadata>()

private fun urlCacheKey(url: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(url.toByteArray())
        .joinToString("") { "%02x".format(it) }

private fun cacheFile(context: Context, url: String): File {
    val dir = File(context.cacheDir, CACHE_DIR_NAME).also { it.mkdirs() }
    return File(dir, "${urlCacheKey(url)}.json")
}

private fun readDiskCache(context: Context, url: String): LinkMetadata? = try {
    val file = cacheFile(context, url)
    if (!file.exists()) {
        null
    } else {
        val metadata = jsonConfig.decodeFromString<LinkMetadata>(file.readText())
        if (System.currentTimeMillis() - metadata.cachedAt > CACHE_TTL_MS) null else metadata
    }
} catch (_: Exception) {
    null
}

private fun writeDiskCache(context: Context, metadata: LinkMetadata) {
    runCatching {
        val jsonString = jsonConfig.encodeToString(metadata)
        cacheFile(context, metadata.url).writeText(jsonString)
    }
}

private fun String.decodeHtmlEntities(): String = this
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("&apos;", "'")
    .replace("&nbsp;", "\u00A0")
    .replace(Regex("&#(\\d+);")) {
        it.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: it.value
    }
    .replace(Regex("&#x([0-9a-fA-F]+);")) {
        it.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: it.value
    }

private fun resolveUrl(base: String, href: String): String? {
    val trimmed = href.trim()
    if (trimmed.isBlank() || trimmed.startsWith("data:")) return null
    return runCatching { URI(base).resolve(trimmed).toString() }.getOrNull()
}

suspend fun fetchLinkMetadata(url: String, context: Context): LinkMetadata =
    withContext(Dispatchers.IO) {
        memCache[url]?.let { return@withContext it }
        readDiskCache(context, url)?.also { memCache[url] = it }?.let { return@withContext it }

        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; LinkPreview/1.0)")
            setRequestProperty("Accept", "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8")
            instanceFollowRedirects = true
        }

        val html = try {
            connection.inputStream.bufferedReader().use { reader ->
                val sb = StringBuilder()
                val buf = CharArray(8_192)
                var totalRead = 0
                var read: Int
                while (reader.read(buf).also { read = it } != -1) {
                    sb.appendRange(buf, 0, read)
                    totalRead += read
                    if (totalRead >= MAX_HTML_BYTES) break
                }
                sb.toString()
            }
        } finally {
            connection.disconnect()
        }

        fun ogContent(prop: String): String? = listOf(
            Regex(
                """<meta[^>]+property=["']og:$prop["'][^>]+content=["']([^"']+)["']""",
                RegexOption.IGNORE_CASE
            ),
            Regex(
                """<meta[^>]+content=["']([^"']+)["'][^>]+property=["']og:$prop["']""",
                RegexOption.IGNORE_CASE
            ),
        ).firstNotNullOfOrNull { it.find(html)?.groupValues?.get(1)?.trim()?.decodeHtmlEntities() }

        fun metaName(name: String): String? = listOf(
            Regex(
                """<meta[^>]+name=["']$name["'][^>]+content=["']([^"']+)["']""",
                RegexOption.IGNORE_CASE
            ),
            Regex(
                """<meta[^>]+content=["']([^"']+)["'][^>]+name=["']$name["']""",
                RegexOption.IGNORE_CASE
            ),
        ).firstNotNullOfOrNull { it.find(html)?.groupValues?.get(1)?.trim()?.decodeHtmlEntities() }

        data class LdSnippet(val name: String?, val description: String?, val image: String?)

        fun JsonObject.optString(key: String): String? =
            this[key]?.jsonPrimitive?.content?.ifBlank { null }

        fun JsonObject.imageUrl(): String? {
            return when (val img = this["image"]) {
                is JsonObject -> img.optString("url")
                is JsonArray -> when (val first = img.firstOrNull()) {
                    is JsonObject -> first.optString("url")
                    else -> first?.jsonPrimitive?.content?.ifBlank { null }
                }

                else -> img?.jsonPrimitive?.content?.ifBlank { null }
            }
        }

        fun JsonObject.toLdSnippet() = LdSnippet(
            name = optString("name") ?: optString("headline"),
            description = optString("description"),
            image = imageUrl(),
        )

        val jsonLd: LdSnippet = Regex(
            """<script[^>]+type=["']application/ld\+json["'][^>]*>(.*?)</script>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).findAll(html).firstNotNullOfOrNull { m ->
            runCatching {
                val element = jsonConfig.parseToJsonElement(m.groupValues[1].trim())
                if (element is JsonObject) {
                    val candidates = if (element.containsKey("@graph")) {
                        element["@graph"]?.jsonArray?.mapNotNull { it as? JsonObject }
                            ?: emptyList()
                    } else {
                        listOf(element)
                    }
                    candidates
                        .map { it.toLdSnippet() }
                        .firstOrNull { it.name != null || it.image != null }
                } else null
            }.getOrNull()
        } ?: LdSnippet(null, null, null)

        val faviconHref: String? = listOf(
            Regex(
                """<link[^>]+rel=["']shortcut icon["'][^>]+href=["']([^"']+)["']""",
                RegexOption.IGNORE_CASE
            ),
            Regex(
                """<link[^>]+href=["']([^"']+)["'][^>]+rel=["']shortcut icon["']""",
                RegexOption.IGNORE_CASE
            ),
            Regex(
                """<link[^>]+rel=["']icon["'][^>]+href=["']([^"']+)["']""",
                RegexOption.IGNORE_CASE
            ),
            Regex(
                """<link[^>]+href=["']([^"']+)["'][^>]+rel=["']icon["']""",
                RegexOption.IGNORE_CASE
            ),
            Regex(
                """<link[^>]+rel=["']apple-touch-icon["'][^>]+href=["']([^"']+)["']""",
                RegexOption.IGNORE_CASE
            ),
            Regex(
                """<link[^>]+href=["']([^"']+)["'][^>]+rel=["']apple-touch-icon["']""",
                RegexOption.IGNORE_CASE
            ),
        ).firstNotNullOfOrNull { it.find(html)?.groupValues?.get(1)?.trim() }

        val title = ogContent("title")
            ?: metaName("twitter:title")
            ?: jsonLd.name
            ?: Regex("<title[^>]*>([^<]+)</title>", RegexOption.IGNORE_CASE)
                .find(html)?.groupValues?.get(1)?.trim()?.decodeHtmlEntities()

        val description = ogContent("description")
            ?: metaName("twitter:description")
            ?: metaName("description")
            ?: jsonLd.description

        val rawImageUrl = ogContent("image:secure_url")
            ?: ogContent("image")
            ?: metaName("twitter:image")
            ?: metaName("twitter:image:src")
            ?: jsonLd.image

        val domain = runCatching { URI(url).host?.removePrefix("www.") }.getOrNull()
        val imageUrl = rawImageUrl?.let { resolveUrl(url, it) }
        val faviconUrl =
            faviconHref?.let { resolveUrl(url, it) } ?: domain?.let { "https://$it/favicon.ico" }

        val metadata = LinkMetadata(
            url = url,
            title = title,
            description = description,
            imageUrl = imageUrl,
            faviconUrl = faviconUrl,
            siteName = ogContent("site_name") ?: domain,
            domain = domain,
        )

        memCache[url] = metadata
        writeDiskCache(context, metadata)
        metadata
    }

@Composable
fun LinkPreviewCard(
    url: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var state by remember(url) { mutableStateOf<LinkPreviewState>(LinkPreviewState.Loading) }
    var loadTrigger by remember(url) { mutableIntStateOf(0) }

    val handleClick by rememberUpdatedState(
        onClick ?: {
            try {
                uriHandler.openUri(url)
            } finally {

            }
        }
    )

    val previewErrorMessage = stringResource(R.string.link_preview_load_failed)
    LaunchedEffect(url, loadTrigger) {
        state = LinkPreviewState.Loading
        state = runCatching { LinkPreviewState.Success(fetchLinkMetadata(url, context)) }
            .getOrElse { e -> LinkPreviewState.Error(e.message ?: previewErrorMessage) }
    }

    ElevatedCard(
        onClick = handleClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
            label = "linkPreviewState",
        ) { currentState ->
            when (currentState) {
                is LinkPreviewState.Loading -> LinkPreviewSkeleton()
                is LinkPreviewState.Error -> LinkPreviewError(
                    url = url,
                    message = currentState.message,
                    onRetry = { loadTrigger++ },
                )

                is LinkPreviewState.Success -> LinkPreviewContent(
                    metadata = currentState.metadata,
                )
            }
        }
    }
}

@Composable
private fun LinkPreviewContent(metadata: LinkMetadata) {
    BoxWithConstraints {
        val useVerticalLayout = maxWidth >= 280.dp && metadata.imageUrl != null

        if (useVerticalLayout) {
            Column {
                AsyncImage(
                    model = metadata.imageUrl,
                    contentDescription = metadata.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )
                LinkPreviewTextContent(metadata, isCompact = false)
            }
        } else {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (metadata.imageUrl != null) {
                    AsyncImage(
                        model = metadata.imageUrl,
                        contentDescription = metadata.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(if (this@BoxWithConstraints.maxWidth < 200.dp) 56.dp else 72.dp)
                            .clip(MaterialTheme.shapes.small),
                    )
                }
                LinkPreviewTextContent(metadata, isCompact = true, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LinkPreviewTextContent(
    metadata: LinkMetadata,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(if (isCompact) 0.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 2.dp else 4.dp)
    ) {
        if (metadata.domain != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                metadata.faviconUrl?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = null,
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape),
                    )
                }
                Text(
                    text = metadata.domain,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        metadata.title?.let {
            Text(
                text = it,
                style = if (isCompact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = if (isCompact) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (!isCompact || metadata.title == null) {
            metadata.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = if (isCompact) 2 else 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LinkPreviewSkeleton() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val sweepPosition by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1_400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerSweep",
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.surfaceVariant,
        ),
        start = Offset(sweepPosition, 0f),
        end = Offset(sweepPosition + 600f, 300f),
    )

    BoxWithConstraints {
        val useVerticalLayout = maxWidth >= 280.dp

        if (useVerticalLayout) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(shimmerBrush),
                )
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(shimmerBrush)
                        )
                        SkeletonLine(widthFraction = 0.35f, height = 12.dp, brush = shimmerBrush)
                    }
                    SkeletonLine(widthFraction = 0.90f, height = 20.dp, brush = shimmerBrush)
                    SkeletonLine(widthFraction = 0.65f, height = 20.dp, brush = shimmerBrush)
                    Spacer(Modifier.height(4.dp))
                    SkeletonLine(widthFraction = 1.00f, height = 14.dp, brush = shimmerBrush)
                    SkeletonLine(widthFraction = 0.95f, height = 14.dp, brush = shimmerBrush)
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(shimmerBrush)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    SkeletonLine(0.8f, 16.dp, shimmerBrush)
                    SkeletonLine(0.5f, 12.dp, shimmerBrush)
                }
            }
        }
    }
}

@Composable
private fun LinkPreviewError(
    url: String,
    message: String = stringResource(R.string.link_preview_error_default),
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.round_link_off_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onRetry) {
            Icon(
                painter = painterResource(R.drawable.round_refresh_24),
                contentDescription = stringResource(R.string.link_preview_retry),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SkeletonLine(
    widthFraction: Float,
    height: Dp,
    brush: Brush,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraSmall,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(shape)
            .background(brush),
    )
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun PreviewSuccessWide() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            LinkPreviewCard(
                url = "https://kotlinlang.org/docs/compose.html",
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 250)
@Composable
private fun PreviewSuccessCompact() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            LinkPreviewCard(
                url = "https://kotlinlang.org/docs/compose.html",
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun PreviewLoadingWide() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ElevatedCard(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                LinkPreviewSkeleton()
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun PreviewError() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ElevatedCard(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                LinkPreviewError(
                    url = "https://example.com/broken-link",
                    message = "Connection timed out",
                    onRetry = {},
                )
            }
        }
    }
}
