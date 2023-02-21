package no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt

import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.quiz.mediator.soknad.Prosesser
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.behandlingsdato
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.eøsArbeidManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.fangstOgFiskInntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.fangstOgFiskManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.grunnbeløp
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.harInntektNesteKalendermåned
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.hattLukkedeSakerSiste8Uker
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.hattLukkedeSakerSiste8UkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.helseTilAlleTyperJobb
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.inntektNesteKalendermånedManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.jobbetUtenforNorge
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.jobbetUtenforNorgeManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.kanJobbeDeltid
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.kanJobbeHvorSomHelst
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.lønnsgaranti
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.minsteinntektfaktor12mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.minsteinntektfaktor36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.oppfyllerMinsteinntektManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.ordinær
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.over67årFradato
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.over67årManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.periodeOppbruktManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.permittert
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.permittertFiskeforedling
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.prototypeFakta
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.reellArbeidssøkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPerioder
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningsdato
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.svangerskapsrelaterteSykepengerManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.søknadstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.uhåndterbartVirkningsdatoManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.villigTilÅBytteYrke
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.ønsketDato

internal object Seksjoner {
    private val behandlingsdatoSeksjon = with(prototypeFakta) {
        Seksjon("behandlingsdato", Rolle.nav, dato(behandlingsdato))
    }
    private val senesteMuligeVirkningsdatoSeksjon = with(prototypeFakta) {
        Seksjon(
            "senesteMuligeVirkningstidspunkt",
            Rolle.nav,
            dato(senesteMuligeVirkningsdato),
        )
    }
    private val minsteinntektKonstanter = with(prototypeFakta) {
        Seksjon(
            "minsteinntektKonstanter",
            Rolle.nav,
            desimaltall(minsteinntektfaktor12mnd),
            desimaltall(minsteinntektfaktor36mnd),
        )
    }
    private val grunnbeløpSeksjon = with(prototypeFakta) {
        Seksjon(
            "grunnbeløp",
            Rolle.nav,
            inntekt(grunnbeløp),
        )
    }
    private val dataFraSøknadLivssyklus = with(prototypeFakta) {
        Seksjon(
            "dataFraSøknadLivssyklus",
            Rolle.nav,
            dato(søknadstidspunkt),
        )
    }
    private val dataFraSøknad = with(prototypeFakta) {
        Seksjon(
            "datafrasøknad",
            Rolle.nav,
            dato(ønsketDato),
            boolsk(verneplikt),
            boolsk(eøsArbeid),
            boolsk(kanJobbeDeltid),
            boolsk(helseTilAlleTyperJobb),
            boolsk(kanJobbeHvorSomHelst),
            boolsk(villigTilÅBytteYrke),
            boolsk(jobbetUtenforNorge),
        )
    }
    private val arbeidsøkerPerioder = with(prototypeFakta) {
        Seksjon(
            "arbeidsøkerperioder",
            Rolle.nav,
            dato(registrertArbeidssøkerPeriodeFom),
            dato(registrertArbeidssøkerPeriodeTom),
            generator(registrertArbeidssøkerPerioder),
        )
    }
    private val dagpengehistorikk = with(prototypeFakta) {
        Seksjon(
            "dagpengehistorikk",
            Rolle.nav,
            boolsk(harHattDagpengerSiste36mnd),
        )
    }
    private val lukkedeSaker = with(prototypeFakta) {
        Seksjon(
            "lukkedeSaker",
            Rolle.nav,
            boolsk(hattLukkedeSakerSiste8Uker),
        )
    }
    private val inntektshistorikk = with(prototypeFakta) {
        Seksjon(
            "inntektshistorikk",
            Rolle.nav,
            boolsk(fangstOgFiskInntektSiste36mnd),
        )
    }
    private val sykepengehistorikk = with(prototypeFakta) {
        Seksjon(
            "sykepengehistorikk",
            Rolle.nav,
            boolsk(sykepengerSiste36mnd),
        )
    }
    private val inntekter = with(prototypeFakta) {
        Seksjon(
            "inntekter",
            Rolle.nav,
            inntekt(inntektSiste12mnd),
            inntekt(inntektSiste36mnd),
        )
    }
    private val inntektNesteKalendermåned = with(prototypeFakta) {
        Seksjon(
            "inntekt neste kalendermåned",
            Rolle.nav,
            boolsk(harInntektNesteKalendermåned),
        )
    }

    /*

    private val godkjennSluttårsak = with(søknad) {
        Seksjon(
            "godkjenn sluttårsak",
            Rolle.saksbehandler,
            boolsk(godkjenningSluttårsak),
        )
    }
     */
    private val endredeArbeidsforhold = with(prototypeFakta) {
        Seksjon(
            "arbeidsforhold",
            Rolle.nav,
            generator(antallEndredeArbeidsforhold),
            boolsk(ordinær),
            boolsk(lønnsgaranti),
            boolsk(permittertFiskeforedling),
            boolsk(permittert),
        )
    }
    private val manuellGjenopptak = with(prototypeFakta) {
        Seksjon(
            "mulig gjenopptak",
            Rolle.manuell,
            boolsk(periodeOppbruktManuell),
        )
    }
    private val manuellHattLukkedeSakerSiste8Uker = with(prototypeFakta) {
        Seksjon(
            "har hatt lukkede saker siste 8 uker",
            Rolle.manuell,
            boolsk(hattLukkedeSakerSiste8UkerManuell),
        )
    }
    private val manuellSykepenger = with(prototypeFakta) {
        Seksjon(
            "svangerskapsrelaterte sykepenger",
            Rolle.manuell,
            boolsk(svangerskapsrelaterteSykepengerManuell),
        )
    }
    private val manuellFangstOgFisk = with(prototypeFakta) {
        Seksjon(
            "mulige inntekter fra fangst og fisk",
            Rolle.manuell,
            boolsk(fangstOgFiskManuell),
        )
    }
    private val manuellEøs = with(prototypeFakta) {
        Seksjon(
            "EØS-arbeid",
            Rolle.manuell,
            boolsk(eøsArbeidManuell),
        )
    }
    private val manuellJobbetUtenforNorge = with(prototypeFakta) {
        Seksjon(
            "jobbet utenfor Norge",
            Rolle.manuell,
            boolsk(jobbetUtenforNorgeManuell),
        )
    }
    private val manuellDatoer = with(prototypeFakta) {
        Seksjon(
            "virkningstidspunkt vi ikke kan håndtere",
            Rolle.manuell,
            boolsk(uhåndterbartVirkningsdatoManuell),
        )
    }
    private val manuellOppfyllerKraveneTilMinsteArbeidsinntekt = with(prototypeFakta) {
        Seksjon(
            "kravene til minste arbeidsinntekt er oppfylt så",
            Rolle.manuell,
            boolsk(oppfyllerMinsteinntektManuell),
        )
    }
    private val manuellHarInntektNesteKalendermåned = with(prototypeFakta) {
        Seksjon(
            "det er inntekt neste kalendermåned",
            Rolle.manuell,
            boolsk(inntektNesteKalendermånedManuell),
        )
    }
    private val manuellErIkkeReellArbeidssøker = with(prototypeFakta) {
        Seksjon(
            "ikke reell arbeidssøker",
            Rolle.manuell,
            boolsk(reellArbeidssøkerManuell),
        )
    }
    private val manuellErIkkeRegistrertArbeidssøker = with(prototypeFakta) {
        Seksjon(
            "ikke registrert arbeidssøker",
            Rolle.manuell,
            boolsk(registrertArbeidssøkerManuell),
        )
    }
    private val manuellOver67år = with(prototypeFakta) {
        Seksjon(
            "over 67 år",
            Rolle.manuell,
            boolsk(over67årManuell),
        )
    }
    private val inntektsrapporteringsperioder = with(prototypeFakta) {
        Seksjon(
            "inntektsrapporteringsperioder",
            Rolle.nav,
            dato(inntektsrapporteringsperiodeTom),
        )
    }
    private val alder = with(prototypeFakta) {
        Seksjon(
            "alder",
            Rolle.nav,
            dato(over67årFradato),
        )
    }
    internal val prosess: Prosess =
        Prosess(
            Prosesser.AvslagPåMinsteinntekt,
            behandlingsdatoSeksjon,
            senesteMuligeVirkningsdatoSeksjon,
            minsteinntektKonstanter,
            grunnbeløpSeksjon,
            dataFraSøknadLivssyklus,
            dataFraSøknad,
            arbeidsøkerPerioder,
            dagpengehistorikk,
            lukkedeSaker,
            inntektshistorikk,
            sykepengehistorikk,
            inntekter,
            inntektNesteKalendermåned,
            endredeArbeidsforhold,
            inntektsrapporteringsperioder,
            alder,
            manuellGjenopptak,
            manuellSykepenger,
            manuellFangstOgFisk,
            manuellEøs,
            manuellDatoer,
            manuellOppfyllerKraveneTilMinsteArbeidsinntekt,
            manuellHarInntektNesteKalendermåned,
            manuellErIkkeReellArbeidssøker,
            manuellErIkkeRegistrertArbeidssøker,
            manuellOver67år,
            manuellJobbetUtenforNorge,
            manuellHattLukkedeSakerSiste8Uker,
        )
}
