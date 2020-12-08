package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.marshalling.Språk
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.ikkeFør
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.regel.ugyldigGodkjentAv
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så
import no.nav.dagpenger.model.subsumsjon.uansett
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import java.nio.file.Path

// Forstår dagpengesøknaden
internal class AvslagPåMinsteinntekt {

    init {
        FaktumTable(søknad, VERSJON_ID)

        FaktumNavBehov(
            VERSJON_ID,
            mapOf(
                1 to "ØnskerDagpengerFraDato",
                2 to "SisteDagMedArbeidsplikt",
                3 to "SisteDagMedLønn",
                4 to "Virkningstidspunkt",
                5 to "FangstOgFiske",
                6 to "InntektSiste3År",
                7 to "InntektSiste12Mnd",
                8 to "3G",
                9 to "1_5G",
                10 to "Søknadstidspunkt",
                11 to "Verneplikt",
                14 to "InnsendtSøknadsId",
                16 to "Registreringsperioder",
                17 to "Lærling",
                20 to "DagensDato",
            )
        )
    }

    private companion object {
        const val VERSJON_ID = 2
    }

    internal val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            dato faktum "Ønsker dagpenger fra dato" id 1 avhengerAv 14,
            dato faktum "Siste dag med arbeidsplikt" id 2 avhengerAv 14,
            dato faktum "Siste dag med lønn" id 3 avhengerAv 14,
            maks dato "Virkningstidspunkt" av 1 og 2 og 3 og 10 id 4,
            ja nei "Driver med fangst og fisk" id 5 avhengerAv 14,
            inntekt faktum "Inntekt siste 36 mnd" id 6 avhengerAv 4 og 5,
            inntekt faktum "Inntekt siste 12 mnd" id 7 avhengerAv 4 og 5,
            inntekt faktum "3G" id 8 avhengerAv 4,
            inntekt faktum "1,5G" id 9 avhengerAv 4,
            dato faktum "Søknadstidspunkt" id 10 avhengerAv 14,
            ja nei "Verneplikt" id 11 avhengerAv 14,
            ja nei "Godjenning av virkingstidspunkt" id 12 avhengerAv 4 og 20,
            dokument faktum "Innsendt søknadsId" id 14,
            ja nei "Godkjenning av dokumentasjon for fangst og fisk" id 15 avhengerAv 5,
            heltall faktum "Antall arbeidsøker registeringsperioder" id 16 genererer 18 og 19,
            ja nei "Lærling" id 17,
            dato faktum "fom" id 18,
            dato faktum "tom" id 19,
            dato faktum "Dagens dato" id 20,
        )
    private val ønsketDato = søknad dato 1
    private val sisteDagMedArbeidsplikt = søknad dato 2
    private val sisteDagMedLønn = søknad dato 3
    private val virkningstidspunkt = søknad dato 4
    private val fangstOgFisk = søknad ja 5
    private val inntektSiste36mnd = søknad inntekt 6
    private val inntektSiste12mnd = søknad inntekt 7
    private val G3 = søknad inntekt 8
    private val G1_5 = søknad inntekt 9
    private val søknadstidspunkt = søknad dato 10
    private val verneplikt = søknad ja 11
    private val godkjenningVirkningstidspunkt = søknad ja 12
    private val godkjenningFangstOgFisk = søknad ja 15
    private val registreringsperioder = søknad generator 16
    private val lærling = søknad ja 17
    private val registrertArbeidsøkerPeriodeFom = søknad dato 18
    private val registrertArbeidsøkerPeriodeTom = søknad dato 19
    private val dagensDato = søknad dato 20

    private val minsteArbeidsinntekt = "minste arbeidsinntekt".minstEnAv(
        inntektSiste36mnd minst G3,
        inntektSiste12mnd minst G1_5,
        verneplikt er true,
        lærling er true
    ) ugyldigGodkjentAv godkjenningVirkningstidspunkt

    private val meldtSomArbeidssøker = registreringsperioder har "periode".makro(
        virkningstidspunkt mellom registrertArbeidsøkerPeriodeFom og registrertArbeidsøkerPeriodeTom
    )

    private val sjekkFangstOgFisk = "fangst og fisk er dokumentert" makro (
        fangstOgFisk er false ugyldigGodkjentAv godkjenningFangstOgFisk
        )

    private val minsteArbeidsinntektMedVirkningstidspunkt =
        dagensDato ikkeFør virkningstidspunkt så (
            sjekkFangstOgFisk uansett (minsteArbeidsinntekt)
            )

    private val inngangsvilkår = "inngangsvilkår".alle(
        minsteArbeidsinntektMedVirkningstidspunkt,
        meldtSomArbeidssøker
    )

    private val oppstart =
        Seksjon(
            "oppstart",
            Rolle.nav,
            dagensDato,
        )

    private val grunnbeløp =
        Seksjon(
            "grunnbeløp",
            Rolle.nav,
            G3,
            G1_5,
        )
    private val datoer =
        Seksjon(
            "datoer",
            Rolle.nav,
            ønsketDato,
            søknadstidspunkt,
            sisteDagMedArbeidsplikt,
            sisteDagMedLønn,
            registrertArbeidsøkerPeriodeFom,
            registrertArbeidsøkerPeriodeTom,
            registreringsperioder
        )
    private val inntektsunntak =
        Seksjon(
            "inntektsunntak",
            Rolle.nav,
            verneplikt,
            lærling
        )
    private val fangstOgfisk =
        Seksjon(
            "fangstOgFisk",
            Rolle.nav,
            fangstOgFisk,
        )
    private val inntekter =
        Seksjon(
            "inntekter",
            Rolle.nav,
            inntektSiste12mnd,
            inntektSiste36mnd,
        )
    private val godkjennDato =
        Seksjon(
            "godkjenn virkningstidspunkt",
            Rolle.saksbehandler,
            godkjenningVirkningstidspunkt
        )
    internal val søknadprosess: Søknadprosess =
        Søknadprosess(
            oppstart,
            grunnbeløp,
            datoer,
            inntektsunntak,
            fangstOgfisk,
            inntekter,
            godkjennDato
        )
    private val versjon = Versjon(
        prototypeSøknad = søknad,
        prototypeSubsumsjon = inngangsvilkår,
        prototypeUserInterfaces = mapOf(
            Versjon.UserInterfaceType.Web to søknadprosess
        )
    )

    fun søknadprosess(person: Person) = versjon.søknadprosess(person, Versjon.UserInterfaceType.Web)
}

fun main() {
    val søknad: Søknad = Søknad(
        2,
        dato faktum "Ønsker dagpenger fra dato" id 1 avhengerAv 14,
        dato faktum "Siste dag med arbeidsplikt" id 2 avhengerAv 14,
        dato faktum "Siste dag med lønn" id 3 avhengerAv 14,
        maks dato "Virkningstidspunkt" av 1 og 2 og 3 og 10 id 4,
        ja nei "Driver med fangst og fisk" id 5 avhengerAv 14,
        inntekt faktum "Inntekt siste 36 mnd" id 6 avhengerAv 4 og 5,
        inntekt faktum "Inntekt siste 12 mnd" id 7 avhengerAv 4 og 5,
        inntekt faktum "3G" id 8 avhengerAv 4,
        inntekt faktum "1,5G" id 9 avhengerAv 4,
        dato faktum "Søknadstidspunkt" id 10 avhengerAv 14,
        ja nei "Verneplikt" id 11 avhengerAv 14,
        ja nei "Godjenning av virkingstidspunkt" id 12 avhengerAv 4,
        dokument faktum "Innsendt søknadsId" id 14,
        ja nei "Godkjenning av dokumentasjon for fangst og fisk" id 15 avhengerAv 5,
        heltall faktum "Antall arbeidsøker registeringsperioder" id 16 genererer 18 og 19,
        ja nei "Lærling" id 17,
        dato faktum "fom" id 18,
        dato faktum "tom" id 19,
        dato faktum "Dagens dato" id 20,
    )

    val oversetter = Språk(versjonId = 2)
    val path = Path.of("resources/oversettelser.properties")
    val nøkler = søknad.map { oversetter.nøkkel(it) }
}
