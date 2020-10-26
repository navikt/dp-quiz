plugins {
    application
    id(Shadow.shadow) version Shadow.version
}

application {
    mainClassName = "no.nav.dagpenger.AppKt"
}

dependencies {
    implementation(project(":model"))

    implementation(Konfig.konfig)

    implementation(Ktor.serverNetty)
    implementation(Ktor.library("jackson"))

    implementation(Kotlin.Logging.kotlinLogging)
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("net.logstash.logback:logstash-logback-encoder:6.4")
    implementation("com.github.navikt:rapids-and-rivers:1.2954646")
    testImplementation(Ktor.ktorTest)
}
