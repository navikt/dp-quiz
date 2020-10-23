package no.nav.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Rolle.søker
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.ikkeFør
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.søknad.Versjon

class Prototype {

    private val fakta: Fakta
        get() = Fakta(
            dato faktum "Fødselsdato" id 1,
            dato faktum "Ønsker dagpenger fra dato" id 2,
            dato faktum "Dato for bortfall på grunn av alder" id 3,
            maks dato "Virkningstidspunkt" av 2 og 6 og 5 og 7 id 4,

            dato faktum "Siste dag med arbeidsplikt" id 5,
            dato faktum "Registreringsdato" id 6,
            dato faktum "Siste dag med lønn" id 7,

            inntekt faktum "Inntekt siste 3 år" id 8,
            inntekt faktum "Inntekt siste 12 mnd" id 9,
            dato faktum "Dimisjonsdato" id 10,
            inntekt faktum "3G" id 11,
            inntekt faktum "1,5G" id 12,

            ja nei "Eier egen bondegård" id 13,
            ja nei "Eier egen bedrift" id 14,
            ja nei "Driver med fangst og fisk" id 15,

            ja nei "Villig til deltidsarbeid" id 16,
            ja nei "Villig til pendling" id 17,
            ja nei "Villig til helse" id 18,
            ja nei "Villig til å ta ethvert arbeid" id 19,
        )

    val fødselsdato = fakta dato 1
    val villigDeltid = fakta ja 16
    val villigPendle = fakta ja 17
    val villigHelse = fakta ja 18
    val villigJobb = fakta ja 19

    val ønsketDato = fakta dato 2
    val registreringsdato = fakta dato 6
    val sisteDagMedLønn = fakta dato 7
    val sisteDagMedArbeidsplikt = fakta dato 5

    val virkningstidspunkt = fakta dato 4

    val datoForBortfallPgaAlder = fakta dato 3
    val dimisjonsdato = fakta dato 10
    val inntektSiste3År = fakta inntekt 8
    val inntektSisteÅr = fakta inntekt 9
    val inntekt3G = fakta inntekt 11
    val inntekt15G = fakta inntekt 12

    val egenBondegård = fakta ja 13
    val egenBedrift = fakta ja 14
    val fangstOgFisk = fakta ja 15

    private val personalia = Seksjon("personalia", søker, fødselsdato)

    val inngangsvilkår =
        "Inngangsvilkår".alle(
            "reell arbeidssøker".alle(
                villigDeltid er true,
                villigPendle er true,
                villigHelse er true,
                villigJobb er true,
                virkningstidspunkt ikkeFør registreringsdato
            ),
            "alder".alle(
                virkningstidspunkt før datoForBortfallPgaAlder,
            )
        ) så (
            "resten".alle(
                "har ikke egen næring".alle(
                    egenBedrift er false,
                    egenBondegård er false,
                    fangstOgFisk er false
                ),
                "minste arbeidsinntekt".minstEnAv(
                    inntektSiste3År minst inntekt3G,
                    inntektSisteÅr minst inntekt15G,
                    dimisjonsdato før virkningstidspunkt
                ),
            )
            )

    private val statiske =
        Seksjon(
            "statiske",
            søker,
            inntekt3G,
            inntekt15G
        )

    private val datoer =
        Seksjon(
            "datoer",
            søker,
            virkningstidspunkt,
            datoForBortfallPgaAlder,
            dimisjonsdato
        )

    private val egenNæring =
        Seksjon(
            "egenNæring",
            søker,
            egenBondegård,
            egenBedrift,
            fangstOgFisk,
        )

    private val inntekter =
        Seksjon(
            "inntekter",
            søker,
            inntektSisteÅr,
            inntektSiste3År,
        )

    private val reellArbeidssøker =
        Seksjon(
            "reellArbeidssøker",
            søker,
            villigDeltid,
            villigHelse,
            villigJobb,
            villigPendle,
        )

    internal val søknad: Søknad =
        Søknad(
            statiske,
            reellArbeidssøker,
            personalia,
            datoer,
            egenNæring,
            inntekter
        )

    // this should not be in this class
    private val versjon = Versjon(1, fakta, inngangsvilkår, mapOf(Versjon.Type.Web to søknad))

    fun søknad(fnr: String) = versjon.søknad(fnr, Versjon.Type.Web)
}