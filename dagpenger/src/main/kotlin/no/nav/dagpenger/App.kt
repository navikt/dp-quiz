package no.nav.dagpenger

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.regelverk.dimisjonsdato
import no.nav.dagpenger.regelverk.egenBedrift
import no.nav.dagpenger.regelverk.egenBondegård
import no.nav.dagpenger.regelverk.fangstOgFisk
import no.nav.dagpenger.regelverk.fødselsdato
import no.nav.dagpenger.regelverk.inngangsvilkår
import no.nav.dagpenger.regelverk.inntektSiste3år
import no.nav.dagpenger.regelverk.inntektSisteÅr
import no.nav.dagpenger.regelverk.villigDeltid
import no.nav.dagpenger.regelverk.villigHelse
import no.nav.dagpenger.regelverk.villigJobb
import no.nav.dagpenger.regelverk.villigPendle
import no.nav.dagpenger.regelverk.virkningstidspunkt

fun main() {
    val personalia = Seksjon(Rolle.søker, fødselsdato)
    val datoer = Seksjon(Rolle.søker, virkningstidspunkt, dimisjonsdato)
    val egenNæring = Seksjon(Rolle.søker, egenBondegård, egenBedrift, fangstOgFisk)
    val inntekter = Seksjon(Rolle.søker, inntektSisteÅr, inntektSiste3år)
    val reellArbeidssøker = Seksjon(Rolle.søker, villigDeltid, villigHelse, villigJobb, villigPendle)

    val søknader = InMemorySøknader {
        Søknad(
            personalia,
            reellArbeidssøker,
            datoer,
            egenNæring,
            inntekter
        )
    }
    embeddedServer(factory = Netty, port = config[port]) {
        søknadApi(søknader, inngangsvilkår)
        naisApi()
    }.start(wait = true)
}
