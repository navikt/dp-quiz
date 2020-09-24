package no.nav.dagpenger

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(factory = Netty, port = config[port]) {
        søknadApi()
        naisApi()
    }.start(wait = true)
}
