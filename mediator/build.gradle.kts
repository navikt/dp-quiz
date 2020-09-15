plugins {
    application
    id(Shadow.shadow) version("5.2.0")
}

repositories {
    maven("https://jitpack.io")
}

application {
    mainClassName = "no.nav.dagpenger.AppKt"
}

dependencies {
    implementation(RapidAndRivers)

    implementation(Konfig.konfig)

    implementation(Database.Flyway)
    implementation(Database.HikariCP)
    implementation(Database.Postgres)
    implementation(Database.Kotlinquery)
    implementation(Database.VaultJdbc) {
        exclude(module = "slf4j-simple")
        exclude(module = "slf4j-api")
    }

    implementation(Kotlin.Logging.kotlinLogging)
}

tasks {
    named("shadowJar") {
        dependsOn("test")
    }
}
