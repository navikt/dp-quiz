package no.nav.dagpenger

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.regelverk.dimisjonsdato
import no.nav.dagpenger.regelverk.fødselsdato
import no.nav.dagpenger.regelverk.utestengt
import no.nav.dagpenger.regelverk.ønsketDato

fun main() {
    val søknader = InMemorySøknader {
        Søknad(
            Seksjon(Rolle.søker, ønsketDato, fødselsdato),
            Seksjon(Rolle.søker, dimisjonsdato),
            Seksjon(Rolle.søker, utestengt),
        )
    }
    embeddedServer(factory = Netty, port = config[port]) {
        søknadApi(søknader)
        naisApi()
    }.start(wait = true)
}
