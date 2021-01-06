package no.nav.dagpenger.quiz.mediator.soknad

import mu.KotlinLogging
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.ikkeFør
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.regel.ugyldigGodkjentAv
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.bareEnAv
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så
import no.nav.dagpenger.model.subsumsjon.uansett

// Forstår dagpengesøknaden
internal object AvslagPåMinsteinntekt {

    private val logger = KotlinLogging.logger { }
    const val VERSJON_ID = 2

    fun registrer(registrer: (søknad: Søknad, versjonId: Int) -> Unit) {
        registrer(søknad, VERSJON_ID)
    }

    const val ønsketDatoId = 1
    private val sisteDagMedArbeidspliktId = 2
    private val sisteDagMedLønnId = 3
    private val virkningstidspunktId = 4
    private val fangstOgFiskId = 5
    private val inntektSiste36mndId = 6
    private val inntektSiste12mndId = 7
    private val G3Id = 8
    private val G1_5Id = 9
    private val søknadstidspunktId = 10
    private val vernepliktId = 11
    private val godkjenningVirkningstidspunktId = 12
    private val innsendtSøknadsIdId = 14
    private val godkjenningFangstOgFiskId = 15
    private val registreringsperioderId = 16
    private val lærlingId = 17
    private val registrertArbeidsøkerPeriodeFomId = 18
    private val registrertArbeidsøkerPeriodeTomId = 19
    private val dagensDatoId = 20
    private val inntektsrapporteringsperiodeFomId = 21
    private val inntektsrapporteringsperiodeTomId = 22
    private val sluttårsakerId = 23
    private val ordinærId = 24
    private val permittertId = 25
    private val lønnsgarantiId = 26
    private val permittertFiskeforedlingId = 27
    private val godkjenningRettighetstypeId = 28
    private val harHattDagpengerSiste36mndId = 29
    private val periodeOppbruktId = 30

    internal val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            dato faktum "Ønsker dagpenger fra dato" id ønsketDatoId avhengerAv innsendtSøknadsIdId,
            dato faktum "Siste dag med arbeidsplikt" id sisteDagMedArbeidspliktId avhengerAv innsendtSøknadsIdId,
            dato faktum "Siste dag med lønn" id sisteDagMedLønnId avhengerAv innsendtSøknadsIdId,
            maks dato "Virkningstidspunkt" av ønsketDatoId og sisteDagMedArbeidspliktId og sisteDagMedLønnId og søknadstidspunktId id virkningstidspunktId,
            boolsk faktum "Driver med fangst og fisk" id fangstOgFiskId avhengerAv innsendtSøknadsIdId,
            inntekt faktum "Inntekt siste 36 mnd" id inntektSiste36mndId avhengerAv virkningstidspunktId og fangstOgFiskId,
            inntekt faktum "Inntekt siste 12 mnd" id inntektSiste12mndId avhengerAv virkningstidspunktId og fangstOgFiskId,
            inntekt faktum "3G" id G3Id avhengerAv virkningstidspunktId,
            inntekt faktum "1,5G" id G1_5Id avhengerAv virkningstidspunktId,
            dato faktum "Søknadstidspunkt" id søknadstidspunktId avhengerAv innsendtSøknadsIdId,
            boolsk faktum "Verneplikt" id vernepliktId avhengerAv innsendtSøknadsIdId,
            boolsk faktum "Godjenning av virkingstidspunkt" id godkjenningVirkningstidspunktId avhengerAv virkningstidspunktId og dagensDatoId,
            dokument faktum "Innsendt søknadsId" id innsendtSøknadsIdId,
            boolsk faktum "Godkjenning av dokumentasjon for fangst og fisk" id godkjenningFangstOgFiskId avhengerAv fangstOgFiskId,
            heltall faktum "Antall arbeidsøker registeringsperioder" id registreringsperioderId genererer registrertArbeidsøkerPeriodeFomId og registrertArbeidsøkerPeriodeTomId,
            boolsk faktum "Lærling" id lærlingId,
            dato faktum "fom" id registrertArbeidsøkerPeriodeFomId,
            dato faktum "tom" id registrertArbeidsøkerPeriodeTomId,
            dato faktum "Dagens dato" id dagensDatoId,
            dato faktum "Inntektsrapporteringsperiode fra og med" id inntektsrapporteringsperiodeFomId avhengerAv virkningstidspunktId,
            dato faktum "Inntektsrapporteringsperiode til og med" id inntektsrapporteringsperiodeTomId avhengerAv virkningstidspunktId,
            heltall faktum "Rettighetstype" id sluttårsakerId genererer ordinærId og permittertId og lønnsgarantiId og permittertFiskeforedlingId avhengerAv innsendtSøknadsIdId,
            boolsk faktum "Permittert" id permittertId,
            boolsk faktum "Ordinær" id ordinærId,
            boolsk faktum "Lønnsgaranti" id lønnsgarantiId,
            boolsk faktum "PermittertFiskeforedling" id permittertFiskeforedlingId,
            boolsk faktum "Godkjenning rettighetstype" id godkjenningRettighetstypeId avhengerAv sluttårsakerId,
            boolsk faktum "Har hatt dagpenger siste 36mnd" id harHattDagpengerSiste36mndId avhengerAv virkningstidspunktId,
            boolsk faktum "Har brukt opp forrige dagpengeperiode" id periodeOppbruktId avhengerAv harHattDagpengerSiste36mndId
        )
    private val ønsketDato = søknad dato ønsketDatoId
    private val sisteDagMedArbeidsplikt = søknad dato sisteDagMedArbeidspliktId
    private val sisteDagMedLønn = søknad dato sisteDagMedLønnId
    private val virkningstidspunkt = søknad dato virkningstidspunktId
    private val fangstOgFisk = søknad boolsk fangstOgFiskId
    private val inntektSiste36mnd = søknad inntekt inntektSiste36mndId
    private val inntektSiste12mnd = søknad inntekt inntektSiste12mndId
    private val G3 = søknad inntekt G3Id
    private val G1_5 = søknad inntekt G1_5Id
    private val søknadstidspunkt = søknad dato søknadstidspunktId
    private val verneplikt = søknad boolsk vernepliktId
    private val godkjenningVirkningstidspunkt = søknad boolsk godkjenningVirkningstidspunktId
    private val godkjenningFangstOgFisk = søknad boolsk godkjenningFangstOgFiskId
    private val registreringsperioder = søknad generator registreringsperioderId
    private val lærling = søknad boolsk lærlingId
    private val registrertArbeidsøkerPeriodeFom = søknad dato registrertArbeidsøkerPeriodeFomId
    private val registrertArbeidsøkerPeriodeTom = søknad dato registrertArbeidsøkerPeriodeTomId
    private val dagensDato = søknad dato dagensDatoId
    private val inntektsrapporteringsperiodeFom = søknad dato inntektsrapporteringsperiodeFomId
    private val inntektsrapporteringsperiodeTom = søknad dato inntektsrapporteringsperiodeTomId
    private val sluttårsaker = søknad generator sluttårsakerId
    private val ordinær = søknad boolsk ordinærId
    private val permittert = søknad boolsk permittertId
    private val lønnsgaranti = søknad boolsk lønnsgarantiId
    private val permittertFiskeforedling = søknad boolsk permittertFiskeforedlingId
    private val godkjenningRettighetstype = søknad boolsk godkjenningRettighetstypeId
    private val harHattDagpengerSiste36mnd = søknad boolsk harHattDagpengerSiste36mndId
    private val periodeOppbrukt = søknad boolsk periodeOppbruktId

    internal val rettighetstype = sluttårsaker med "sluttårsak".makro(
        "bare en av".bareEnAv(
            ordinær er true,
            permittertFiskeforedling er true,
            lønnsgaranti er true,
            permittert er true
        )
    )

    private val minsteArbeidsinntekt = "minste arbeidsinntekt".minstEnAv(
        inntektSiste36mnd minst G3,
        inntektSiste12mnd minst G1_5,
        verneplikt er true,
        lærling er true
    ).ugyldigGodkjentAv(godkjenningVirkningstidspunkt, godkjenningRettighetstype)

    private val meldtSomArbeidssøker = registreringsperioder har "periode".makro(
        virkningstidspunkt mellom registrertArbeidsøkerPeriodeFom og registrertArbeidsøkerPeriodeTom
    )

    private val sjekkFangstOgFisk = "fangst og fisk er dokumentert" makro (
        fangstOgFisk er false ugyldigGodkjentAv godkjenningFangstOgFisk
        )

    private val gjenopptak = "skal ha gjenopptak" makro (
        harHattDagpengerSiste36mnd er true så (periodeOppbrukt er true)
        )

    private val sjekkVirkningstidspunkt = "søker på riktig tidspunkt" makro (
        dagensDato ikkeFør virkningstidspunkt eller
            (dagensDato mellom inntektsrapporteringsperiodeFom og inntektsrapporteringsperiodeTom)
        )

    private val minsteArbeidsinntektMedVirkningstidspunkt =
        sjekkVirkningstidspunkt så (
            sjekkFangstOgFisk uansett (minsteArbeidsinntekt)
            )

    private val inngangsvilkår = gjenopptak eller "inngangsvilkår".alle(
        minsteArbeidsinntektMedVirkningstidspunkt,
        meldtSomArbeidssøker,
        rettighetstype
    )

    private val oppstart =
        Seksjon(
            "oppstart",
            Rolle.nav,
            dagensDato,
            inntektsrapporteringsperiodeFom,
            inntektsrapporteringsperiodeTom
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
    private val ytelseshistorikk =
        Seksjon(
            "ytelsehistorikk",
            Rolle.nav,
            harHattDagpengerSiste36mnd
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
    private val godkjennFangstOgFisk =
        Seksjon(
            "godkjenn fangst og fisk",
            Rolle.saksbehandler,
            godkjenningFangstOgFisk
        )

    internal val arbeidsforholdNav = Seksjon(
        "rettighetstype",
        Rolle.nav,
        sluttårsaker,
        ordinær,
        lønnsgaranti,
        permittertFiskeforedling,
        permittert,
    )

    internal val arbeidsforholdSaksbehandler = Seksjon(
        "godkjenn rettighetstype",
        Rolle.saksbehandler,
        godkjenningRettighetstype,
    )

    private val manuell =
        Seksjon(
            "manuell",
            Rolle.manuell,
            periodeOppbrukt,
        )

    internal val søknadprosess: Søknadprosess =
        Søknadprosess(
            oppstart,
            grunnbeløp,
            datoer,
            ytelseshistorikk,
            inntektsunntak,
            fangstOgfisk,
            godkjennFangstOgFisk,
            inntekter,
            godkjennDato,
            arbeidsforholdNav,
            arbeidsforholdSaksbehandler,
            manuell
        )

    private val faktumNavBehov =
        FaktumNavBehov(
            mapOf(
                ønsketDatoId to "ØnskerDagpengerFraDato",
                sisteDagMedArbeidspliktId to "SisteDagMedArbeidsplikt",
                sisteDagMedLønnId to "SisteDagMedLønn",
                virkningstidspunktId to "Virkningstidspunkt",
                fangstOgFiskId to "FangstOgFiske",
                inntektSiste36mndId to "InntektSiste3År",
                inntektSiste12mndId to "InntektSiste12Mnd",
                G3Id to "3G",
                G1_5Id to "1_5G",
                søknadstidspunktId to "Søknadstidspunkt",
                vernepliktId to "Verneplikt",
                innsendtSøknadsIdId to "InnsendtSøknadsId",
                registreringsperioderId to "Registreringsperioder",
                lærlingId to "Lærling",
                dagensDatoId to "DagensDato",
                inntektsrapporteringsperiodeFomId to "InntektsrapporteringsperiodeFom",
                inntektsrapporteringsperiodeTomId to "InntektsrapporteringsperiodeTom",
                sluttårsakerId to "Rettighetstype",
                ordinærId to "Ordinær",
                permittertId to "Permittert",
                lønnsgarantiId to "Lønnsgaranti",
                permittertFiskeforedlingId to "PermittertFiskeforedling",
                harHattDagpengerSiste36mndId to "HarHattDagpengerSiste36Mnd"
            )
        )
    private val versjon = Versjon.Bygger(
        prototypeSøknad = søknad,
        prototypeSubsumsjon = inngangsvilkår,
        prototypeUserInterfaces = mapOf(
            Versjon.UserInterfaceType.Web to søknadprosess
        ),
        faktumNavBehov = faktumNavBehov
    ).registrer().also {
        logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID \n\n\n\n" }
    }

    fun søknadprosess(person: Person) = versjon.søknadprosess(person, Versjon.UserInterfaceType.Web)
}
