plugins {
    id("piggy.android.library")
}

android {
    namespace = "com.goldhardt.core.domain"

}

dependencies {
    api(libs.kotlinx.coroutines.core)
}
