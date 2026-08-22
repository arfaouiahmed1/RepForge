plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}
android {
    namespace = "com.repforge.core.threed"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}
dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    val bom = platform(libs.androidx.compose.bom)
    implementation(bom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    // 3D — Filament disabled for demo to avoid Kotlin 2.4 metadata mismatch (1.74 needs Kotlin 2.4). Placeholder 2D chips work.
    // When you bump to Kotlin 2.4 + KSP 2.4, uncomment:
    // implementation("io.github.sceneview:sceneview:4.30.0")
    // implementation("com.google.android.filament:filament-android:1.74.0")
    // implementation("com.google.android.filament:filament-utils-android:1.74.0")
    // implementation("com.google.android.filament:gltfio-android:1.74.0")
    implementation(libs.androidx.core.ktx)
}

