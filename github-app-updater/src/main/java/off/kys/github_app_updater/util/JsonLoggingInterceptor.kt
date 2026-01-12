package off.kys.github_app_updater.util

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

/**
 * An interceptor for logging JSON responses from the GitHub API.
 */
internal class JsonLoggingInterceptor : Interceptor {

    /**
     * Intercepts the request and response to log the JSON response.
     *
     * @param chain The interceptor chain.
     * @return The response.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val responseBody = response.body
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body
            val buffer: Buffer = source.buffer.clone()

            val charset = responseBody.contentType()?.charset(Charsets.UTF_8)
                ?: Charsets.UTF_8

            val json = buffer.readString(charset)

            println(
                """
                ===== GitHub API RESPONSE =====
                URL: ${request.url}
                STATUS: ${response.code}
                BODY:
                $json
                ===============================
                """.trimIndent()
            )
        }

        return response
    }
}