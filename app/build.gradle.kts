plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    kotlin("plugin.serialization") version "2.0.0"

    id("com.google.devtools.ksp")

    //SafeArgs for Navigation
    id("androidx.navigation.safeargs.kotlin")

    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.jobfinderapp"
    compileSdk = 36


    defaultConfig {
        applicationId = "com.example.jobfinderapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Основная библиотека Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Конвертер
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // Логирование запросов
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")


    // Navigation Component
    val nav_version = "2.7.4" // самая свежая стабильная версия на 2026 год
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")


    //Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")

    //Room Implementation
    val roomVersion = "2.6.1" // Актуальная стабильная версия

    // Основная библиотека Room
    implementation("androidx.room:room-runtime:$roomVersion")
    // Поддержка Kotlin Coroutines для Room (suspend функции)
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")
    // Процессор аннотаций (используем KSP)
    ksp("androidx.room:room-compiler:$roomVersion")

    // Для встроенной поддержки paging3
    val pagingVersion = "3.3.6" // Актуальная стабильная версия
    implementation("androidx.paging:paging-runtime:$pagingVersion")

    //Зависимость для SpringAnimation
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")

    //kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    //Data Store современаая замена устаревшему SharedPreferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    //SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    //Facebook shimmer для аннимированного скелетона
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    implementation("androidx.core:core-splashscreen:1.0.1")

    //lottie animation
    implementation("com.airbnb.android:lottie:6.7.1")
}