plugins {
    application
    id(Shadow.shadow) version Shadow.version
}

application {
    mainClassName = "no.nav.dagpenger.AppKt"
}

dependencies {
    implementation(project(":model"))

    implementation(Konfig.konfig)

    implementation(Ktor.server)
    implementation(Ktor.serverNetty)
    implementation(Ktor.library("jackson"))

    implementation(Kotlin.Logging.kotlinLogging)

    testImplementation(Ktor.ktorTest)
}
