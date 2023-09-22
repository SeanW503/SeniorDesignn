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
        google()
        mavenCentral()

    }
}

buildscript {
    repositories {
        // maven {
        //     url 'https://example.com/maven-repo'
        // }
    }
    // ...
}


rootProject.name = "PlantEye"
include(":app")


