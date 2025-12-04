plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.com.google.dagger.hilt)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.experiment.jetpackxr.soundexplorer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.experiment.jetpackxr.soundexplorer"
        minSdk = 34
        targetSdk = 35
        versionCode = 300
        versionName = "0.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // Futures
    implementation(libs.androidx.concurrent.futures)
    implementation(libs.kotlinx.coroutines.guava)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // XR
    implementation(libs.androidx.xr.compose)
    implementation(libs.androidx.xr.arcore)
    implementation(libs.androidx.xr.material3)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    // AndroidX
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material3.android)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.foundation)

    // material icons
    implementation(libs.androidx.compose.material.icons.core)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.espresso.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.scenecore.testing)
    testImplementation(libs.androidx.compose.testing)
    testImplementation(libs.androidx.runtime.testing)
    testImplementation(libs.robolectric)

    // Testing
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
}