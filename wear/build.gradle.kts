plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}
android {
    namespace = "com.repforge.wear"
    compileSdk = 36
    defaultConfig { applicationId = "com.repforge.wear"; minSdk = 26; targetSdk = 36; versionCode = 1; versionName = "0.1.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    buildFeatures { compose = true }
}
dependencies {
    val bom = platform(libs.androidx.compose.bom)
    implementation(bom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    // Wear Material 1.4.0 is stable; material3 1.0.0 artifact not yet on mavenCentral at that coordinate
    implementation("androidx.wear.compose:compose-material:1.4.0")
    implementation("com.google.android.gms:play-services-wearable:19.0.0")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}