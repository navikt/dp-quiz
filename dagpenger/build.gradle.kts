dependencies {
    implementation(project(":model"))

    implementation(Ktor.server)
    implementation(Ktor.serverNetty)
    implementation(Ktor.library("jackson"))

    implementation(Kotlin.Logging.kotlinLogging)

    testImplementation(Ktor.ktorTest)
}
