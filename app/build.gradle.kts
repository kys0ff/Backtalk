import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "off.kys.backtalk"
    compileSdk = 37

    defaultConfig {
        applicationId = "off.kys.backtalk"
        minSdk = 23
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 32
        versionName = "0.3.2"
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

val generateChangelogTask = tasks.register("generateChangelog") {
    val changelogFile = file("src/main/assets/changelog.txt")
    outputs.file(changelogFile)
    outputs.upToDateWhen { false }

    doLast {
        val assetDir = file("src/main/assets")
        if (!assetDir.exists()) assetDir.mkdirs()

        val logText = runCatching {
            val tagProcess = ProcessBuilder("git", "describe", "--tags", "--abbrev=0").start()
            tagProcess.waitFor()
            val lastTag = tagProcess.inputStream.bufferedReader().readText().trim()

            if (lastTag.isNotEmpty()) {
                val logProcess = ProcessBuilder("git", "log", "$lastTag..HEAD", "--oneline").start()
                logProcess.waitFor()
                logProcess.inputStream.bufferedReader().readText()
            } else {
                val logProcess = ProcessBuilder("git", "log", "-n", "5", "--oneline").start()
                logProcess.waitFor()
                logProcess.inputStream.bufferedReader().readText()
            }
        }.getOrElse { "No changelog available" }

        changelogFile.writeText(logText)
    }
}

androidComponents {
    onVariants { variant ->
        val isFDroid = variant.productFlavors.any { it.second == "fdroid" }

        if (!isFDroid) {
            variant.sources.assets?.addGeneratedSourceDirectory(
                generateChangelogTask
            ) {
                val directoryProperty = project.objects.directoryProperty()
                directoryProperty.set(project.layout.projectDirectory.dir("src/main/assets"))
                directoryProperty
            }
        }
    }
}