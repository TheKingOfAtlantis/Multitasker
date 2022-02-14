
object Version {
    const val hilt        = "2.40.5"
    const val lifecycle   = "2.4.0"
    const val workmanager = "2.7.1"
    const val datastore   = "1.0.0"
    const val room        = "2.4.0"

    const val compose     = "1.1.0-rc01"
    const val accompanist = "0.22.0-rc"
    const val nav         = "2.4.0-rc01"

    const val testing     = "1.4.1-alpha03"
    const val espresso    = "3.5.0-alpha03"

    const val retrofit    = "2.9.0"
}

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "uk.co.sksulai.multitasker"
        versionCode   = 2
        versionName   = "0.0.2"

        minSdk        = 26
        targetSdk     = 31

        testInstrumentationRunner = "uk.co.sksulai.multitasker.test.Runner"
        testInstrumentationRunnerArguments += mapOf("clearPackageData" to "true")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isTestCoverageEnabled = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
    buildFeatures.compose = true
    composeOptions { kotlinCompilerExtensionVersion = Version.compose }

    dependenciesInfo {
        includeInApk    = true
        includeInBundle = true
    }
    packagingOptions {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
    bundle {
        language { enableSplit = true }
        density  { enableSplit = true }
        abi      { enableSplit = true }
    }
}

kapt {
    correctErrorTypes = true
    arguments {
        arg("room.schemaLocation",  "$projectDir/schemas")
        arg("room.incremental",     "true")
        arg("room.expandProjection","true")
    }
}

dependencies {
    // Androidx Core
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.window:window:1.0.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:${Version.hilt}")
    kapt("com.google.dagger:hilt-android-compiler:${Version.hilt}")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:${Version.retrofit}")
    implementation("com.squareup.retrofit2:converter-gson:${Version.retrofit}")

    // Coil
    implementation("io.coil-kt:coil-compose:1.4.0")

    // Jetpack Compose
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.compose.ui:ui:1.1.0")
    implementation("androidx.compose.material:material:1.1.0")
    implementation("androidx.compose.material:material-icons-core:${Version.compose}")
    implementation("androidx.compose.material:material-icons-extended:${Version.compose}")

    debugImplementation("androidx.compose.ui:ui-tooling:1.1.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.1.0")

    //Compose: Accompanist
    implementation("com.google.accompanist:accompanist-insets:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-insets-ui:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-pager:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-pager-indicators:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-placeholder-material:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-permissions:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-drawablepainter:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-navigation-material:${Version.accompanist}")

    // Arch: Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Version.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${Version.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${Version.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate")

    // Arch: DataStore
    implementation("androidx.datastore:datastore:${Version.datastore}")
    implementation("androidx.datastore:datastore-preferences:${Version.datastore}")

    // Arch: Room
    implementation("androidx.room:room-runtime:${Version.room}")
    kapt("androidx.room:room-compiler:${Version.room}")
    implementation("androidx.room:room-ktx:${Version.room}")

    // Arch: Navigation
    implementation("androidx.navigation:navigation-compose:2.5.0-alpha01")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // Arch: WorkManager
    implementation("androidx.work:work-runtime-ktx:${Version.workmanager}")
    implementation("androidx.work:work-gcm:${Version.workmanager}")

    // Firebase & Google
    implementation("com.google.android.gms:play-services-auth:20.1.0")

    implementation(platform("com.google.firebase:firebase-bom:29.0.3"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-dynamic-links-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.0-native-mt")

    // Testing: Instrumentation Testing
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test.ext:truth:1.4.0")
    androidTestUtil("androidx.test:orchestrator:1.4.1")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Version.compose}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${Version.compose}")

    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")

    androidTestImplementation("com.google.dagger:hilt-android-testing:${Version.hilt}")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:${Version.hilt}")
}
