// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.1")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.38.1")
    }
}

plugins {
    id("com.android.application") version "7.1.1"  apply false
    id("com.android.library")     version "7.1.1"  apply false
    kotlin("android")             version "1.5.21" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
