package no.nav.dagpenger

import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.regelverk.dimisjonsdato
import no.nav.dagpenger.regelverk.egenBedrift
import no.nav.dagpenger.regelverk.egenBondegård
import no.nav.dagpenger.regelverk.fangstOgFisk
import no.nav.dagpenger.regelverk.fødselsdato
import no.nav.dagpenger.regelverk.inntektSiste3år
import no.nav.dagpenger.regelverk.inntektSisteÅr
import no.nav.dagpenger.regelverk.villigDeltid
import no.nav.dagpenger.regelverk.villigHelse
import no.nav.dagpenger.regelverk.villigJobb
import no.nav.dagpenger.regelverk.villigPendle
import no.nav.dagpenger.regelverk.virkningstidspunkt

internal class EnkelSøknad : SøknadBygger {
    private val personalia = Seksjon(Rolle.søker, fødselsdato)
    private val datoer = Seksjon(
        Rolle.søker,
        virkningstidspunkt,
        dimisjonsdato
    )
    private val egenNæring = Seksjon(
        Rolle.søker,
        egenBondegård,
        egenBedrift,
        fangstOgFisk,
    )
    private val inntekter = Seksjon(
        Rolle.søker,
        inntektSisteÅr,
        inntektSiste3år,
    )
    private val reellArbeidssøker = Seksjon(
        Rolle.søker,
        villigDeltid,
        villigHelse,
        villigJobb,
        villigPendle,
    )

    override fun søknad(): Søknad =
        Søknad(
            personalia,
            reellArbeidssøker,
            datoer,
            egenNæring,
            inntekter
        )
}
