
plugins {
    id("com.android.application")
    
  id("kotlin-android")
  
  id("com.google.devtools.ksp")
  
}

android {
    namespace = "com.zandeveloper.tiktokly"
    compileSdk = 35
    
    
    defaultConfig {
        applicationId = "com.zandeveloper.tiktokly"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
        
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    buildFeatures {
        viewBinding = true
        
    }
    
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget("17"))
    }
}

dependencies {

implementation("com.squareup.okhttp3:okhttp:4.11.0")
implementation("com.google.code.gson:gson:2.11.0")
implementation("com.github.Spikeysanju:MotionToast:1.4")
	
     implementation("com.google.android.gms:play-services-ads:22.6.0")

     implementation("com.github.bumptech.glide:glide:4.16.0")
ksp("com.github.bumptech.glide:ksp:4.16.0")
     
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
