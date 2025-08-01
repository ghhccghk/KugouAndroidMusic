pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")  // 添加 JitPack 库
    }
}

gradle.extra.apply {
    set("androidxMediaEnableMidiModule", true)
}


rootProject.name = "musicplay"
(gradle as ExtensionAware).extra["androidxMediaModulePrefix"] = "media3-"
apply(from = file("media/core_settings.gradle"))
include(":hificore",":app")
 