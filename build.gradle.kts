// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.navigationUi) apply false
    alias(libs.plugins.ksp)
    id("androidx.baselineprofile") version "1.4.1" apply false
    id("com.mikepenz.aboutlibraries.plugin") version "14.2.1" apply false
    id("com.osacky.doctor") version "0.12.1"
}

doctor {
    javaHome {
        ensureJavaHomeMatches.set(false)
        ensureJavaHomeIsSet.set(false)
    }
}

tasks.withType(JavaCompile::class.java) {
    options.compilerArgs.add("-Xlint:all")
}