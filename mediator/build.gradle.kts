plugins {
    application
    id(Shadow.shadow) version Shadow.version
}

application {
    mainClassName = "no.nav.dagpenger.quiz.mediator.AppKt"
}

dependencies {
    implementation(project(":model"))
    implementation(RapidAndRivers)
    implementation(Database.Flyway)
    implementation(Database.HikariCP)
    implementation(Database.Postgres)
    implementation(Database.Kotlinquery)
    implementation(Konfig.konfig)
    implementation(Kotlin.Logging.kotlinLogging)

    testImplementation(Mockk.mockk)
    testImplementation(TestContainers.postgresql)
}
