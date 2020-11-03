repositories {
    jcenter()
}
dependencies {
    implementation(project(":model"))
    implementation("com.github.navikt:rapids-and-rivers:1.74ae9cb")
    implementation(Kotlin.Logging.kotlinLogging)

    testImplementation(Mockk.mockk)
}
