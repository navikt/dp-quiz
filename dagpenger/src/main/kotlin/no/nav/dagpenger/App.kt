package no.nav.dagpenger

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.dagpenger.regelverk.inngangsvilkår

fun main() {
    val søknader = InMemorySøknader(EnkelSøknad())

    embeddedServer(factory = Netty, port = config[port]) {
        søknadApi(søknader, inngangsvilkår)
        naisApi()
    }.start(wait = true)
}
