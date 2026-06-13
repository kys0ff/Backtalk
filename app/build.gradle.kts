import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
}

val appVersion: String = "0.3.2"

android {
    namespace = "off.kys.backtalk"
    compileSdk = 37

    defaultConfig {
        applicationId = "off.kys.backtalk"
        minSdk = 23
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 32
        versionName = appVersion
        buildConfigField("String", "VERSION_NAME", "\"$versionName\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions.add("distribution")

    productFlavors {
        create("github") {
            dimension = "distribution"
            buildConfigField("Boolean", "IS_FDROID", "false")
        }
        create("fdroid") {
            dimension = "distribution"
            buildConfigField("Boolean", "IS_FDROID", "true")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("androidTest") {
            assets.directories.add(file("$projectDir/schemas").absolutePath)
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.bundles.voyager)
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
    implementation(libs.bundles.room)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.concurrent.futures)
    implementation(libs.bundles.camerax)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.gau)
    implementation(libs.haze)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.koin.test)

    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

abstract class GenerateCaptionStringsAssetTask : DefaultTask() {
    @get:Input
    abstract val targetStringNames: ListProperty<String>

    @get:InputDirectory
    abstract val resDir: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

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

abstract class GenerateChangelogTask : DefaultTask() {
    @get:Input
    abstract val currentVersion: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

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

val generateCaptionStringsAssetTask =
    tasks.register<GenerateCaptionStringsAssetTask>("generateCaptionStringsAsset") {
        targetStringNames.set(
            listOf(
                "chat_media_image",
                "chat_media_general",
                "chat_media_voice"
            )
        )
        resDir.set(file("$projectDir/src/main/res"))
        outputFile.set(file("$projectDir/src/main/assets/caption_strings.txt"))

        val taskRequests = gradle.startParameter.taskNames
        val isFDroidTargeted = taskRequests.any { it.contains("fdroid", ignoreCase = true) }

        if (isFDroidTargeted) {
            enabled = false
        }
    }

val generateChangelogTask = tasks.register<GenerateChangelogTask>("generateChangelog") {
    currentVersion.set(appVersion)
    outputDir.set(file("$projectDir/src/main/assets"))
    outputs.upToDateWhen { false }

    val taskRequests = gradle.startParameter.taskNames
    val isFDroidTargeted = taskRequests.any { it.contains("fdroid", ignoreCase = true) }

    if (isFDroidTargeted) {
        enabled = false
    }
}

tasks.matching { it.name.startsWith("preBuild") }.configureEach {
    dependsOn(generateChangelogTask)
    dependsOn(generateCaptionStringsAssetTask)
}