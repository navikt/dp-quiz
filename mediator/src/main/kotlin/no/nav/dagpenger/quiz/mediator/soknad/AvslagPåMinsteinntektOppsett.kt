package no.nav.dagpenger.quiz.mediator.soknad

import mu.KotlinLogging
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.regeltre
import no.nav.dagpenger.quiz.mediator.soknad.Seksjoner.søknadprosess

// Forstår dagpengesøknaden
internal object AvslagPåMinsteinntektOppsett {
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
    const val antallEndredeArbeidsforhold = 23
    const val ordinær = 24
    const val permittert = 25
    const val lønnsgaranti = 26
    const val permittertFiskeforedling = 27
    const val godkjenningSluttårsak = 28
    const val harHattDagpengerSiste36mnd = 29
    const val periodeOppbruktManuell = 30
    const val sykepengerSiste36mnd = 31
    const val svangerskapsrelaterteSykepengerManuell = 32
    const val fangstOgFiskManuell = 33
    const val eøsArbeid = 34
    const val eøsArbeidManuell = 35
    const val uhåndterbartVirkningstidspunktManuell = 36
    const val senesteMuligeVirkningstidspunkt = 37
    const val flereArbeidsforholdManuell = 38

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
            boolsk faktum "Lærling" id lærling avhengerAv innsendtSøknadsId,
            dato faktum "fom" id registrertArbeidsøkerPeriodeFom,
            dato faktum "tom" id registrertArbeidsøkerPeriodeTom,
            dato faktum "Dagens dato" id dagensDato,
            dato faktum "Inntektsrapporteringsperiode fra og med" id inntektsrapporteringsperiodeFom avhengerAv virkningstidspunkt,
            dato faktum "Inntektsrapporteringsperiode til og med" id inntektsrapporteringsperiodeTom avhengerAv virkningstidspunkt,
            heltall faktum "sluttårsaker" id antallEndredeArbeidsforhold genererer ordinær og permittert og lønnsgaranti og permittertFiskeforedling avhengerAv innsendtSøknadsId,
            boolsk faktum "Permittert" id permittert,
            boolsk faktum "Ordinær" id ordinær,
            boolsk faktum "Lønnsgaranti" id lønnsgaranti,
            boolsk faktum "PermittertFiskeforedling" id permittertFiskeforedling,
            boolsk faktum "Godkjenning sluttårsak" id godkjenningSluttårsak avhengerAv ordinær og permittert og permittertFiskeforedling og lønnsgaranti,
            boolsk faktum "Har hatt dagpenger siste 36mnd" id harHattDagpengerSiste36mnd avhengerAv virkningstidspunkt,
            boolsk faktum "Har brukt opp forrige dagpengeperiode" id periodeOppbruktManuell avhengerAv harHattDagpengerSiste36mnd,
            boolsk faktum "Sykepenger siste 36 mnd" id sykepengerSiste36mnd avhengerAv virkningstidspunkt,
            boolsk faktum "Svangerskapsrelaterte sykepenger" id svangerskapsrelaterteSykepengerManuell avhengerAv sykepengerSiste36mnd,
            boolsk faktum "Fangst og fisk manuell" id fangstOgFiskManuell avhengerAv fangstOgFisk,
            boolsk faktum "Har hatt inntekt/trygdeperioder fra EØS" id eøsArbeid avhengerAv innsendtSøknadsId,
            boolsk faktum "EØS arbeid manuell" id eøsArbeidManuell avhengerAv eøsArbeid,
            boolsk faktum "Ugyldig dato manuell" id uhåndterbartVirkningstidspunktManuell avhengerAv virkningstidspunkt,
            boolsk faktum "Flere arbeidsforhold manuell" id flereArbeidsforholdManuell avhengerAv antallEndredeArbeidsforhold,
            dato faktum "Grensedato 14 dager frem i tid" id senesteMuligeVirkningstidspunkt avhengerAv dagensDato
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
                eøsArbeid to "EØSArbeid"
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
