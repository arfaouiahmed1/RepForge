plugins {
    kotlin("jvm") version "2.4.10"
    application
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.10"
}

group = "com.repforge.backend"
version = "0.1.0"

application { mainClass.set("com.repforge.backend.ApplicationKt") }

dependencies {
    implementation("io.ktor:ktor-server-core:3.1.1")
    implementation("io.ktor:ktor-server-netty:3.1.1")
    implementation("io.ktor:ktor-server-content-negotiation:3.1.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
    implementation("io.ktor:ktor-server-auth:3.1.1")
    implementation("ch.qos.logback:logback-classic:1.5.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    // Firebase Admin — ID token verification (Auth) + later Firestore/Play Developer API.
    // BACKEND JVM ONLY: never add this to any Android module (APK bloat + secret risk).
    implementation("com.google.firebase:firebase-admin:9.4.3")
    testImplementation("io.ktor:ktor-server-test-host:3.1.1")
    testImplementation(kotlin("test"))
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

// JAVA_HOME may be a newer JBR (e.g. JDK 25); pin bytecode to the 17 target for BOTH compilers.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}
