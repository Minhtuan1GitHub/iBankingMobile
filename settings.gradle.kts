pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()       // ✅ BẮT BUỘC: Thêm dòng này
        mavenCentral() // ✅ BẮT BUỘC: Thêm dòng này
    }
}

rootProject.name = "iBankingApp"
include(":app")