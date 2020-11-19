package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.ikkeFør
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så
import no.nav.dagpenger.quiz.mediator.db.FaktumTable

// Forstår dagpengesøknaden
class Prototype {

    init {
        val faktumRecord: FaktumTable = FaktumTable(søknad, VERSJON_ID)
    }

    companion object {
        const val VERSJON_ID = 1
    }

    private val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
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

    val fødselsdato = søknad dato 1
    val villigDeltid = søknad ja 16
    val villigPendle = søknad ja 17
    val villigHelse = søknad ja 18
    val villigJobb = søknad ja 19

    val ønsketDato = søknad dato 2
    val registreringsdato = søknad dato 6
    val sisteDagMedLønn = søknad dato 7
    val sisteDagMedArbeidsplikt = søknad dato 5

    val virkningstidspunkt = søknad dato 4

    val datoForBortfallPgaAlder = søknad dato 3
    val dimisjonsdato = søknad dato 10
    val inntektSiste3År = søknad inntekt 8
    val inntektSisteÅr = søknad inntekt 9
    val inntekt3G = søknad inntekt 11
    val inntekt15G = søknad inntekt 12

    val egenBondegård = søknad ja 13
    val egenBedrift = søknad ja 14
    val fangstOgFisk = søknad ja 15

    private val personalia = Seksjon("personalia", Rolle.søker, fødselsdato)

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
            Rolle.søker,
            inntekt3G,
            inntekt15G
        )

    private val datoer =
        Seksjon(
            "datoer",
            Rolle.søker,
            virkningstidspunkt,
            datoForBortfallPgaAlder,
            dimisjonsdato
        )

    private val egenNæring =
        Seksjon(
            "egenNæring",
            Rolle.søker,
            egenBondegård,
            egenBedrift,
            fangstOgFisk,
        )

    private val inntekter =
        Seksjon(
            "inntekter",
            Rolle.søker,
            inntektSisteÅr,
            inntektSiste3År,
        )

    private val reellArbeidssøker =
        Seksjon(
            "reellArbeidssøker",
            Rolle.søker,
            villigDeltid,
            villigHelse,
            villigJobb,
            villigPendle,
        )

    internal val søknadprosess: Søknadprosess =
        Søknadprosess(
            statiske,
            reellArbeidssøker,
            personalia,
            datoer,
            egenNæring,
            inntekter
        )

    private val versjon = Versjon(
        prototypeSøknad = søknad,
        prototypeSubsumsjon = inngangsvilkår,
        prototypeUserInterfaces = mapOf(
            Versjon.UserInterfaceType.Web to søknadprosess
        )
    )

    fun søknadprosess(fnr: String) = versjon.søknadprosess(fnr, Versjon.UserInterfaceType.Web)
}
