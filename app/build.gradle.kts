
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
    compileSdk        = 31
    buildToolsVersion = "32.0.0-rc1"

    defaultConfig {
        applicationId = "uk.co.sksulai.multitasker"
        minSdk        = 26
        targetSdk     = 31
        versionCode   = 2
        versionName   = "0.0.2"

        testInstrumentationRunner = "uk.co.sksulai.multitasker.test.Runner"
        testInstrumentationRunnerArguments += mapOf(
            "clearPackageData" to "true"
        )
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isTestCoverageEnabled = true
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += listOf(
            "-Xskip-prerelease-check",
            "-XXLanguage:+InlineClasses",
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }
    buildFeatures.compose = true
    composeOptions { kotlinCompilerExtensionVersion = "1.1.0-beta02" }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    dependenciesInfo {
        includeInApk    = true
        includeInBundle = true
    }
    packagingOptions {
        resources.excludes += setOf(
            "/META-INF/AL2.0",
            "/META-INF/LGPL2.1"
        )
    }
    bundle {
        language { enableSplit = true }
        density  { enableSplit = true }
        abi      { enableSplit = true }
    }

    useLibrary("android.test.runner")
    useLibrary("android.test.base")
    useLibrary("android.test.mock")
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
    implementation(kotlin("stdlib", version = "1.5.31"))

    // Kotlin Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2-native-mt")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.5.2-native-mt")

    // Hilt
    implementation("com.google.dagger:hilt-android:${Version.hilt}")
    kapt("com.google.dagger:hilt-android-compiler:${Version.hilt}")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:${Version.retrofit}")
    implementation("com.squareup.retrofit2:converter-gson:${Version.retrofit}")

    // Coil
    implementation("io.coil-kt:coil-compose:1.4.0")

    // Androidx
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.window:window:1.0.0-rc01")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:${Version.compose}")
    implementation("androidx.compose.ui:ui-test:${Version.compose}")
    implementation("androidx.compose.ui:ui-tooling:${Version.compose}")
    implementation("androidx.compose.foundation:foundation:${Version.compose}")
    implementation("androidx.compose.material:material:${Version.compose}")
    implementation("androidx.compose.material:material-icons-core:${Version.compose}")
    implementation("androidx.compose.material:material-icons-extended:${Version.compose}")

    implementation("androidx.compose.foundation:foundation-layout:1.0.5")
    implementation("androidx.compose.animation:animation:1.1.0-rc01")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${Version.lifecycle}")

    implementation("com.google.accompanist:accompanist-insets:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-insets-ui:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-pager:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-pager-indicators:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-placeholder-material:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-permissions:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-drawablepainter:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-navigation-material:0.22.1-SNAPSHOT")

    // Firebase & Google
    implementation("com.google.android.gms:play-services-auth:20.0.1")

    implementation(platform("com.google.firebase:firebase-bom:29.0.3"))
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-dynamic-links-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")


    // Arch: Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Version.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Version.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${Version.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:${Version.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${Version.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-service:${Version.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-process:${Version.lifecycle}")

    // Arch: DataStore
    implementation("androidx.datastore:datastore:${Version.datastore}")
    implementation("androidx.datastore:datastore-preferences:${Version.datastore}")

    // Arch: Room
    implementation("androidx.room:room-runtime:${Version.room}")
    kapt("androidx.room:room-compiler:${Version.room}")
    implementation("androidx.room:room-ktx:${Version.room}")

    // Arch: Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:${Version.nav}")
    implementation("androidx.navigation:navigation-ui-ktx:${Version.nav}")
    implementation("androidx.navigation:navigation-compose:2.4.0-rc01")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0-rc01")

    // Arch: WorkManager
    implementation("androidx.work:work-runtime-ktx:${Version.workmanager}")
    implementation("androidx.work:work-gcm:${Version.workmanager}")

    // Testing: Instrumentation Testing

    androidTestImplementation("androidx.test.ext:junit:+")
    androidTestImplementation("androidx.test.espresso:espresso-core:+")

    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.3")
    androidTestImplementation("androidx.test.ext:truth:1.4.0")
    androidTestImplementation("com.google.truth:truth:1.1.3")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Version.compose}")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")

    androidTestImplementation("com.google.dagger:hilt-android-testing:${Version.hilt}")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:${Version.hilt}")
}
