plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.google-services")
}

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.2")

    defaultConfig {
        applicationId = "uk.co.sksulai.multitasker"
        minSdkVersion(26)
        targetSdkVersion(30)
        versionCode = 2
        versionName = "0.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            "-Xallow-jvm-ir-dependencies",
            "-Xskip-prerelease-check",
            "-XXLanguage:+InlineClasses",
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }
    buildFeatures.compose = true
    composeOptions { kotlinCompilerExtensionVersion = Versions.compose }
    testOptions { unitTests.isIncludeAndroidResources = true }

    dependenciesInfo {
        includeInApk = true
        includeInBundle = true
    }
    packagingOptions {
        excludes += "/META-INF/AL2.0"
        excludes += "/META-INF/LGPL2.1"
    }
    bundle {
        language { enableSplit = true }
        density { enableSplit = true }
        abi { enableSplit = true }
    }
}

kapt.arguments {
    arg("room.schemaLocation",   "$projectDir/schemas")
    arg("room.incremental",      "true")
    arg("room.expandProjection", "true")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.30")

    // Kotlin Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.6")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.11.0")
    kapt("com.github.bumptech.glide:compiler:4.11.0")

    // Androidx
    implementation("androidx.core:core-ktx:1.5.0-beta02")
    implementation("androidx.appcompat:appcompat:1.3.0-beta01")
    implementation("com.google.android.material:material:1.3.0")

    // Jetpack Compose
    implementation("androidx.compose.runtime:runtime:1.0.0-beta02")
    implementation("androidx.compose.runtime:runtime-livedata:1.0.0-beta02")
    implementation("androidx.compose.foundation:foundation:1.0.0-beta02")
    implementation("androidx.compose.foundation:foundation-layout:1.0.0-beta02")
    implementation("androidx.compose.material:material:1.0.0-beta02")
    implementation("androidx.compose.material:material-icons-extended:1.0.0-beta02")
    implementation("androidx.compose.animation:animation:1.0.0-beta02")
    implementation("androidx.compose.ui:ui:1.0.0-beta02")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.0-alpha05")

    implementation("dev.chrisbanes.accompanist:accompanist-glide:0.6.2")
    implementation("dev.chrisbanes.accompanist:accompanist-insets:0.6.2")

    implementation("androidx.compose.ui:ui-tooling:1.0.0-beta02")
    implementation("androidx.compose.ui:ui-test:1.0.0-beta02")

    // Facebook
    implementation("com.facebook.android:facebook-login:[5,6)")

    // Firebase & Google
    implementation("com.google.android.gms:play-services-auth:19.0.0")

    implementation(platform("com.google.firebase:firebase-bom:25.3.1"))
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Arch: Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.3.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.3.0")
    implementation("androidx.lifecycle:lifecycle-service:2.3.0")
    implementation("androidx.lifecycle:lifecycle-process:2.3.0")

    // Arch: DataStore
    implementation("androidx.datastore:datastore:1.0.0-alpha07")
    implementation("androidx.datastore:datastore-preferences:1.0.0-alpha07")

    // Arch: Room
    implementation("androidx.room:room-runtime:2.3.0-beta02")
    kapt("androidx.room:room-compiler:2.3.0-beta02")
    implementation("androidx.room:room-ktx:2.3.0-beta02")

    // Arch: Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.3")
    implementation("androidx.navigation:navigation-compose:1.0.0-alpha08")

    // Arch: WorkManager
    implementation("androidx.work:work-runtime-ktx:2.7.0-alpha01")
    implementation("androidx.work:work-gcm:2.7.0-alpha01")

    // Testing
    testImplementation("junit:junit:4.13")
    testImplementation("androidx.test:core:1.3.0")
    testImplementation("org.robolectric:robolectric:4.3.1")
    testImplementation("org.mockito:mockito-core:2.19.0")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("androidx.room:room-testing:2.3.0-beta02")

    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test:rules:1.3.0")
    androidTestImplementation("org.hamcrest:hamcrest-library:2.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("androidx.navigation:navigation-testing:2.3.3")
	androidTestImplementation("androidx.work:work-testing:2.7.0-alpha01")
}
