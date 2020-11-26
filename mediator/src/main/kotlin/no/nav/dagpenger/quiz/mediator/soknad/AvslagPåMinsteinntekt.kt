package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
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
import no.nav.dagpenger.model.regel.ikkeFør
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
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
                3 to "Registreringsdato",
                4 to "SisteDagMedLønn",
                5 to "Virkningstidspunkt",
                6 to "FangstOgFisk",
                7 to "InntektSiste3År",
                8 to "InntektSiste12Mnd",
                9 to "3G",
                10 to "1_5G",
                11 to "Søknadstidspunkt",
                12 to "Verneplikt",
                14 to "GodkjenningDokumentasjonFangstOgFisk",
                15 to "InnsendtSøknadsId",
            )
        )
    }

    private companion object {
        const val VERSJON_ID = 2
    }

    private val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            dato faktum "Ønsker dagpenger fra dato" id 1 avhengerAv 15,
            dato faktum "Siste dag med arbeidsplikt" id 2 avhengerAv 15,
            dato faktum "Registreringsdato" id 3,
            dato faktum "Siste dag med lønn" id 4 avhengerAv 15,
            maks dato "Virkningstidspunkt" av 1 og 2 og 3 og 4 og 11 id 5,
            ja nei "Driver med fangst og fisk" id 6 avhengerAv 15,
            inntekt faktum "Inntekt siste 3 år" id 7 avhengerAv 5 og 6,
            inntekt faktum "Inntekt siste 12 mnd" id 8 avhengerAv 5 og 6,
            inntekt faktum "3G" id 9 avhengerAv 5,
            inntekt faktum "1,5G" id 10 avhengerAv 5,
            dato faktum "Søknadstidspunkt" id 11 avhengerAv 15,
            ja nei "Verneplikt" id 12 avhengerAv 15,
            ja nei "Godjenning av virkingstidspunkt" id 13 avhengerAv 5,
            dokument faktum "dokumentasjon for fangst og fisk" id 14 avhengerAv 6,
            dokument faktum "Innsendt søknadsId" id 15,
            ja nei "Godkjenning av dokumentasjon for fangst og fisk" id 16 avhengerAv 14,
        )
    private val ønsketDato = søknad dato 1
    private val registreringsdato = søknad dato 2
    private val sisteDagMedLønn = søknad dato 3
    private val sisteDagMedArbeidsplikt = søknad dato 4
    private val virkningstidspunkt = søknad dato 5
    private val fangstOgFisk = søknad ja 6
    private val inntektSiste3År = søknad inntekt 7
    private val inntektSisteÅr = søknad inntekt 8
    private val G3 = søknad inntekt 9
    private val G1_5 = søknad inntekt 10
    private val søknadstidspunkt = søknad dato 11
    private val verneplikt = søknad ja 12
    private val godkjenningVirkningstidspunkt = søknad ja 13
    private val dokumentasjonFangstOgFisk = søknad dokument 14
    private val godkjenningFangstOgFisk = søknad ja 16

    private val minsteArbeidsinntekt = "minste arbeidsinntekt".minstEnAv(
        inntektSiste3År minst G3,
        inntektSisteÅr minst G1_5,
        verneplikt er true
    )

    private val sjekkFangstOgFisk = "fangst og fisk er dokumentert" makro (
        fangstOgFisk er false eller (godkjenningFangstOgFisk av dokumentasjonFangstOgFisk)
        )

    private val inngangsvilkår =
        (
            (søknadstidspunkt ikkeFør virkningstidspunkt)
                godkjentAv godkjenningVirkningstidspunkt
            ) så (
            sjekkFangstOgFisk så (
                minsteArbeidsinntekt
                )
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
            registreringsdato,
            sisteDagMedArbeidsplikt,
            sisteDagMedLønn
        )
    private val vernepliktSeksjon =
        Seksjon(
            "verneplikt",
            Rolle.nav,
            verneplikt,
        )
    private val egenNæring =
        Seksjon(
            "egenNæring",
            Rolle.nav,
            fangstOgFisk,
        )
    private val fangstOgFiskSeksjon =
        Seksjon(
            "egenNæring",
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
            vernepliktSeksjon,
            egenNæring,
            fangstOgFiskSeksjon,
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
