import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.devtools.ksp")
    id("com.google.firebase.firebase-perf")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.zandeveloper.tiktokly"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zandeveloper.tiktokly"
        minSdk = 23
        targetSdk = 35
        versionCode = 2
        versionName = "1.6.5"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // -------------------------
    // LOAD key.properties
    // -------------------------
    val keystoreProps = Properties()
    val keystoreFile = rootProject.file("key.properties")

    if (keystoreFile.exists()) {
        FileInputStream(keystoreFile).use { keystoreProps.load(it) }
    }
    // -------------------------

    signingConfigs {
        create("release") {
            if (keystoreFile.exists()) {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget("17"))
    }
}

dependencies {
    implementation("com.github.takusemba:spotlight:2.0.5")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.github.Spikeysanju:MotionToast:1.4")
    implementation("com.google.firebase:firebase-perf")
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.android.gms:play-services-ads:22.6.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")

    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}