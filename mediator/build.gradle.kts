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

    // unleash
    implementation("no.finn.unleash:unleash-client-java:4.2.1") {
        exclude("org.apache.logging.log4j")
    }

    testImplementation(Mockk.mockk)
    testImplementation(TestContainers.postgresql)
    testImplementation(Junit5.params)
    testImplementation("guru.nidi:graphviz-java:0.18.1")
    testImplementation("guru.nidi:graphviz-kotlin:0.18.1")
}
