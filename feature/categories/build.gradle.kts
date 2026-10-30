plugins {
    id("piggy.android.compose")
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.goldhardt.feature.categories"

    defaultConfig {
        testInstrumentationRunner = "dagger.hilt.android.testing.HiltTestRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.auth)
    implementation(projects.core.designsystem)

    implementation(libs.androidx.core.ktx)

    // UI + Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Lifecycle ViewModel KTX
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Hilt Navigation Compose
    implementation(libs.hilt.navigation.compose)

    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.ui.tooling)
}
