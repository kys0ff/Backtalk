package off.kys.backtalk.data.util

import off.kys.backtalk.domain.model.LibraryInfo

/**
 * Provides a list of libraries used in the Backtalk application.
 *
 * This object centralizes library metadata for display in the license screen,
 * ensuring easy updates and separation of concerns.
 */
object LibraryProvider {

    /**
     * A list of libraries and their respective versions and licenses, sorted by name.
     */
    val libraries = listOf(
        LibraryInfo("Activity Compose", "1.13.0", "Apache License 2.0"),
        LibraryInfo("Biometric", "1.4.0-alpha07", "Apache License 2.0"),
        LibraryInfo("CameraX", "1.6.1", "Apache License 2.0"),
        LibraryInfo("Coil", "2.7.0", "Apache License 2.0"),
        LibraryInfo("Concurrent Futures", "1.3.0", "Apache License 2.0"),
        LibraryInfo("Core KTX", "1.19.0", "Apache License 2.0"),
        LibraryInfo("Desugar JDK Libs", "2.1.5", "Apache License 2.0"),
        LibraryInfo("DocumentFile", "1.1.0", "Apache License 2.0"),
        LibraryInfo("Espresso Core", "3.7.0", "Apache License 2.0"),
        LibraryInfo("Exifinterface", "1.4.2", "Apache License 2.0"),
        LibraryInfo("Gau", "1.0.0", "MIT License"),
        LibraryInfo("Jetpack Compose BOM", "2026.06.01", "Apache License 2.0"),
        LibraryInfo("JUnit", "4.13.2", "Eclipse Public License 1.0"),
        LibraryInfo("JUnit Extension", "1.3.0", "Apache License 2.0"),
        LibraryInfo("Koin", "4.2.2", "Apache License 2.0"),
        LibraryInfo("Kotlin Coroutines Test", "1.11.0", "Apache License 2.0"),
        LibraryInfo("Kotlinx Collections Immutable", "0.5.1", "Apache License 2.0"),
        LibraryInfo("Kotlinx Serialization", "1.11.0", "Apache License 2.0"),
        LibraryInfo("Lifecycle Process", "2.11.0", "Apache License 2.0"),
        LibraryInfo("Lifecycle Runtime Compose", "2.11.0", "Apache License 2.0"),
        LibraryInfo("Lifecycle Runtime KTX", "2.11.0", "Apache License 2.0"),
        LibraryInfo("MockK", "1.14.11", "Apache License 2.0"),
        LibraryInfo("Mockito", "5.23.0", "MIT License"),
        LibraryInfo("Mockito-Kotlin", "6.3.0", "MIT License"),
        LibraryInfo("Room", "2.8.4", "Apache License 2.0"),
        LibraryInfo("Splashscreen", "1.2.0", "Apache License 2.0"),
        LibraryInfo("Voyager", "1.1.0-beta03", "MIT License"),
        LibraryInfo("WorkManager", "2.11.2", "Apache License 2.0")
    )

}