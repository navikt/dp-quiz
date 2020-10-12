package no.nav.dagpenger

import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad

internal class EnkelSøknad : SøknadBygger {
    lateinit var fakta: Dagpengefakta

    private val personalia
        get() = with(fakta) { Seksjon("personalia", Rolle.søker, fødselsdato) }

    private val statiske
        get() = with(fakta) {
            Seksjon(
                "statiske",
                Rolle.søker,
                inntekt3G,
                inntekt15G
            )
        }
    private val datoer
        get() = with(fakta) {
            Seksjon(
                "datoer",
                Rolle.søker,
                virkningstidspunkt,
                datoForBortfallPgaAlder,
                dimisjonsdato
            )
        }
    private val egenNæring
        get() = with(fakta) {
            Seksjon(
                "egenNæring",
                Rolle.søker,
                egenBondegård,
                egenBedrift,
                fangstOgFisk,
            )
        }
    private val inntekter
        get() = with(fakta) {
            Seksjon(
                "inntekter",
                Rolle.søker,
                inntektSisteÅr,
                inntektSiste3År,
            )
        }
    private val reellArbeidssøker
        get() = with(fakta) {
            Seksjon(
                "reellArbeidssøker",
                Rolle.søker,
                villigDeltid,
                villigHelse,
                villigJobb,
                villigPendle,
            )
        }

    override fun søknad(): Søknad {
        fakta = Dagpengefakta()
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
