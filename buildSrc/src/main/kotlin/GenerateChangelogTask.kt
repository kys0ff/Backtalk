import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStream

abstract class GenerateChangelogTask : DefaultTask() {
    @get:Input
    abstract val currentVersion: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        outputDir.convention(project.layout.projectDirectory.dir("src/main/assets"))
        outputs.upToDateWhen { false }

        val taskRequests = project.gradle.startParameter.taskNames
        val isFDroidTargeted = taskRequests.any { it.contains("fdroid", ignoreCase = true) }

        if (isFDroidTargeted) {
            enabled = false
        }
    }

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile
        if (!dir.exists()) dir.mkdirs()
        val changelogFile = File(dir, "changelog.txt")

        val targetTag = "v${currentVersion.get()}"

        val logText = runCatching {
            ProcessBuilder("git", "fetch", "--tags").start().waitFor()

            val checkTag = ProcessBuilder("git", "rev-parse", "--verify", targetTag).start()
            if (checkTag.waitFor() == 0) {
                val logProcess =
                    ProcessBuilder("git", "log", "$targetTag..HEAD", "--oneline").start()
                val commits = logProcess.inputStream.boostedReadText().trim()
                logProcess.waitFor()

                commits.ifEmpty { "No new commits since $targetTag" }
            } else {
                "Version tag $targetTag not found. No changelog available."
            }
        }.getOrElse { "Error generating changelog: ${it.message}" }

        changelogFile.writeText(logText)
    }

    private fun InputStream.boostedReadText(): String = bufferedReader().use { it.readText() }
}
