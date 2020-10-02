package no.nav.dagpenger

import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad

internal class EnkelSøknad : SøknadBygger {
    lateinit var f: Dagpengefakta

    private val personalia
        get() = Seksjon(Rolle.søker, f.fødselsdato)

    private val statiske
        get() = Seksjon(
            Rolle.nav,
            f.inntekt3G,
            f.inntekt15G
        )
    private val datoer
        get() = Seksjon(
            Rolle.søker,
            f.virkningstidspunkt,
            f.datoForBortfallPgaAlder,
            f.dimisjonsdato
        )
    private val egenNæring
        get() = Seksjon(
            Rolle.søker,
            f.egenBondegård,
            f.egenBedrift,
            f.fangstOgFisk,
        )
    private val inntekter
        get() = Seksjon(
            Rolle.søker,
            f.inntektSisteÅr,
            f.inntektSiste3År,
        )
    private val reellArbeidssøker
        get() = Seksjon(
            Rolle.søker,
            f.villigDeltid,
            f.villigHelse,
            f.villigJobb,
            f.villigPendle,
        )

    override fun søknad(): Søknad {
        f = Dagpengefakta()
        return Søknad(
            statiske,
            reellArbeidssøker,
            personalia,
            datoer,
            egenNæring,
            inntekter
        )
    }
}
