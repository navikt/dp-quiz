plugins {
    application
}

application {
    mainClass.set("no.nav.dagpenger.quiz.mediator.MainKt")
}

dependencies {
    implementation(project(":model"))
    implementation(libs.rapids.and.rivers)
    implementation(libs.bundles.postgres)
    implementation(libs.konfig)
    implementation(libs.kotlin.logging)
    implementation("de.slub-dresden:urnlib:2.0.1")

    // unleash
    implementation("io.getunleash:unleash-client-java:9.2.4") {
        exclude("org.apache.logging.log4j")
    }

    testImplementation(libs.bundles.postgres.test)
    testImplementation(libs.mockk)
    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit}")
    testImplementation(testFixtures(project(":model")))
}
