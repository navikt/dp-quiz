rootProject.name = "dp-quiz"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://dl.bintray.com/gradle/gradle-plugins")
    }
}

include("mediator", "model")

dependencyResolutionManagement {
    repositories {
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    versionCatalogs {
        create("libs") {
            from("no.nav.dagpenger:dp-version-catalog:20250522.167.2ca026")
        }
    }
}
