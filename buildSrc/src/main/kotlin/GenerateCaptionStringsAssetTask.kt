import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

abstract class GenerateCaptionStringsAssetTask : DefaultTask() {
    @get:Input
    abstract val targetStringNames: ListProperty<String>

    @get:InputDirectory
    abstract val resDir: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        targetStringNames.convention(
            listOf(
                "chat_media_image",
                "chat_media_general",
                "chat_media_voice"
            )
        )
        resDir.convention(project.layout.projectDirectory.dir("src/main/res"))
        outputFile.convention(project.layout.projectDirectory.file("src/main/assets/caption_strings.txt"))

        val taskRequests = project.gradle.startParameter.taskNames
        val isFDroidTargeted = taskRequests.any { it.contains("fdroid", ignoreCase = true) }

        if (isFDroidTargeted) {
            enabled = false
        }
    }

    @TaskAction
    fun generate() {
        val names = targetStringNames.get().toSet()
        val resourcesDir = resDir.get().asFile
        val outFile = outputFile.get().asFile

        if (!outFile.parentFile.exists()) {
            outFile.parentFile.mkdirs()
        }

        val extractedValues = mutableSetOf<String>()
        val dbFactory = DocumentBuilderFactory.newInstance()

        dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)

        val valueDirs = resourcesDir.listFiles { file ->
            file.isDirectory && file.name.startsWith("values")
        }

        valueDirs?.forEach { valuesDir ->
            val stringsFile = File(valuesDir, "strings.xml")
            if (stringsFile.exists()) {
                try {
                    val dBuilder = dbFactory.newDocumentBuilder()
                    val doc = dBuilder.parse(stringsFile)
                    doc.documentElement.normalize()

                    val nList = doc.getElementsByTagName("string")
                    for (i in 0 until nList.length) {
                        val node = nList.item(i)
                        if (node.nodeType == Node.ELEMENT_NODE) {
                            val element = node as Element
                            val nameAttr = element.getAttribute("name")

                            if (names.contains(nameAttr)) {
                                val textValue = element.textContent.trim()
                                if (textValue.isNotEmpty()) {
                                    extractedValues.add(textValue)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error parsing: ${stringsFile.absolutePath}", e)
                }
            }
        }

        outFile.writeText(extractedValues.joinToString("\n"))
        logger.lifecycle("Successfully generated ${outFile.name} with ${extractedValues.size} items.")
    }
}
