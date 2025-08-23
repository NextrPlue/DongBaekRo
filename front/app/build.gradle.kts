plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.redstonetorch.dongbaekro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.redstonetorch.dongbaekro"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        val kakaoRestKey = project.findProperty("KAKAO_REST_KEY") as String? ?: "3f64ed6fe279a3283fde2430006a681f"
        val kakaoNativeKey = project.findProperty("KAKAO_NATIVE_KEY") as String? ?: "d17493f40fc2b80fc57cb03e3abdddec"
        
        println("DEBUG: KAKAO_REST_KEY = '$kakaoRestKey'")
        println("DEBUG: KAKAO_NATIVE_KEY = '$kakaoNativeKey'")

        manifestPlaceholders["KAKAO_NATIVE_KEY"] = kakaoNativeKey


        buildConfigField("String", "KAKAO_REST_KEY", "\"$kakaoRestKey\"")
        buildConfigField("String", "KAKAO_NATIVE_KEY", "\"$kakaoNativeKey\"")



        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
        
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
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            pickFirsts += setOf(
                "META-INF/versions/9/OSGI-INF/MANIFEST.MF",
                "META-INF/DEPENDENCIES"
            )
            excludes += setOf(
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }
}

dependencies {
    // Compose BOM으로 버전 통일
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    
    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.compose.runtime:runtime-livedata")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(libs.androidx.navigation.runtime.android)
    
    // Hilt - 통일된 버전
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    
    // 카카오 SDK
    implementation("com.kakao.sdk:v2-all:2.20.0")
    implementation("com.kakao.maps.open:android:2.12.8")
    
    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")


    // Storage
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    // Camera
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    
    // Images
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.github.CanHub:Android-Image-Cropper:4.3.2")
    implementation("com.github.yalantis:ucrop:2.2.8")
    
    // Permissions - 최신 API 사용
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // Animation
    implementation("com.airbnb.android:lottie-compose:6.4.0")
    
    // WebSocket & STOMP
    implementation("org.java-websocket:Java-WebSocket:1.5.4")
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    
    // ML
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.22.0")
    implementation(libs.generativeai)
    
    // Calendar
    implementation("com.kizitonwose.calendar:compose:2.5.0")
    
    // Payment
    implementation("com.github.iamport:iamport-android:v1.4.8")
    
    // Other
    implementation(libs.common)
    implementation(libs.media3.common.ktx)
    implementation(libs.androidx.compose.testing)
    
    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
