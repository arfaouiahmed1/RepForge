plugins {
    kotlin("jvm") version "2.4.10"
    id("io.ktor.plugin") version "3.1.1"
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
    // Firebase Admin for Firestore + Google Play Developer API for billing verification
    // implementation("com.google.firebase:firebase-admin:9.3.0")
    // implementation("com.google.apis:google-api-services-androidpublisher:v3-rev20241113-2.0.0")
    testImplementation(kotlin("test"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
