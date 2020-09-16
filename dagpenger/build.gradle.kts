dependencies {

    implementation(project(":model"))
    implementation(Ktor.server)
    implementation(Ktor.serverNetty)
    implementation(Ktor.library("jackson"))
    testImplementation(Ktor.ktorTest)
}
