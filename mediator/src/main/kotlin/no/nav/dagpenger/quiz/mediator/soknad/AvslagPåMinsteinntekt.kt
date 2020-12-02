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
import no.nav.dagpenger.model.regel.av
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.godkjentAv
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.ikkeFør
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så
import no.nav.dagpenger.quiz.mediator.db.FaktumTable

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
                13 to "GodkjenningDokumentasjonFangstOgFisk",
                14 to "InnsendtSøknadsId",
                16 to "Registreringsperioder"
            )
        )
    }

    private companion object {
        const val VERSJON_ID = 2
    }

    private val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            dato faktum "Ønsker dagpenger fra dato" id 1 avhengerAv 14,
            dato faktum "Siste dag med arbeidsplikt" id 2 avhengerAv 14,
            dato faktum "Siste dag med lønn" id 3 avhengerAv 14,
            maks dato "Virkningstidspunkt" av 1 og 2 og 3 og 10 id 4,
            ja nei "Driver med fangst og fisk" id 5 avhengerAv 14,
            inntekt faktum "Inntekt siste 3 år" id 6 avhengerAv 4 og 5,
            inntekt faktum "Inntekt siste 12 mnd" id 7 avhengerAv 4 og 5,
            inntekt faktum "3G" id 8 avhengerAv 4,
            inntekt faktum "1,5G" id 9 avhengerAv 4,
            dato faktum "Søknadstidspunkt" id 10 avhengerAv 14,
            ja nei "Verneplikt" id 11 avhengerAv 14,
            ja nei "Godjenning av virkingstidspunkt" id 12 avhengerAv 4,
            dokument faktum "dokumentasjon for fangst og fisk" id 13 avhengerAv 5,
            dokument faktum "Innsendt søknadsId" id 14,
            ja nei "Godkjenning av dokumentasjon for fangst og fisk" id 15 avhengerAv 13,
            heltall faktum "Antall arbeidsøker registeringsperioder" id 16 genererer 18 og 19,
            ja nei "Lærling" id 17,
            dato faktum "fom" id 18,
            dato faktum "tom" id 19,

        )
    private val ønsketDato = søknad dato 1
    private val sisteDagMedArbeidsplikt = søknad dato 2
    private val sisteDagMedLønn = søknad dato 3
    private val virkningstidspunkt = søknad dato 4
    private val fangstOgFisk = søknad ja 5
    private val inntektSiste3År = søknad inntekt 6
    private val inntektSisteÅr = søknad inntekt 7
    private val G3 = søknad inntekt 8
    private val G1_5 = søknad inntekt 9
    private val søknadstidspunkt = søknad dato 10
    private val verneplikt = søknad ja 11
    private val godkjenningVirkningstidspunkt = søknad ja 12
    private val dokumentasjonFangstOgFisk = søknad dokument 13
    private val godkjenningFangstOgFisk = søknad ja 15
    private val registreringsperioder = søknad generator 16
    private val lærling = søknad ja 17
    private val registrertArbeidsøkerPeriodeFom = søknad dato 18
    private val registrertArbeidsøkerPeriodeTom = søknad dato 19

    private val minsteArbeidsinntekt = "minste arbeidsinntekt".minstEnAv(
        inntektSiste3År minst G3,
        inntektSisteÅr minst G1_5,
        verneplikt er true,
        lærling er true
    )


    val meldtSomArbeidssøker = registreringsperioder har "periode".makro(
        ønsketDato mellom registrertArbeidsøkerPeriodeFom og registrertArbeidsøkerPeriodeTom
    )


    private val sjekkFangstOgFisk = "fangst og fisk er dokumentert" makro (
        fangstOgFisk er false eller (godkjenningFangstOgFisk av dokumentasjonFangstOgFisk)
        )

    private val minsteArbeidsInntektMedVirkningstidspunkt =
        ((søknadstidspunkt ikkeFør virkningstidspunkt) godkjentAv godkjenningVirkningstidspunkt) så (sjekkFangstOgFisk så (minsteArbeidsinntekt))

    private val inngangsvilkår = "inngangsvilkår".alle(
        minsteArbeidsInntektMedVirkningstidspunkt,
        meldtSomArbeidssøker
    )

    private val statiske =
        Seksjon(
            "statiske",
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
    private val fangstOgFiskDokumentasjon =
        Seksjon(
            "fangstOgFiskDokumentasjon",
            Rolle.nav,
            dokumentasjonFangstOgFisk
        )
    private val inntekter =
        Seksjon(
            "inntekter",
            Rolle.nav,
            inntektSisteÅr,
            inntektSiste3År,
        )
    private val godkjennDato =
        Seksjon(
            "godkjenn virkningstidspunkt",
            Rolle.saksbehandler,
            godkjenningVirkningstidspunkt
        )
    internal val søknadprosess: Søknadprosess =
        Søknadprosess(
            statiske,
            datoer,
            inntektsunntak,
            fangstOgfisk,
            fangstOgFiskDokumentasjon,
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
