package no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt

import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.`avviklingsdato for midlertidig krav til minsteinntekt`
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
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.lærling
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
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.reellArbeidssøkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPerioder
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningsdato
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.svangerskapsrelaterteSykepengerManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.søknad
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.søknadstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.uhåndterbartVirkningsdatoManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.villigTilÅBytteYrke
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.ønsketDato

internal object Seksjoner {

    private val aviklingMinsteArbeidsinntektSeksjon = with(søknad) {
        Seksjon("aviklingMinsteArbeidsinntekt", Rolle.nav, dato(`avviklingsdato for midlertidig krav til minsteinntekt`))
    }

    private val behandlingsdatoSeksjon = with(søknad) {
        Seksjon("behandlingsdato", Rolle.nav, dato(behandlingsdato))
    }

    private val senesteMuligeVirkningsdatoSeksjon = with(søknad) {
        Seksjon(
            "senesteMuligeVirkningstidspunkt",
            Rolle.nav,
            dato(senesteMuligeVirkningsdato)
        )
    }

    private val minsteinntektKonstanter = with(søknad) {
        Seksjon(
            "minsteinntektKonstanter",
            Rolle.nav,
            desimaltall(minsteinntektfaktor12mnd),
            desimaltall(minsteinntektfaktor36mnd)
        )
    }

    private val grunnbeløpSeksjon = with(søknad) {
        Seksjon(
            "grunnbeløp",
            Rolle.nav,
            inntekt(grunnbeløp)
        )
    }

    private val dataFraSøknad = with(søknad) {
        Seksjon(
            "datafrasøknad",
            Rolle.nav,
            dato(ønsketDato),
            dato(søknadstidspunkt),
            boolsk(verneplikt),
            boolsk(lærling),
            boolsk(eøsArbeid),
            boolsk(kanJobbeDeltid),
            boolsk(helseTilAlleTyperJobb),
            boolsk(kanJobbeHvorSomHelst),
            boolsk(villigTilÅBytteYrke),
            boolsk(jobbetUtenforNorge)
        )
    }

    private val arbeidsøkerPerioder = with(søknad) {
        Seksjon(
            "arbeidsøkerperioder",
            Rolle.nav,
            dato(registrertArbeidssøkerPeriodeFom),
            dato(registrertArbeidssøkerPeriodeTom),
            generator(registrertArbeidssøkerPerioder)
        )
    }

    private val dagpengehistorikk = with(søknad) {
        Seksjon(
            "dagpengehistorikk",
            Rolle.nav,
            boolsk(harHattDagpengerSiste36mnd),
        )
    }

    private val lukkedeSaker = with(søknad) {
        Seksjon(
            "lukkedeSaker",
            Rolle.nav,
            boolsk(hattLukkedeSakerSiste8Uker),
        )
    }

    private val inntektshistorikk = with(søknad) {
        Seksjon(
            "inntektshistorikk",
            Rolle.nav,
            boolsk(fangstOgFiskInntektSiste36mnd)
        )
    }

    private val sykepengehistorikk = with(søknad) {
        Seksjon(
            "sykepengehistorikk",
            Rolle.nav,
            boolsk(sykepengerSiste36mnd)
        )
    }

    private val inntekter = with(søknad) {
        Seksjon(
            "inntekter",
            Rolle.nav,
            inntekt(inntektSiste12mnd),
            inntekt(inntektSiste36mnd)
        )
    }

    private val inntektNesteKalendermåned = with(søknad) {
        Seksjon(
            "inntekt neste kalendermåned",
            Rolle.nav,
            boolsk(harInntektNesteKalendermåned)
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

    private val endredeArbeidsforhold = with(søknad) {
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

    private val manuellGjenopptak = with(søknad) {
        Seksjon(
            "mulig gjenopptak",
            Rolle.manuell,
            boolsk(periodeOppbruktManuell),
        )
    }

    private val manuellHattLukkedeSakerSiste8Uker = with(søknad) {
        Seksjon(
            "har hatt lukkede saker siste 8 uker",
            Rolle.manuell,
            boolsk(hattLukkedeSakerSiste8UkerManuell),
        )
    }

    private val manuellSykepenger = with(søknad) {
        Seksjon(
            "svangerskapsrelaterte sykepenger",
            Rolle.manuell,
            boolsk(svangerskapsrelaterteSykepengerManuell)
        )
    }

    private val manuellFangstOgFisk = with(søknad) {
        Seksjon(
            "mulige inntekter fra fangst og fisk",
            Rolle.manuell,
            boolsk(fangstOgFiskManuell)
        )
    }

    private val manuellEøs = with(søknad) {
        Seksjon(
            "EØS-arbeid",
            Rolle.manuell,
            boolsk(eøsArbeidManuell)
        )
    }

    private val manuellJobbetUtenforNorge = with(søknad) {
        Seksjon(
            "jobbet utenfor Norge",
            Rolle.manuell,
            boolsk(jobbetUtenforNorgeManuell)
        )
    }

    private val manuellDatoer = with(søknad) {
        Seksjon(
            "virkningstidspunkt vi ikke kan håndtere",
            Rolle.manuell,
            boolsk(uhåndterbartVirkningsdatoManuell)
        )
    }

    private val manuellOppfyllerKraveneTilMinsteArbeidsinntekt = with(søknad) {
        Seksjon(
            "kravene til minste arbeidsinntekt er oppfylt så",
            Rolle.manuell,
            boolsk(oppfyllerMinsteinntektManuell)
        )
    }

    private val manuellHarInntektNesteKalendermåned = with(søknad) {
        Seksjon(
            "det er inntekt neste kalendermåned",
            Rolle.manuell,
            boolsk(inntektNesteKalendermånedManuell)
        )
    }

    private val manuellErIkkeReellArbeidssøker = with(søknad) {
        Seksjon(
            "ikke reell arbeidssøker",
            Rolle.manuell,
            boolsk(reellArbeidssøkerManuell)
        )
    }

    private val manuellErIkkeRegistrertArbeidssøker = with(søknad) {
        Seksjon(
            "ikke registrert arbeidssøker",
            Rolle.manuell,
            boolsk(registrertArbeidssøkerManuell)
        )
    }

    private val manuellOver67år = with(søknad) {
        Seksjon(
            "over 67 år",
            Rolle.manuell,
            boolsk(over67årManuell)
        )
    }

    private val inntektsrapporteringsperioder = with(søknad) {
        Seksjon(
            "inntektsrapporteringsperioder",
            Rolle.nav,
            dato(inntektsrapporteringsperiodeTom)
        )
    }

    private val alder = with(søknad) {
        Seksjon(
            "alder",
            Rolle.nav,
            dato(over67årFradato)
        )
    }

    internal val søknadprosess: Søknadprosess =
        Søknadprosess(
            behandlingsdatoSeksjon,
            aviklingMinsteArbeidsinntektSeksjon,
            senesteMuligeVirkningsdatoSeksjon,
            minsteinntektKonstanter,
            grunnbeløpSeksjon,
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
