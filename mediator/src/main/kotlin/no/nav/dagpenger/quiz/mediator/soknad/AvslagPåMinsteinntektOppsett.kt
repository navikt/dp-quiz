package no.nav.dagpenger.quiz.mediator.soknad

import mu.KotlinLogging
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.multiplikasjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.regeltre
import no.nav.dagpenger.quiz.mediator.soknad.Seksjoner.søknadprosess

// Forstår dagpengesøknaden
internal object AvslagPåMinsteinntektOppsett {
    private val logger = KotlinLogging.logger { }
    const val VERSJON_ID = 6

    fun registrer(registrer: (søknad: Søknad, versjonId: Int) -> Unit) {
        registrer(søknad, VERSJON_ID)
    }

    const val ønsketDato = 1
    const val sisteDagMedLønn = 3
    const val virkningstidspunkt = 4
    const val fangstOgFisk = 5
    const val inntektSiste36mnd = 6
    const val inntektSiste12mnd = 7
    const val minsteinntektfaktor36mnd = 8
    const val minsteinntektfaktor12mnd = 9
    const val minsteinntektsterskel36mnd = 10
    const val minsteinntektsterskel12mnd = 11
    const val grunnbeløp = 12
    const val søknadstidspunkt = 13
    const val verneplikt = 14
    // const val godkjenningSisteDagMedLønn = 15
    const val innsendtSøknadsId = 17
    const val registreringsperioder = 19
    const val lærling = 20
    const val registrertArbeidsøkerPeriodeFom = 21
    const val registrertArbeidsøkerPeriodeTom = 22
    const val behandlingsdato = 23
    const val inntektsrapporteringsperiodeFom = 24
    const val inntektsrapporteringsperiodeTom = 25
    const val antallEndredeArbeidsforhold = 26
    const val ordinær = 27
    const val permittert = 28
    const val lønnsgaranti = 29
    const val permittertFiskeforedling = 30
    // const val godkjenningSluttårsak = 31
    const val harHattDagpengerSiste36mnd = 32
    const val periodeOppbruktManuell = 33
    const val sykepengerSiste36mnd = 34
    const val svangerskapsrelaterteSykepengerManuell = 35
    const val fangstOgFiskManuell = 36
    const val eøsArbeid = 37
    const val eøsArbeidManuell = 38
    const val uhåndterbartVirkningstidspunktManuell = 39
    const val senesteMuligeVirkningstidspunkt = 40
    const val flereArbeidsforholdManuell = 41
    const val oppfyllerMinsteinntektManuell = 42
    const val harInntektNesteKalendermåned = 43
    const val inntektNesteKalendermånedManuell = 44

    internal val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            dato faktum "Ønsker dagpenger fra dato" id ønsketDato avhengerAv innsendtSøknadsId,
            dato faktum "Siste dag med lønn" id sisteDagMedLønn avhengerAv innsendtSøknadsId,
            maks dato "Virkningstidspunkt" av ønsketDato og sisteDagMedLønn og søknadstidspunkt id virkningstidspunkt,
            boolsk faktum "Driver med fangst og fisk" id fangstOgFisk avhengerAv innsendtSøknadsId,
            inntekt faktum "Inntekt siste 36 mnd" id inntektSiste36mnd avhengerAv virkningstidspunkt og fangstOgFisk,
            inntekt faktum "Inntekt siste 12 mnd" id inntektSiste12mnd avhengerAv virkningstidspunkt og fangstOgFisk,
            inntekt faktum "Grunnbeløp" id grunnbeløp avhengerAv virkningstidspunkt,
            desimaltall faktum "Øvre faktor" id minsteinntektfaktor36mnd avhengerAv virkningstidspunkt,
            desimaltall faktum "Nedre faktor" id minsteinntektfaktor12mnd avhengerAv virkningstidspunkt,
            multiplikasjon inntekt "Minsteinntektsterskel siste 36 mnd" av minsteinntektfaktor36mnd ganger grunnbeløp id minsteinntektsterskel36mnd,
            multiplikasjon inntekt "Minsteinntektsterskel siste 12 mnd" av minsteinntektfaktor12mnd ganger grunnbeløp id minsteinntektsterskel12mnd,
            dato faktum "Søknadstidspunkt" id søknadstidspunkt avhengerAv innsendtSøknadsId,
            boolsk faktum "Verneplikt" id verneplikt avhengerAv innsendtSøknadsId,
            // boolsk faktum "Godkjenning av siste dag med lønn" id godkjenningSisteDagMedLønn avhengerAv sisteDagMedLønn,
            dokument faktum "Innsendt søknadsId" id innsendtSøknadsId,
            heltall faktum "Antall arbeidsøker registeringsperioder" id registreringsperioder genererer registrertArbeidsøkerPeriodeFom og registrertArbeidsøkerPeriodeTom,
            boolsk faktum "Lærling" id lærling avhengerAv innsendtSøknadsId,
            dato faktum "fom" id registrertArbeidsøkerPeriodeFom,
            dato faktum "tom" id registrertArbeidsøkerPeriodeTom,
            dato faktum "Behandlingsdato" id behandlingsdato,
            dato faktum "Inntektsrapporteringsperiode fra og med" id inntektsrapporteringsperiodeFom avhengerAv virkningstidspunkt,
            dato faktum "Inntektsrapporteringsperiode til og med" id inntektsrapporteringsperiodeTom avhengerAv virkningstidspunkt,
            heltall faktum "sluttårsaker" id antallEndredeArbeidsforhold genererer ordinær og permittert og lønnsgaranti og permittertFiskeforedling avhengerAv innsendtSøknadsId,
            boolsk faktum "Permittert" id permittert,
            boolsk faktum "Ordinær" id ordinær,
            boolsk faktum "Lønnsgaranti" id lønnsgaranti,
            boolsk faktum "PermittertFiskeforedling" id permittertFiskeforedling,
            // boolsk faktum "Godkjenning sluttårsak" id godkjenningSluttårsak avhengerAv antallEndredeArbeidsforhold,
            boolsk faktum "Har hatt dagpenger siste 36mnd" id harHattDagpengerSiste36mnd avhengerAv virkningstidspunkt,
            boolsk faktum "Har brukt opp forrige dagpengeperiode" id periodeOppbruktManuell avhengerAv harHattDagpengerSiste36mnd,
            boolsk faktum "Sykepenger siste 36 mnd" id sykepengerSiste36mnd avhengerAv virkningstidspunkt,
            boolsk faktum "Svangerskapsrelaterte sykepenger" id svangerskapsrelaterteSykepengerManuell avhengerAv sykepengerSiste36mnd,
            boolsk faktum "Fangst og fisk manuell" id fangstOgFiskManuell avhengerAv fangstOgFisk,
            boolsk faktum "Har hatt inntekt/trygdeperioder fra EØS" id eøsArbeid avhengerAv innsendtSøknadsId,
            boolsk faktum "EØS arbeid manuell" id eøsArbeidManuell avhengerAv eøsArbeid,
            boolsk faktum "Ugyldig dato manuell" id uhåndterbartVirkningstidspunktManuell avhengerAv virkningstidspunkt,
            boolsk faktum "Flere arbeidsforhold manuell" id flereArbeidsforholdManuell avhengerAv antallEndredeArbeidsforhold,
            dato faktum "Grensedato 14 dager frem i tid" id senesteMuligeVirkningstidspunkt avhengerAv behandlingsdato,
            boolsk faktum "Oppfyller kravene til minste arbeidsinntekt, går til manuell" id oppfyllerMinsteinntektManuell,
            boolsk faktum "Har inntekt neste kalendermåned" id harInntektNesteKalendermåned avhengerAv virkningstidspunkt,
            boolsk faktum "Har inntekt neste kalendermåned, skal til manuell" id inntektNesteKalendermånedManuell
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
                minsteinntektfaktor36mnd to "ØvreTerskelFaktor",
                minsteinntektfaktor12mnd to "NedreTerskelFaktor",
                grunnbeløp to "Grunnbeløp",
                søknadstidspunkt to "Søknadstidspunkt",
                verneplikt to "Verneplikt",
                innsendtSøknadsId to "InnsendtSøknadsId",
                registreringsperioder to "Registreringsperioder",
                lærling to "Lærling",
                behandlingsdato to "Behandlingsdato",
                senesteMuligeVirkningstidspunkt to "SenesteMuligeVirkningstidspunkt",
                inntektsrapporteringsperiodeFom to "InntektsrapporteringsperiodeFom",
                inntektsrapporteringsperiodeTom to "InntektsrapporteringsperiodeTom",
                antallEndredeArbeidsforhold to "Rettighetstype",
                ordinær to "Ordinær",
                permittert to "Permittert",
                lønnsgaranti to "Lønnsgaranti",
                permittertFiskeforedling to "PermittertFiskeforedling",
                harHattDagpengerSiste36mnd to "HarHattDagpengerSiste36Mnd",
                sykepengerSiste36mnd to "SykepengerSiste36Måneder",
                eøsArbeid to "EØSArbeid",
                harInntektNesteKalendermåned to "HarRapportertInntektNesteMåned"
            )
        )

    private val versjon = Versjon.Bygger(
        prototypeSøknad = søknad,
        prototypeSubsumsjon = regeltre,
        prototypeUserInterfaces = mapOf(
            Versjon.UserInterfaceType.Web to søknadprosess
        ),
        faktumNavBehov = faktumNavBehov
    ).registrer().also {
        logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID \n\n\n\n" }
    }
}
