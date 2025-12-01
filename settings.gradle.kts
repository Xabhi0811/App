pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        // ✅ Android Gradle Plugin
        id("com.android.application") version "8.4.2"
        id("com.android.library") version "8.4.2"

        // ✅ Kotlin 1.9 (Stable)
        id("org.jetbrains.kotlin.android") version "1.9.23"

        // ✅ KSP for Kotlin 1.9
        id("com.google.devtools.ksp") version "1.9.23-1.0.20"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SimpleBudget"
include(":app")
