plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    id("androidx.navigation.safeargs.kotlin") version "2.9.5"
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.mobil"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.mobil"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding=true
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
    kotlinOptions { jvmTarget = "11" }
}

dependencies {
    // Mevcut Standart Kütüphaneler
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Veritabanı
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    // Authentication (Giriş/Kayıt)
    implementation("com.google.firebase:firebase-auth-ktx:23.2.1")
    // Storage (Resim Yükleme)
    implementation("com.google.firebase:firebase-storage-ktx:21.0.2")

    // (Resim Gösterme)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.viewpager2)

    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    // Test Kütüphaneleri
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit: İnternetten veri (yazı/JSON) çekmek için (Kur API'si için)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // Glide: İnternetten resim yüklemek için (QR Kod API'si için)
    implementation("com.github.bumptech.glide:glide:4.16.0")
}