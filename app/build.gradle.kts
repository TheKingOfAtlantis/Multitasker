plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-android-extensions'
    id 'kotlin-kapt'
    id 'com.google.gms.google-services'
}

android {
    compileSdkVersion 29
    buildToolsVersion "29.0.3"

    defaultConfig {
        applicationId "uk.co.sksulai.multitasker"
        minSdkVersion 26
        targetSdkVersion 29
        versionCode 1
        versionName "0.0.1"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
        freeCompilerArgs = [
            "-XXLanguage:+InlineClasses",
            '-Xopt-in=kotlin.RequiresOptIn'
        ]
    }
    buildFeatures.compose true
    composeOptions {
        kotlinCompilerVersion = "1.3.70-dev-withExperimentalGoogleExtensions-20200424"
        kotlinCompilerExtensionVersion "0.1.0-dev11"
    }
}

kapt.arguments {
    arg("room.schemaLocation",   "$projectDir/schemas")
    arg("room.incremental",      "true")
    arg("room.expandProjection", "true")
}

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"

    // Kotlin Coroutine
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.6'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.6'

    // Glide
    implementation 'com.github.bumptech.glide:glide:4.11.0'
    kapt 'com.github.bumptech.glide:compiler:4.11.0'

    // Androidx
    implementation 'androidx.core:core-ktx:1.2.0'
    implementation 'androidx.appcompat:appcompat:1.1.0'
    implementation 'com.google.android.material:material:1.1.0'

    // Jetpack Compose
    def compose_version = '0.1.0-dev11'
    implementation "androidx.ui:ui-core:$compose_version"
    implementation "androidx.ui:ui-layout:$compose_version"
    implementation "androidx.ui:ui-material:$compose_version"
    implementation "androidx.ui:ui-tooling:$compose_version"
    implementation "androidx.ui:ui-material-icons-extended:$compose_version"
    implementation "androidx.ui:ui-livedata:$compose_version"


    // Facebook
    implementation 'com.facebook.android:facebook-login:5.15.3'

    // Firebase & Google
    implementation platform('com.google.firebase:firebase-bom:25.3.1')
    implementation "com.google.android.gms:play-services-auth:18.0.0"
    implementation 'com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava'
    implementation 'com.google.firebase:firebase-analytics-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'
    implementation 'com.google.firebase:firebase-auth-ktx'

    // Arch: Lifecycle
    def lifecycle_version = "2.2.0"
    def arch_version = "2.1.0"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-common-java8:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-service:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-process:$lifecycle_version"

    // Arch: Room
    def room_version = "2.2.5"
    implementation "androidx.room:room-runtime:$room_version"
    kapt "androidx.room:room-compiler:$room_version"
    implementation "androidx.room:room-ktx:$room_version"

    // Arch: Navigation
    def nav_version = "2.3.0-beta01"
    implementation "androidx.navigation:navigation-fragment-ktx:$nav_version"
    implementation "androidx.navigation:navigation-ui-ktx:$nav_version"

    // Arch: WorkManager
    def work_version = "2.3.4"
    implementation "androidx.work:work-runtime-ktx:$work_version"
    implementation "androidx.work:work-gcm:$work_version"

    // Testing
    testImplementation 'junit:junit:4.13'
    testImplementation 'org.robolectric:robolectric:4.3'
    testImplementation "androidx.arch.core:core-testing:$arch_version"
    testImplementation "androidx.room:room-testing:$room_version"
    androidTestImplementation 'androidx.test.ext:junit:1.1.1'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.2.0'
    androidTestImplementation "androidx.navigation:navigation-testing:$nav_version"
	androidTestImplementation "androidx.work:work-testing:$work_version"
}
