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

    const val ønsketDato = 1
    const val sisteDagMedLønn = 3
    const val virkningstidspunkt = 4
    const val fangstOgFisk = 5
    const val inntektSiste36mnd = 6
    const val inntektSiste12mnd = 7
    const val G3 = 8
    const val G1_5 = 9
    const val søknadstidspunkt = 10
    const val verneplikt = 11
    const val godkjenningSisteDagMedLønn = 12
    const val innsendtSøknadsId = 14
    const val registreringsperioder = 16
    const val lærling = 17
    const val registrertArbeidsøkerPeriodeFom = 18
    const val registrertArbeidsøkerPeriodeTom = 19
    const val dagensDato = 20
    const val inntektsrapporteringsperiodeFom = 21
    const val inntektsrapporteringsperiodeTom = 22
    const val sluttårsaker = 23
    const val ordinær = 24
    const val permittert = 25
    const val lønnsgaranti = 26
    const val permittertFiskeforedling = 27
    const val godkjenningRettighetstype = 28
    const val harHattDagpengerSiste36mnd = 29
    const val periodeOppbrukt = 30
    const val sykepengerSiste36mnd = 31
    const val svangerskapsrelaterteSykepenger = 32
    const val fangstFiskManuell = 33

    internal val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            dato faktum "Ønsker dagpenger fra dato" id ønsketDato avhengerAv innsendtSøknadsId,
            dato faktum "Siste dag med lønn" id sisteDagMedLønn avhengerAv innsendtSøknadsId,
            maks dato "Virkningstidspunkt" av ønsketDato og sisteDagMedLønn og søknadstidspunkt id virkningstidspunkt,
            boolsk faktum "Driver med fangst og fisk" id fangstOgFisk avhengerAv innsendtSøknadsId,
            inntekt faktum "Inntekt siste 36 mnd" id inntektSiste36mnd avhengerAv virkningstidspunkt og fangstOgFisk,
            inntekt faktum "Inntekt siste 12 mnd" id inntektSiste12mnd avhengerAv virkningstidspunkt og fangstOgFisk,
            inntekt faktum "3G" id G3 avhengerAv virkningstidspunkt,
            inntekt faktum "1,5G" id G1_5 avhengerAv virkningstidspunkt,
            dato faktum "Søknadstidspunkt" id søknadstidspunkt avhengerAv innsendtSøknadsId,
            boolsk faktum "Verneplikt" id verneplikt avhengerAv innsendtSøknadsId,
            boolsk faktum "Godjenning av siste dag med lønn" id godkjenningSisteDagMedLønn avhengerAv sisteDagMedLønn og dagensDato,
            dokument faktum "Innsendt søknadsId" id innsendtSøknadsId,
            heltall faktum "Antall arbeidsøker registeringsperioder" id registreringsperioder genererer registrertArbeidsøkerPeriodeFom og registrertArbeidsøkerPeriodeTom,
            boolsk faktum "Lærling" id lærling,
            dato faktum "fom" id registrertArbeidsøkerPeriodeFom,
            dato faktum "tom" id registrertArbeidsøkerPeriodeTom,
            dato faktum "Dagens dato" id dagensDato,
            dato faktum "Inntektsrapporteringsperiode fra og med" id inntektsrapporteringsperiodeFom avhengerAv virkningstidspunkt,
            dato faktum "Inntektsrapporteringsperiode til og med" id inntektsrapporteringsperiodeTom avhengerAv virkningstidspunkt,
            heltall faktum "Rettighetstype" id sluttårsaker genererer ordinær og permittert og lønnsgaranti og permittertFiskeforedling avhengerAv innsendtSøknadsId,
            boolsk faktum "Permittert" id permittert,
            boolsk faktum "Ordinær" id ordinær,
            boolsk faktum "Lønnsgaranti" id lønnsgaranti,
            boolsk faktum "PermittertFiskeforedling" id permittertFiskeforedling,
            boolsk faktum "Godkjenning rettighetstype" id godkjenningRettighetstype avhengerAv sluttårsaker,
            boolsk faktum "Har hatt dagpenger siste 36mnd" id harHattDagpengerSiste36mnd avhengerAv virkningstidspunkt,
            boolsk faktum "Har brukt opp forrige dagpengeperiode" id periodeOppbrukt avhengerAv harHattDagpengerSiste36mnd,
            boolsk faktum "Sykepenger siste 36 mnd" id sykepengerSiste36mnd avhengerAv virkningstidspunkt,
            boolsk faktum "Svangerskapsrelaterte sykepenger" id svangerskapsrelaterteSykepenger avhengerAv svangerskapsrelaterteSykepenger,
            boolsk faktum "Fangst og fisk manuell" id fangstFiskManuell avhengerAv fangstOgFisk
        )
    internal val rettighetstype = with(søknad) {
        generator(sluttårsaker) med "sluttårsak".makro(
            "bare en av".bareEnAv(
                boolsk(ordinær) er true,
                boolsk(permittertFiskeforedling) er true,
                boolsk(lønnsgaranti) er true,
                boolsk(permittert) er true
            )
        )
    }
    private val minsteArbeidsinntekt = with(søknad) {
        "minste arbeidsinntekt".minstEnAv(
            inntekt(inntektSiste36mnd) minst inntekt(G3),
            inntekt(inntektSiste12mnd) minst inntekt(G1_5),
            boolsk(verneplikt) er true,
            boolsk(lærling) er true
        ).ugyldigGodkjentAv(boolsk(godkjenningSisteDagMedLønn), boolsk(godkjenningRettighetstype))
    }
    private val meldtSomArbeidssøker = with(søknad) {
        generator(registreringsperioder) har "periode".makro(
            dato(virkningstidspunkt) mellom dato(registrertArbeidsøkerPeriodeFom) og
                dato(registrertArbeidsøkerPeriodeTom)
        )
    }
    private val sjekkFangstOgFisk = with(søknad) {
        "fangst og fisk er dokumentert" makro (
            boolsk(fangstOgFisk) er true så (boolsk(fangstFiskManuell) er true)
            )
    }
    private val gjenopptak = with(søknad) {
        "skal ha gjenopptak" makro (
            boolsk(harHattDagpengerSiste36mnd) er true så (boolsk(periodeOppbrukt) er true)
            )
    }
    private val sjekkSykepenger = with(søknad) {
        "svangerskapsrelaterte sykepenger" makro (
            boolsk(sykepengerSiste36mnd) er false eller (boolsk(svangerskapsrelaterteSykepenger) er true)
            )
    }
    private val sjekkVirkningstidspunkt = with(søknad) {
        "søker på riktig tidspunkt" makro (
            dato(dagensDato) ikkeFør dato(virkningstidspunkt) eller (
                dato(dagensDato) mellom dato(inntektsrapporteringsperiodeFom) og dato(inntektsrapporteringsperiodeTom)
                )
            )
    }
    private val minsteArbeidsinntektMedVirkningstidspunkt =
        sjekkFangstOgFisk uansett (sjekkSykepenger så minsteArbeidsinntekt)

    private val inngangsvilkår = sjekkVirkningstidspunkt så (
        gjenopptak eller "inngangsvilkår".alle(
            minsteArbeidsinntektMedVirkningstidspunkt,
            meldtSomArbeidssøker,
            rettighetstype
        )
        )

    private val oppstart = with(søknad) {
        Seksjon(
            "oppstart",
            Rolle.nav,
            dato(dagensDato),
            dato(inntektsrapporteringsperiodeFom),
            dato(inntektsrapporteringsperiodeTom)
        )
    }
    private val grunnbeløp = with(søknad) {
        Seksjon(
            "grunnbeløp",
            Rolle.nav,
            inntekt(G3),
            inntekt(G1_5),
        )
    }
    private val datoer = with(søknad) {
        Seksjon(
            "datoer",
            Rolle.nav,
            dato(ønsketDato),
            dato(søknadstidspunkt),
            dato(sisteDagMedLønn),
            dato(registrertArbeidsøkerPeriodeFom),
            dato(registrertArbeidsøkerPeriodeTom),
            generator(registreringsperioder)
        )
    }
    private val ytelseshistorikk = with(søknad) {
        Seksjon(
            "ytelsehistorikk",
            Rolle.nav,
            boolsk(harHattDagpengerSiste36mnd),
            boolsk(sykepengerSiste36mnd)
        )
    }
    private val inntektsunntak = with(søknad) {
        Seksjon(
            "inntektsunntak",
            Rolle.nav,
            boolsk(verneplikt),
            boolsk(lærling)
        )
    }
    private val fangstOgfisk = with(søknad) {
        Seksjon(
            "fangstOgFisk",
            Rolle.nav,
            boolsk(fangstOgFisk),
        )
    }
    private val inntekter = with(søknad) {
        Seksjon(
            "inntekter",
            Rolle.nav,
            inntekt(inntektSiste12mnd),
            inntekt(inntektSiste36mnd),
        )
    }
    private val godkjennDato = with(søknad) {
        Seksjon(
            "godkjenn virkningstidspunkt",
            Rolle.saksbehandler,
            boolsk(godkjenningSisteDagMedLønn)
        )
    }
    internal val arbeidsforholdNav = with(søknad) {
        Seksjon(
            "rettighetstype",
            Rolle.nav,
            generator(sluttårsaker),
            boolsk(ordinær),
            boolsk(lønnsgaranti),
            boolsk(permittertFiskeforedling),
            boolsk(permittert),
        )
    }
    internal val arbeidsforholdSaksbehandler = with(søknad) {
        Seksjon(
            "godkjenn rettighetstype",
            Rolle.saksbehandler,
            boolsk(godkjenningRettighetstype),
        )
    }
    private val manuellGjenopptak = with(søknad) {
        Seksjon(
            "mulig gjenopptak",
            Rolle.manuell,
            boolsk(periodeOppbrukt),
        )
    }
    private val manuellSykepenger = with(søknad) {
        Seksjon(
            "svangerskapsrelaterte sykepenger",
            Rolle.manuell,
            boolsk(svangerskapsrelaterteSykepenger)
        )
    }
    private val manuellFangstOgFisk = with(søknad) {
        Seksjon(
            "fangst og fisk manuell",
            Rolle.manuell,
            boolsk(fangstFiskManuell)
        )
    }
    internal val søknadprosess: Søknadprosess =
        Søknadprosess(
            oppstart,
            grunnbeløp,
            datoer,
            ytelseshistorikk,
            inntektsunntak,
            fangstOgfisk,
            inntekter,
            godkjennDato,
            arbeidsforholdNav,
            arbeidsforholdSaksbehandler,
            manuellGjenopptak,
            manuellSykepenger,
            manuellFangstOgFisk
        )
    private val faktumNavBehov =
        FaktumNavBehov(
            mapOf(
                ønsketDato to "ØnskerDagpengerFraDato",
                sisteDagMedLønn to "SisteDagMedLønn",
                virkningstidspunkt to "Virkningstidspunkt",
                fangstOgFisk to "FangstOgFiske",
                inntektSiste36mnd to "InntektSiste3År",
                inntektSiste12mnd to "InntektSiste12Mnd",
                G3 to "3G",
                G1_5 to "1_5G",
                søknadstidspunkt to "Søknadstidspunkt",
                verneplikt to "Verneplikt",
                innsendtSøknadsId to "InnsendtSøknadsId",
                registreringsperioder to "Registreringsperioder",
                lærling to "Lærling",
                dagensDato to "DagensDato",
                inntektsrapporteringsperiodeFom to "InntektsrapporteringsperiodeFom",
                inntektsrapporteringsperiodeTom to "InntektsrapporteringsperiodeTom",
                sluttårsaker to "Rettighetstype",
                ordinær to "Ordinær",
                permittert to "Permittert",
                lønnsgaranti to "Lønnsgaranti",
                permittertFiskeforedling to "PermittertFiskeforedling",
                harHattDagpengerSiste36mnd to "HarHattDagpengerSiste36Mnd",
                sykepengerSiste36mnd to "SykepengerSiste36Måneder"
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
