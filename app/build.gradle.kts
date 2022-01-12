plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
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
        useIR     = true
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
    implementation("com.google.dagger:hilt-android:2.38.1")
    kapt("com.google.dagger:hilt-android-compiler:2.38.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")

    // Androidx
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.0-rc01")
    implementation("com.google.android.material:material:1.4.0")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.1.0-beta02")
    implementation("androidx.compose.ui:ui-tooling:1.1.0-beta02")
    implementation("androidx.compose.foundation:foundation:1.1.0-beta02")
    implementation("androidx.compose.material:material:1.1.0-beta02")
    implementation("androidx.compose.material:material-icons-core:1.1.0-beta02")
    implementation("androidx.compose.material:material-icons-extended:1.1.0-beta02")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.1.0-beta02")

    implementation("androidx.compose.foundation:foundation-layout:1.1.0-beta02")
    implementation("androidx.compose.animation:animation:1.1.0-beta02")

    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.0-rc01")

    implementation("com.google.accompanist:accompanist-glide:0.15.0")
    implementation("com.google.accompanist:accompanist-insets:0.13.0")

    implementation("androidx.compose.ui:ui-tooling:1.1.0-beta02")
    implementation("androidx.compose.ui:ui-test:1.1.0-beta02")

    // Firebase & Google
    implementation("com.google.android.gms:play-services-auth:19.2.0")

    implementation(platform("com.google.firebase:firebase-bom:29.0.0"))
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-dynamic-links-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")


    // Arch: Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.4.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.4.0")
    implementation("androidx.lifecycle:lifecycle-service:2.4.0")
    implementation("androidx.lifecycle:lifecycle-process:2.4.0")

    // Arch: DataStore
    implementation("androidx.datastore:datastore:1.0.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Arch: Room
    implementation("androidx.room:room-runtime:2.4.0-beta01")
    kapt("androidx.room:room-compiler:2.4.0-beta01")
    implementation("androidx.room:room-ktx:2.4.0-beta01")

    // Arch: Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")
    implementation("androidx.navigation:navigation-compose:2.4.0-beta02")

    // Arch: WorkManager
    implementation("androidx.work:work-runtime-ktx:2.7.0-alpha04")
    implementation("androidx.work:work-gcm:2.7.0-alpha04")

    // Testing: Instrumentation Testing

    androidTestImplementation("androidx.test.ext:junit:+")
    androidTestImplementation("androidx.test.espresso:espresso-core:+")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.1.0-beta02")

    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.3")
    androidTestImplementation("androidx.test.ext:truth:1.4.0")
    androidTestImplementation("com.google.truth:truth:1.1.3")

    androidTestImplementation("com.google.dagger:hilt-android-testing:2.38.1")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.38.1")
}
