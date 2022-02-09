package no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt

import mu.KotlinLogging
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.min
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.multiplikasjon
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntekt.regeltre
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.Seksjoner.søknadprosess

// Forstår dagpengesøknaden
internal object AvslagPåMinsteinntektOppsett {
    private val logger = KotlinLogging.logger { }
    val VERSJON_ID = Prosessversjon(Prosess.AvslagPåMinsteinntekt, 26)

    fun registrer(registrer: (søknad: Søknad) -> Unit) {
        registrer(søknad)
    }

    const val ønsketDato = 1
    const val virkningsdato = 4
    const val fangstOgFiskInntektSiste36mnd = 5
    const val inntektSiste36mnd = 6
    const val inntektSiste12mnd = 7
    const val minsteinntektfaktor36mnd = 8
    const val minsteinntektfaktor12mnd = 9
    const val minsteinntektsterskel36mnd = 10
    const val minsteinntektsterskel12mnd = 11
    const val grunnbeløp = 12
    const val søknadstidspunkt = 13
    const val verneplikt = 14
    const val innsendtSøknadsId = 17
    const val registrertArbeidssøkerPerioder = 19
    const val lærling = 20
    const val registrertArbeidssøkerPeriodeFom = 21
    const val registrertArbeidssøkerPeriodeTom = 22
    const val behandlingsdato = 23
    const val inntektsrapporteringsperiodeTom = 25
    const val antallEndredeArbeidsforhold = 26
    const val ordinær = 27
    const val permittert = 28
    const val lønnsgaranti = 29
    const val permittertFiskeforedling = 30
    const val harHattDagpengerSiste36mnd = 32
    const val periodeOppbruktManuell = 33
    const val sykepengerSiste36mnd = 34
    const val svangerskapsrelaterteSykepengerManuell = 35
    const val fangstOgFiskManuell = 36
    const val eøsArbeid = 37
    const val eøsArbeidManuell = 38
    const val uhåndterbartVirkningsdatoManuell = 39
    const val senesteMuligeVirkningsdato = 40
    const val oppfyllerMinsteinntektManuell = 42
    const val harInntektNesteKalendermåned = 43
    const val inntektNesteKalendermånedManuell = 44
    const val førsteAvVirkningsdatoOgBehandlingsdato = 45
    const val kanJobbeDeltid = 46
    const val helseTilAlleTyperJobb = 47
    const val kanJobbeHvorSomHelst = 48
    const val villigTilÅBytteYrke = 49
    const val reellArbeidssøkerManuell = 50
    const val registrertArbeidssøkerManuell = 51
    const val arenaFagsakId = 52
    const val fortsattRettKorona = 53
    const val fortsattRettKoronaManuell = 54
    const val over67årFradato = 55
    const val over67årManuell = 56
    const val jobbetUtenforNorge = 57
    const val jobbetUtenforNorgeManuell = 58
    const val hattLukkedeSakerSiste8Uker = 59
    const val hattLukkedeSakerSiste8UkerManuell = 60

    internal val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            dato faktum "Ønsker dagpenger fra dato" id ønsketDato avhengerAv innsendtSøknadsId,
            maks dato "Virkningsdato" av ønsketDato og søknadstidspunkt id virkningsdato,
            boolsk faktum "Har hatt inntekt fra fangst og fisk siste 36 mnd" id fangstOgFiskInntektSiste36mnd avhengerAv virkningsdato,
            inntekt faktum "Inntekt siste 36 mnd" id inntektSiste36mnd avhengerAv virkningsdato og fangstOgFiskInntektSiste36mnd,
            inntekt faktum "Inntekt siste 12 mnd" id inntektSiste12mnd avhengerAv virkningsdato og fangstOgFiskInntektSiste36mnd,
            inntekt faktum "Grunnbeløp" id grunnbeløp avhengerAv virkningsdato,
            desimaltall faktum "Øvre faktor" id minsteinntektfaktor36mnd avhengerAv virkningsdato,
            desimaltall faktum "Nedre faktor" id minsteinntektfaktor12mnd avhengerAv virkningsdato,
            multiplikasjon inntekt "Minsteinntektsterskel siste 36 mnd" av minsteinntektfaktor36mnd ganger grunnbeløp id minsteinntektsterskel36mnd,
            multiplikasjon inntekt "Minsteinntektsterskel siste 12 mnd" av minsteinntektfaktor12mnd ganger grunnbeløp id minsteinntektsterskel12mnd,
            dato faktum "Søknadstidspunkt" id søknadstidspunkt avhengerAv innsendtSøknadsId,
            boolsk faktum "Verneplikt" id verneplikt avhengerAv innsendtSøknadsId,
            dokument faktum "Innsendt søknadsId" id innsendtSøknadsId,
            heltall faktum "Antall arbeidsøker registeringsperioder" id registrertArbeidssøkerPerioder genererer registrertArbeidssøkerPeriodeFom og registrertArbeidssøkerPeriodeTom,
            boolsk faktum "Lærling" id lærling avhengerAv innsendtSøknadsId,
            dato faktum "fom" id registrertArbeidssøkerPeriodeFom,
            dato faktum "tom" id registrertArbeidssøkerPeriodeTom,
            dato faktum "Behandlingsdato" id behandlingsdato,
            heltall faktum "Antall endrede arbeidsforhold" id antallEndredeArbeidsforhold genererer ordinær og permittert og lønnsgaranti og permittertFiskeforedling avhengerAv innsendtSøknadsId,
            boolsk faktum "Permittert" id permittert,
            boolsk faktum "Ordinær" id ordinær,
            boolsk faktum "Lønnsgaranti" id lønnsgaranti,
            boolsk faktum "PermittertFiskeforedling" id permittertFiskeforedling,
            boolsk faktum "Har hatt dagpenger siste 36mnd" id harHattDagpengerSiste36mnd avhengerAv virkningsdato,
            boolsk faktum "Har brukt opp forrige dagpengeperiode" id periodeOppbruktManuell avhengerAv harHattDagpengerSiste36mnd,
            boolsk faktum "Sykepenger siste 36 mnd" id sykepengerSiste36mnd avhengerAv virkningsdato,
            boolsk faktum "Svangerskapsrelaterte sykepenger" id svangerskapsrelaterteSykepengerManuell avhengerAv sykepengerSiste36mnd,
            boolsk faktum "Fangst og fisk manuell" id fangstOgFiskManuell avhengerAv fangstOgFiskInntektSiste36mnd,
            boolsk faktum "Har hatt inntekt/trygdeperioder fra EØS" id eøsArbeid avhengerAv innsendtSøknadsId,
            boolsk faktum "EØS arbeid manuell" id eøsArbeidManuell avhengerAv eøsArbeid,
            boolsk faktum "Ugyldig dato manuell" id uhåndterbartVirkningsdatoManuell avhengerAv virkningsdato,
            dato faktum "Grensedato 14 dager frem i tid" id senesteMuligeVirkningsdato avhengerAv behandlingsdato,
            boolsk faktum "Oppfyller kravene til minste arbeidsinntekt, går til manuell" id oppfyllerMinsteinntektManuell,
            boolsk faktum "Har inntekt neste kalendermåned" id harInntektNesteKalendermåned avhengerAv virkningsdato,
            boolsk faktum "Har inntekt neste kalendermåned, skal til manuell" id inntektNesteKalendermånedManuell,
            min dato "Første dato av virkningsdato og behandlingsdato" id førsteAvVirkningsdatoOgBehandlingsdato av virkningsdato og behandlingsdato,
            boolsk faktum "Har mulighet til å jobbe heltid og deltid" id kanJobbeDeltid avhengerAv innsendtSøknadsId,
            boolsk faktum "Har ingen helsemessige begrensninger for arbeid" id helseTilAlleTyperJobb avhengerAv innsendtSøknadsId,
            boolsk faktum "Har mulighet til å jobbe hvor som helst" id kanJobbeHvorSomHelst avhengerAv innsendtSøknadsId,
            boolsk faktum "Er villig til å bytte yrke eller gå ned i lønn" id villigTilÅBytteYrke avhengerAv innsendtSøknadsId,
            boolsk faktum "Reell arbeidssøker manuell" id reellArbeidssøkerManuell avhengerAv kanJobbeDeltid og helseTilAlleTyperJobb og kanJobbeHvorSomHelst og villigTilÅBytteYrke,
            boolsk faktum "Registrert arbeidssøker manuell" id registrertArbeidssøkerManuell avhengerAv registrertArbeidssøkerPerioder,
            dato faktum "Inntektsrapporteringsperiode til og med" id inntektsrapporteringsperiodeTom avhengerAv behandlingsdato,
            dokument faktum "FagsakId i Arena" id arenaFagsakId,
            boolsk faktum "Har fortsatt rett til dagpenger i korona-periode" id fortsattRettKorona,
            boolsk faktum "Fortsatt rett korona manuell" id fortsattRettKoronaManuell,
            dato faktum "Over 67 år fra-dato" id over67årFradato,
            boolsk faktum "Over 67 år manuell" id over67årManuell,
            boolsk faktum "Har jobbet utenfor Norge" id jobbetUtenforNorge avhengerAv innsendtSøknadsId,
            boolsk faktum "Har jobbet utenfor Norge manuell" id jobbetUtenforNorgeManuell avhengerAv jobbetUtenforNorge,
            boolsk faktum "Har hatt lukkede saker siste 8 uker" id hattLukkedeSakerSiste8Uker avhengerAv virkningsdato,
            boolsk faktum "Har hatt lukkede saker siste 8 uker manuell" id hattLukkedeSakerSiste8UkerManuell avhengerAv hattLukkedeSakerSiste8Uker
        )

    private val faktumNavBehov =
        FaktumNavBehov(
            mapOf(
                ønsketDato to "ØnskerDagpengerFraDato",
                virkningsdato to "Virkningstidspunkt",
                fangstOgFiskInntektSiste36mnd to "FangstOgFiskeInntektSiste36mnd",
                inntektSiste36mnd to "InntektSiste3År",
                inntektSiste12mnd to "InntektSiste12Mnd",
                minsteinntektfaktor36mnd to "ØvreTerskelFaktor",
                minsteinntektfaktor12mnd to "NedreTerskelFaktor",
                grunnbeløp to "Grunnbeløp",
                søknadstidspunkt to "Søknadstidspunkt",
                verneplikt to "Verneplikt",
                innsendtSøknadsId to "InnsendtSøknadsId",
                registrertArbeidssøkerPerioder to "Registreringsperioder",
                lærling to "Lærling",
                behandlingsdato to "Behandlingsdato",
                senesteMuligeVirkningsdato to "SenesteMuligeVirkningstidspunkt",
                antallEndredeArbeidsforhold to "Rettighetstype",
                ordinær to "Ordinær",
                permittert to "Permittert",
                lønnsgaranti to "Lønnsgaranti",
                permittertFiskeforedling to "PermittertFiskeforedling",
                harHattDagpengerSiste36mnd to "HarHattDagpengerSiste36Mnd",
                sykepengerSiste36mnd to "SykepengerSiste36Måneder",
                eøsArbeid to "EØSArbeid",
                harInntektNesteKalendermåned to "HarRapportertInntektNesteMåned",
                kanJobbeDeltid to "KanJobbeDeltid",
                helseTilAlleTyperJobb to "HelseTilAlleTyperJobb",
                kanJobbeHvorSomHelst to "KanJobbeHvorSomHelst",
                villigTilÅBytteYrke to "VilligTilÅBytteYrke",
                inntektsrapporteringsperiodeTom to "InntektsrapporteringsperiodeTom",
                fortsattRettKorona to "FortsattRettKorona",
                over67årFradato to "ForGammelGrensedato",
                jobbetUtenforNorge to "JobbetUtenforNorge",
                hattLukkedeSakerSiste8Uker to "HarHattLukketSiste8Uker"
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
