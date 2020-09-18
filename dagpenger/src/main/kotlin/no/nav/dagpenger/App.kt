package no.nav.dagpenger

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import mu.KotlinLogging

val log = KotlinLogging.logger { }

fun main() {
    embeddedServer(factory = Netty, port = 8080) {
        søknadApi()
        naisApi()
    }.start(wait = true)
}
