package off.kys.github_app_updater.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import off.kys.github_app_updater.BuildConfig
import off.kys.github_app_updater.common.GithubConstants
import off.kys.github_app_updater.model.github.GitHubCompare
import off.kys.github_app_updater.model.github.GitHubRelease
import off.kys.github_app_updater.util.JsonLoggingInterceptor
import off.kys.github_app_updater.util.isNotNullOrBlank
import off.kys.github_app_updater.util.runIf
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for the GitHub API.
 */
internal interface GitHubApi {

    /**
     * Retrieves the latest release information for a repository.
     *
     * @param repo The repository name.
     * @return The latest release information
     */
    @GET("repos/{repo}/releases/latest")
    suspend fun latestRelease(
        @Path("repo", encoded = true) repo: String
    ): GitHubRelease

    /**
     * Retrieves the commit history between two versions.
     *
     * @param repo The repository name.
     * @param base The base version.
     * @param head The head version.
     * @param perPage The number of commits per page.
     * @return The commit history between the versions.
     */
    @GET("repos/{repo}/compare/{base}...{head}")
    suspend fun compare(
        @Path(value = "repo", encoded = true) repo: String,
        @Path("base") base: String,
        @Path("head") head: String,
        @Query("per_page") perPage: Int = 100
    ): GitHubCompare

    companion object {
        /**
         * Creates a new instance of the GitHubApi interface.
         *
         * @param token The GitHub personal access token.
         * @return The created GitHubApi instance.
         */
        fun create(token: String?): GitHubApi {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Accept", "application/vnd.github+json")
                        .addHeader("X-GitHub-Api-Version", GithubConstants.GITHUB_API_VERSION)
                        .runIf(token.isNotNullOrBlank()) {
                            addHeader("Authorization", "Bearer $token")
                        }
                        .build()
                    chain.proceed(request)
                }
                .runIf(BuildConfig.DEBUG) {
                    addInterceptor(JsonLoggingInterceptor())
                }
                .build()

            return Retrofit.Builder()
                .baseUrl(GithubConstants.GITHUB_API_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(GitHubApi::class.java)
        }
    }
}