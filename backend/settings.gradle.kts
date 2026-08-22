// Standalone JVM build — intentionally EXCLUDED from the Android settings.gradle.kts.
// Build from repo root:  ./gradlew -p backend build
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "repforge-backend"
