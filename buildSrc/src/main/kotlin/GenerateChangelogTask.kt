import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

abstract class GenerateChangelogTask : DefaultTask() {

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

        val logText = runCatching {
            runCommand("git", "fetch", "--tags")
            val latestTag = runCommand("git", "describe", "--tags", "--abbrev=0").trim()

            if (latestTag.isNotEmpty()) {
                val commits = runCommand("git", "log", "$latestTag..HEAD", "--oneline").trim()
                commits.ifEmpty { "No new commits since $latestTag" }
            } else {
                "No git tags found. No changelog available."
            }
        }.getOrElse { "Error generating changelog: ${it.message}" }

        changelogFile.writeText(logText)
    }

    private fun runCommand(vararg command: String): String {
        val process = ProcessBuilder(*command)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        val result = process.inputStream.boostedReadText()
        val error = process.errorStream.boostedReadText()

        val exited = process.waitFor(5, TimeUnit.SECONDS)

        if (!exited) {
            process.destroyForcibly()
            throw RuntimeException("Command timed out: ${command.joinToString(" ")}")
        }

        if (process.exitValue() != 0) {
            throw RuntimeException(error.ifEmpty { "Command failed with exit code ${process.exitValue()}" })
        }

        return result
    }

    private fun InputStream.boostedReadText(): String = bufferedReader().use { it.readText() }
}