rootProject.name = "dp-quiz"

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        maven(url = "https://dl.bintray.com/gradle/gradle-plugins")
    }
}

include("mediator", "dagpenger", "model")
include("nav-mediator")
