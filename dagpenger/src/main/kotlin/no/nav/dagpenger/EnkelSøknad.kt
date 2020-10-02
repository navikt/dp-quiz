package no.nav.dagpenger

import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.regelverk.datoForBortfallPgaAlder
import no.nav.dagpenger.regelverk.dimisjonsdato
import no.nav.dagpenger.regelverk.egenBedrift
import no.nav.dagpenger.regelverk.egenBondegård
import no.nav.dagpenger.regelverk.fangstOgFisk
import no.nav.dagpenger.regelverk.fødselsdato
import no.nav.dagpenger.regelverk.inntekt15G
import no.nav.dagpenger.regelverk.inntekt3G
import no.nav.dagpenger.regelverk.inntektSiste3år
import no.nav.dagpenger.regelverk.inntektSisteÅr
import no.nav.dagpenger.regelverk.villigDeltid
import no.nav.dagpenger.regelverk.villigHelse
import no.nav.dagpenger.regelverk.villigJobb
import no.nav.dagpenger.regelverk.villigPendle
import no.nav.dagpenger.regelverk.virkningstidspunkt

internal class EnkelSøknad : SøknadBygger {
    private val personalia
        get() = Seksjon(Rolle.søker, fødselsdato)
    private val statiske
        get() = Seksjon(
            Rolle.nav,
            inntekt3G,
            inntekt15G
        )
    private val datoer
        get() = Seksjon(
            Rolle.søker,
            virkningstidspunkt,
            datoForBortfallPgaAlder,
            dimisjonsdato
        )
    private val egenNæring
        get() = Seksjon(
            Rolle.søker,
            egenBondegård,
            egenBedrift,
            fangstOgFisk,
        )
    private val inntekter
        get() = Seksjon(
            Rolle.søker,
            inntektSisteÅr,
            inntektSiste3år,
        )
    private val reellArbeidssøker
        get() = Seksjon(
            Rolle.søker,
            villigDeltid,
            villigHelse,
            villigJobb,
            villigPendle,
        )

    override fun søknad(): Søknad =
        Søknad(
            statiske,
            personalia,
            reellArbeidssøker,
            datoer,
            egenNæring,
            inntekter
        )
}
