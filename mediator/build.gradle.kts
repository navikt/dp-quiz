plugins {
    application
    id(Shadow.shadow) version Shadow.version
}

application {
    mainClass.set("no.nav.dagpenger.quiz.mediator.AppKt")
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
    implementation("de.slub-dresden:urnlib:2.0.1")

    // unleash
    implementation("io.getunleash:unleash-client-java:5.0.2") {
        exclude("org.apache.logging.log4j")
    }

    testImplementation(Mockk.mockk)
    testImplementation(TestContainers.postgresql)
    testImplementation(Junit5.params)
}
