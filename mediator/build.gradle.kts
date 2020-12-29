plugins {
    application
    id(Shadow.shadow) version Shadow.version
}

application {
    mainClassName = "no.nav.dagpenger.quiz.mediator.AppKt"
}

dependencies {
    implementation(project(":model"))
    implementation("com.github.navikt:rapids-and-rivers:1.20a7b92")
    implementation("org.flywaydb:flyway-core:6.3.2")
    implementation("com.zaxxer:HikariCP:3.4.1")
    implementation("org.postgresql:postgresql:42.2.11")
    implementation("com.github.seratch:kotliquery:1.3.1")
    implementation(Konfig.konfig)
    implementation(Kotlin.Logging.kotlinLogging)

    testImplementation(Mockk.mockk)
    testImplementation("org.testcontainers:postgresql:1.15.0-rc2")
}
