plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.screenshotcleaner"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.screenshotcleaner"
        minSdk = 29
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.0"
    }

    buildFeatures { buildConfig = true }
    buildTypes {
        release { isMinifyEnabled = false }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.google.android.gms:play-services-ads:25.4.0")
    implementation("com.android.billingclient:billing-ktx:9.1.0")
}
