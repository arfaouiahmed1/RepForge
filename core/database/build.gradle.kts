import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}
android {
    namespace = "com.repforge.core.database"
    compileSdk = 36
    defaultConfig { minSdk = 28 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    // Robolectric unit tests (Room in-memory DB + MigrationTestHelper) need merged resources/assets.
    testOptions { unitTests { isIncludeAndroidResources = true } }
    // NOTE: schema JSONs for MigrationTestHelper are provided as plain files under
    // src/test/assets/schemas/ (copied from ./schemas) because AGP 9.3.1 + Gradle 9.5
    // throws a decorated-cast ClassCastException on sourceSets.getByName("test"){} here.
}
dependencies {
    api(project(":core:model"))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

// Export Room schema JSONs for migration verification (todo 3)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Blocking migration-verification gate: the exported schema JSON for the CURRENT
// @Database version must exist and be committed. Any entity change without a
// version bump + fresh export fails this task (wired as required in pr.yml).
val verifyRoomMigrations by tasks.registering {
    group = "verification"
    description = "Fails if the exported Room schema JSON for the current @Database version is missing"
    doLast {
        val source = file("src/main/kotlin/com/repforge/core/database/RepForgeDatabase.kt")
        val declared = Regex("""version\s*=\s*(\d+)""").find(source.readText())
            ?.groupValues?.get(1)?.toIntOrNull()
            ?: throw GradleException("Cannot parse @Database version from RepForgeDatabase.kt")
        val schemaDir = file("schemas/com.repforge.core.database.RepForgeDatabase")
        val schema = File(schemaDir, "$declared.json")
        if (!schema.exists()) {
            throw GradleException(
                "Missing exported Room schema for version $declared at ${schema.relativeTo(rootDir)}. " +
                    "Bump the @Database version when entities change and commit the regenerated JSON."
            )
        }
        println("verifyRoomMigrations: schema v$declared OK (${schema.length()} bytes)")
    }
}