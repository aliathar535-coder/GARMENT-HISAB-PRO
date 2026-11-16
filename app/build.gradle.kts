
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.garment.hisabpro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.garment.hisabpro"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
