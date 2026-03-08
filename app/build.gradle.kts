plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")

    //SafeArgs for Navigation
    id("androidx.navigation.safeargs.kotlin")

    id("kotlin-parcelize")
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

    //Dagger2
    val daggerVersion = "2.51.1"
    implementation("com.google.dagger:dagger:$daggerVersion")
    // Процессор аннотаций (генерирует код)
    ksp("com.google.dagger:dagger-compiler:$daggerVersion")
    implementation("com.google.dagger:dagger-android:$daggerVersion")
    ksp("com.google.dagger:dagger-android-processor:$daggerVersion")

    //Room Implementation
    val roomVersion = "2.6.1" // Актуальная стабильная версия

    // Основная библиотека Room
    implementation("androidx.room:room-runtime:$roomVersion")

    // Поддержка Kotlin Coroutines для Room (suspend функции)
    implementation("androidx.room:room-ktx:$roomVersion")

    // Процессор аннотаций (используем KSP)
    ksp("androidx.room:room-compiler:$roomVersion")
}