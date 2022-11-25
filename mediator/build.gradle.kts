plugins {
    application
}

application {
    mainClass.set("no.nav.dagpenger.quiz.mediator.MainKt")
}

dependencies {
    implementation(project(":model"))
    implementation(RapidAndRiversKtor2)
    implementation(Database.Flyway)
    implementation(Database.HikariCP)
    implementation(Database.Postgres)
    implementation(Database.Kotlinquery)
    implementation(Konfig.konfig)
    implementation(Kotlin.Logging.kotlinLogging)
    implementation("de.slub-dresden:urnlib:2.0.1")

    // unleash
    implementation("io.getunleash:unleash-client-java:7.0.0") {
        exclude("org.apache.logging.log4j")
    }

    testImplementation(Mockk.mockk)
    testImplementation(TestContainers.postgresql)
    testImplementation(Junit5.params)
    testImplementation(testFixtures(project(":model")))
}
