// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha08")
        classpath(kotlin("gradle-plugin", version = "1.4.30"))
        classpath("com.google.gms:google-services:4.3.5")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    }
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}