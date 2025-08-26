plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.dagger.hilt.android)
    id("kotlin-kapt")
    alias(libs.plugins.google.play.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.makinul.background.remover"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.makinul.background.remover"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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

    implementation(libs.airbnb.lottie)

    // For Json parsing
    implementation(libs.google.gson)

    // Import the BoM for the Firebase platform
    implementation(platform(libs.google.firebase.bom))
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.realtime.database)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}