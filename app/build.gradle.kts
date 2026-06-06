import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.InputStream

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
                val logProcess = ProcessBuilder("git", "log", "$targetTag..HEAD", "--oneline").start()
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

val generateChangelogTask = tasks.register<GenerateChangelogTask>("generateChangelog") {
    currentVersion.set(appVersion)
    outputDir.set(layout.buildDirectory.dir("generated/changelog"))
    outputs.upToDateWhen { false }
}

androidComponents {
    onVariants { variant ->
        val isFDroid = variant.productFlavors.any { it.second == "fdroid" }

        if (!isFDroid) {
            variant.sources.assets?.addGeneratedSourceDirectory(
                generateChangelogTask,
                GenerateChangelogTask::outputDir
            )
        }
    }
}