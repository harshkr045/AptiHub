@file:Suppress("DEPRECATION")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.harshkr.aptihub"
    compileSdk = 36

    signingConfigs {
        create("release") {
            val releaseStoreFile = project.findProperty("RELEASE_STORE_FILE")
            val releaseStorePassword = project.findProperty("RELEASE_STORE_PASSWORD")
            val releaseKeyAlias = project.findProperty("RELEASE_KEY_ALIAS")
            val releaseKeyPassword = project.findProperty("RELEASE_KEY_PASSWORD")

            if (releaseStoreFile != null) {
                storeFile = file(releaseStoreFile.toString())
                storePassword = releaseStorePassword?.toString()
                keyAlias = releaseKeyAlias?.toString()
                keyPassword = releaseKeyPassword?.toString()
            }
        }
    }

    defaultConfig {
        applicationId = "com.harshkr.aptihub"
        minSdk = 24
        targetSdk = 36
        versionCode = 6
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        viewBinding = true
    }
}

dependencies {
    //noinspection UseTomlInstead
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    // The BOM will now manage all of these versions correctly
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    //noinspection UseTomlInstead
    implementation("com.airbnb.android:lottie:6.7.1")
    //noinspection UseTomlInstead
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
