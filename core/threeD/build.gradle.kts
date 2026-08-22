plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}
android {
    namespace = "com.repforge.core.threed"
    compileSdk = 36
    defaultConfig { minSdk = 28 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    buildFeatures { compose = true }
}
dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    val bom = platform(libs.androidx.compose.bom)
    implementation(bom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    // 3D stack ACTIVE (todo 23): Kotlin 2.4.10 built-in unblocks Filament 1.75.0 / SceneView 4.31.0.
    // All io.github.sceneview / com.google.android.filament imports stay INSIDE this module,
    // hidden from feature modules behind the ExerciseRenderer interface.
    implementation(libs.sceneview)
    implementation(libs.filament.android)
    implementation(libs.filament.utils.android)
    implementation(libs.gltfio.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}