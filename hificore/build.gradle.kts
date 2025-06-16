import com.android.build.gradle.internal.cxx.configure.CmakeProperty

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.nift4.gramophone.hificore"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    externalNativeBuild.cmake {
        CmakeProperty.ANDROID_STL
    }
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        coreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
    }
    buildFeatures {
        prefab = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation("io.github.nift4.dlfunc:dlfunc:0.1.6")
    coreLibraryDesugaring('com.android.tools:desugar_jdk_libs:2.0.3')
}