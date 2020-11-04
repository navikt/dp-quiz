repositories {
    jcenter()
}
dependencies {
    implementation(project(":model"))
    implementation("com.github.navikt:rapids-and-rivers:1.20a7b92")
    implementation(Kotlin.Logging.kotlinLogging)

    testImplementation(Mockk.mockk)
}
