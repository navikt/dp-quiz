repositories {
    jcenter()
    maven("https://jitpack.io")
}
dependencies {
    implementation(project(":model"))
    implementation("com.github.navikt:rapids-and-rivers:1.74ae9cb")
    implementation("org.flywaydb:flyway-core:6.3.2")
    implementation("com.zaxxer:HikariCP:3.4.1")
    implementation("org.postgresql:postgresql:42.2.11")
    implementation("com.github.seratch:kotliquery:1.3.1")
    implementation(Kotlin.Logging.kotlinLogging)

    testImplementation(Mockk.mockk)
    testImplementation("org.testcontainers:postgresql:1.15.0-rc2")
}
