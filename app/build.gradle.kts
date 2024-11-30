plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.dagger.hilt.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.makinul.background.remover"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.makinul.background.remover"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.1"

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
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // media pipe AI
    implementation(libs.mediapipe.tasks.vision)
    implementation(libs.mediapipe.tasks.text)

    // for dependency injection
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)

    // CameraX core library
    implementation(libs.androidx.camera.core)
//    implementation "androidx.camera:camera-core:$camerax_version"
//
//    // CameraX Camera2 extensions
//    implementation "androidx.camera:camera-camera2:$camerax_version"
//
//    // CameraX Lifecycle library
//    implementation "androidx.camera:camera-lifecycle:$camerax_version"
//
//    implementation "androidx.camera:camera-video:${camerax_version}"
//
//    // CameraX View class
//    implementation "androidx.camera:camera-view:$camerax_version"
//    implementation "androidx.camera:camera-extensions:${camerax_version}"

    // coroutines
    implementation(libs.jetbrains.kotlinx.coroutines)

    // for network calling
    implementation(libs.squareup.retrofit2.retrofit)
    implementation(libs.squareup.retrofit2.converter.gson)

    // okhttp for retrofit
    implementation(libs.squareup.okhttp3.okhttp)
    implementation(libs.squareup.okhttp3.logging.interceptor)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}