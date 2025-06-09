import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.cxx.configure.CmakeProperty

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.abc) apply false
    id("com.google.devtools.ksp")
    kotlin("plugin.parcelize")
    kotlin("plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.ghhccghk.musicplay"
    compileSdk = 35

    val buildTime = System.currentTimeMillis()
    defaultConfig {
        applicationId = "com.ghhccghk.musicplay"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        //noinspection ChromeOsAbiSupport
        ndk.abiFilters += arrayOf("arm64-v8a", "armeabi-v7a")
        buildConfigField("long", "BUILD_TIME", "$buildTime")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 启用按架构分包
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = true
        }
    }


    buildTypes {
        externalNativeBuild {
            cmake {
                path = file("src/main/cpp/CMakeLists.txt")
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            vcsInfo.include = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    externalNativeBuild.cmake {
        CmakeProperty.ANDROID_STL
    }
    buildFeatures {
        compose = true
    }
    ksp {
        arg("room.schemaLocation", project.layout.projectDirectory.dir("schemas").asFile.absolutePath)
    }
    applicationVariants.all {
        outputs.all {
            val outputImpl = this as BaseVariantOutputImpl
            // 尝试获取 filters 中的 abi 架构名称
            val abiFilter = outputImpl.filters.find { it.filterType.equals("ABI", ignoreCase = true) }
            val abiName = abiFilter?.identifier ?: "universal"
            outputImpl.outputFileName =
                "Music_Player-$versionName-$versionCode-$name-$abiName-$buildTime.apk"
        }
    }
}

dependencies {
    implementation(project(":hificore"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.preference.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)

    implementation(libs.core)
    implementation(libs.zxing.android.embedded)
    implementation(libs.gson)
    implementation(libs.glide)


    val composeBom = platform("androidx.compose:compose-bom:2025.05.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Choose one of the following:
    // Material Design 3
    implementation(libs.androidx.material3)
    // or Material Design 2
    implementation(libs.androidx.material)
    // or skip Material Design and build directly on top of foundational components
    implementation(libs.androidx.foundation)
    // or only import the main APIs for the underlying toolkit systems,
    // such as input and measurement/layout
    implementation(libs.androidx.ui)

    // Android Studio Preview support
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    // UI Tests
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Optional - Included automatically by material, only add when you need
    // the icons but not the material library (e.g. when using Material3 or a
    // custom design system based on Foundation)
    implementation(libs.androidx.material.icons.core)
    // Optional - Add full set of material icons
    implementation(libs.androidx.material.icons.extended)
    // Optional - Add window size utils
    implementation(libs.androidx.adaptive)

    // Optional - Integration with activities
    implementation(libs.androidx.activity.compose)
    // Optional - Integration with ViewModels
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Optional - Integration with LiveData
    implementation(libs.androidx.runtime.livedata)
    // Optional - Integration with RxJava
    implementation(libs.androidx.runtime.rxjava2)

    implementation(libs.androidx.media3.exoplayer)// Media3 ExoPlayer
    implementation(libs.androidx.media3.session)   // 媒体会话管理
    implementation(libs.androidx.media3.ui)   // 媒体会话管理
    implementation(libs.androidx.media3.ui.compose)   // 媒体会话管理

    implementation(libs.superlyricapi)

    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen) //
    implementation(libs.androidx.room.ktx)

    implementation( libs.kotlinx.coroutines.core)
    implementation( libs.kotlinx.coroutines.android)

    implementation( libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation (libs.androidx.room.rxjava2)
    implementation( libs.androidx.room.ktx)

    implementation (libs.rxjava)
    implementation(libs.rxandroid)

    implementation(libs.androidx.mediarouter)

    implementation(libs.androidx.palette.ktx)

    implementation(libs.cupertino.icons.extended)



}