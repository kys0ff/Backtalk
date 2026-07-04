import off.kys.backtalk.build.GenerateCaptionStringsAssetTask
import off.kys.backtalk.build.GenerateChangelogAssetTask
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
        targetSdk = 37
        versionCode = 38
        versionName = "0.3.8"
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
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
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
    implementation(libs.voyager.navigator)
    implementation(libs.voyager.koin)
    implementation(libs.voyager.transitions)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.exifinterface)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.concurrent.futures)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.coil.gif)
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

val generateCaptionStringsAssetTask =
    tasks.register<GenerateCaptionStringsAssetTask>("generateCaptionStringsAsset") {
        group = "build"
        description = "Generates the caption strings asset file."
    }

val generateChangelogAssetTask =
    tasks.register<GenerateChangelogAssetTask>("generateChangelogAsset") {
        group = "build"
        description = "Generates the changelog asset file."
    }

tasks.matching { it.name.startsWith("preBuild") }.configureEach {
    dependsOn(generateChangelogAssetTask)
    dependsOn(generateCaptionStringsAssetTask)
}