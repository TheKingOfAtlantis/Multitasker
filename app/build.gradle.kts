plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("com.google.gms.google-services")
}

android {
    compileSdkVersion(29)
    buildToolsVersion("30.0.0")

    defaultConfig {
        applicationId = "uk.co.sksulai.multitasker"
        minSdkVersion(26)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "0.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
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
        freeCompilerArgs = listOf(
            "-XXLanguage:+InlineClasses",
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }
    buildFeatures.compose = true
    composeOptions {
        kotlinCompilerVersion = "1.3.70-dev-withExperimentalGoogleExtensions-20200424"
        kotlinCompilerExtensionVersion = Versions.compose
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    dependenciesInfo {
        includeInApk = true
        includeInBundle = true
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")

    // Kotlin Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.6")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.11.0")
    kapt("com.github.bumptech.glide:compiler:4.11.0")

    // Androidx
    implementation("androidx.core:core-ktx:1.3.0")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("com.google.android.material:material:1.1.0")

    // Jetpack Compose
    implementation("androidx.ui:ui-core:${Versions.compose}")
    implementation("androidx.ui:ui-layout:${Versions.compose}")
    implementation("androidx.ui:ui-material:${Versions.compose}")
    implementation("androidx.ui:ui-tooling:${Versions.compose}")
    implementation("androidx.ui:ui-material-icons-extended:${Versions.compose}")
    implementation("androidx.ui:ui-livedata:${Versions.compose}")
    implementation("com.github.zsoltk:compose-router:0.12.0")
    // Facebook
    implementation("com.facebook.android:facebook-login:[5,6)")

    // Firebase & Google
    implementation("com.google.android.gms:play-services-auth:18.0.0")

    implementation(platform("com.google.firebase:firebase-bom:25.3.1"))
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Arch: Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-service:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-process:${Versions.lifecycle}")

    // Arch: Room
    implementation("androidx.room:room-runtime:${Versions.room}")
    kapt("androidx.room:room-compiler:${Versions.room}")
    implementation("androidx.room:room-ktx:${Versions.room}")

    // Arch: Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:${Versions.nav}")
    implementation("androidx.navigation:navigation-ui-ktx:${Versions.nav}")

    // Arch: WorkManager
    implementation("androidx.work:work-runtime-ktx:${Versions.work}")
    implementation("androidx.work:work-gcm:${Versions.work}")

    // Testing
    testImplementation("junit:junit:4.13")
    testImplementation("androidx.test:core:1.2.0")
    testImplementation("org.robolectric:robolectric:4.3.1")
    testImplementation("org.mockito:mockito-core:2.19.0")
    testImplementation("androidx.arch.core:core-testing:${Versions.arch}")
    testImplementation("androidx.room:room-testing:${Versions.room}")

    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("org.hamcrest:hamcrest-library:2.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("androidx.navigation:navigation-testing:${Versions.nav}")
	androidTestImplementation("androidx.work:work-testing:${Versions.work}")
}
