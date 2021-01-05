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

    private val ønsketDatoFactory = dato faktum "Ønsker dagpenger fra dato" id 1

    private val sisteDagMedArbeidspliktFactory = dato faktum "Siste dag med arbeidsplikt" id 2
    private val sisteDagMedLønnFactory = dato faktum "Siste dag med lønn" id 3
    private val virkningstidspunktFactory = maks dato "Virkningstidspunkt" av 1 og 2 og 3 og 10 id 4
    private val fangstOgFiskFactory = boolsk faktum "Driver med fangst og fisk" id 5
    private val inntektSiste36mndFactory = inntekt faktum "Inntekt siste 36 mnd" id 6
    private val inntektSiste12mndFactory = inntekt faktum "Inntekt siste 12 mnd" id 7
    private val G3Factory = inntekt faktum "3G" id 8
    private val G1_5Factory = inntekt faktum "1,5G" id 9
    private val søknadstidspunktFactory = dato faktum "Søknadstidspunkt" id 10
    private val vernepliktFactory = boolsk faktum "Verneplikt" id 11
    private val godkjenningVirkningstidspunktFactory = boolsk faktum "Godjenning av virkingstidspunkt" id 12
    private val innsendtSøknadsIdFactory = dokument faktum "Innsendt søknadsId" id 14
    private val godkjenningFangstOgFiskFactory = boolsk faktum "Godkjenning av dokumentasjon for fangst og fisk" id 15
    private val registreringsperioderFactory =
        heltall faktum "Antall arbeidsøker registeringsperioder" id 16 genererer 18 og 19
    private val lærlingFactory = boolsk faktum "Lærling" id 17
    private val registrertArbeidsøkerPeriodeFomFactory = dato faktum "fom" id 18
    private val registrertArbeidsøkerPeriodeTomFactory = dato faktum "tom" id 19
    private val dagensDatoFactory = dato faktum "Dagens dato" id 20
    private val inntektsrapporteringsperiodeFomFactory = dato faktum "Inntektsrapporteringsperiode fra og med" id 21
    private val inntektsrapporteringsperiodeTomFactory = dato faktum "Inntektsrapporteringsperiode til og med" id 22
    private val sluttårsakerFactory = heltall faktum "Rettighetstype" id 23 genererer 27 og 24 og 25 og 26 avhengerAv 14
    private val årsåkPermittertFactory = boolsk faktum "Permittert" id 24
    private val årsakOrdinærFactory = boolsk faktum "Ordinær" id 25
    private val lønnsgarantiFactory = boolsk faktum "Lønnsgaranti" id 26
    private val permittertFiskeforedlingFactory = boolsk faktum "PermittertFiskeforedling" id 27
    private val godkjenningRettighetstypeFactory = boolsk faktum "Godkjenning rettighetstype" id 28

    internal val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            ønsketDatoFactory avhengerAv innsendtSøknadsIdFactory,
            sisteDagMedArbeidspliktFactory avhengerAv innsendtSøknadsIdFactory,
            sisteDagMedLønnFactory avhengerAv innsendtSøknadsIdFactory,
            maks dato "Virkningstidspunkt" av 1 og 2 og 3 og 10 id 4,
            fangstOgFiskFactory avhengerAv innsendtSøknadsIdFactory,
            inntektSiste36mndFactory avhengerAv 4 og fangstOgFiskFactory,
            inntektSiste12mndFactory avhengerAv 4 og fangstOgFiskFactory,
            G3Factory avhengerAv virkningstidspunktFactory,
            G1_5Factory avhengerAv virkningstidspunktFactory,
            søknadstidspunktFactory avhengerAv innsendtSøknadsIdFactory,
            vernepliktFactory avhengerAv innsendtSøknadsIdFactory,
            godkjenningVirkningstidspunktFactory avhengerAv virkningstidspunktFactory og dagensDatoFactory,
            innsendtSøknadsIdFactory,
            godkjenningFangstOgFiskFactory avhengerAv fangstOgFiskFactory,
            registreringsperioderFactory,
            lærlingFactory,
            registrertArbeidsøkerPeriodeFomFactory,
            registrertArbeidsøkerPeriodeTomFactory,
            dagensDatoFactory,
            inntektsrapporteringsperiodeFomFactory avhengerAv virkningstidspunktFactory,
            inntektsrapporteringsperiodeTomFactory avhengerAv virkningstidspunktFactory,
            sluttårsakerFactory,
            årsåkPermittertFactory,
            årsakOrdinærFactory,
            lønnsgarantiFactory,
            permittertFiskeforedlingFactory,
            godkjenningRettighetstypeFactory avhengerAv sluttårsakerFactory
        )
    private val ønsketDato = søknad dato ønsketDatoFactory
    private val sisteDagMedArbeidsplikt = søknad dato sisteDagMedArbeidspliktFactory
    private val sisteDagMedLønn = søknad dato sisteDagMedLønnFactory
    private val virkningstidspunkt = søknad dato 4
    private val fangstOgFisk = søknad boolsk fangstOgFiskFactory
    private val inntektSiste36mnd = søknad inntekt inntektSiste36mndFactory
    private val inntektSiste12mnd = søknad inntekt inntektSiste12mndFactory
    private val G3 = søknad inntekt G3Factory
    private val G1_5 = søknad inntekt G1_5Factory
    private val søknadstidspunkt = søknad dato søknadstidspunktFactory
    private val verneplikt = søknad boolsk vernepliktFactory
    private val godkjenningVirkningstidspunkt = søknad boolsk godkjenningVirkningstidspunktFactory
    private val godkjenningFangstOgFisk = søknad boolsk godkjenningFangstOgFiskFactory
    private val registreringsperioder = søknad generator registreringsperioderFactory
    private val lærling = søknad boolsk lærlingFactory
    private val registrertArbeidsøkerPeriodeFom = søknad dato registrertArbeidsøkerPeriodeFomFactory
    private val registrertArbeidsøkerPeriodeTom = søknad dato registrertArbeidsøkerPeriodeTomFactory
    private val dagensDato = søknad dato dagensDatoFactory
    private val inntektsrapporteringsperiodeFom = søknad dato inntektsrapporteringsperiodeFomFactory
    private val inntektsrapporteringsperiodeTom = søknad dato inntektsrapporteringsperiodeTomFactory
    private val sluttårsaker = søknad generator sluttårsakerFactory
    private val ordinær = søknad boolsk årsakOrdinærFactory
    private val permittert = søknad boolsk årsåkPermittertFactory
    private val lønnsgaranti = søknad boolsk lønnsgarantiFactory
    private val permittertFiskeforedling = søknad boolsk permittertFiskeforedlingFactory
    private val godkjenningRettighetstype = søknad boolsk godkjenningRettighetstypeFactory

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

    private val sjekkVirkningstidspunkt = "søker på riktig tidspunkt" makro (
        dagensDato ikkeFør virkningstidspunkt eller
            (dagensDato mellom inntektsrapporteringsperiodeFom og inntektsrapporteringsperiodeTom)
        )

    private val minsteArbeidsinntektMedVirkningstidspunkt =
        sjekkVirkningstidspunkt så (
            sjekkFangstOgFisk uansett (minsteArbeidsinntekt)
            )

    private val inngangsvilkår = "inngangsvilkår".alle(
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

    internal val søknadprosess: Søknadprosess =
        Søknadprosess(
            oppstart,
            grunnbeløp,
            datoer,
            inntektsunntak,
            fangstOgfisk,
            godkjennFangstOgFisk,
            inntekter,
            godkjennDato,
            arbeidsforholdNav,
            arbeidsforholdSaksbehandler
        )

    private val faktumNavBehov =
        FaktumNavBehov(
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
                21 to "InntektsrapporteringsperiodeFom",
                22 to "InntektsrapporteringsperiodeTom",
                23 to "Rettighetstype",
                24 to "Permittert",
                25 to "Ordinær",
                26 to "Lønnsgaranti",
                27 to "PermittertFiskeforedling",
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
