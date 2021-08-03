package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.behandlingsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeidManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFisk
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFiskManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.flereArbeidsforholdManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fortsattRettKorona
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fortsattRettKoronaManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.grunnbeløp
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harInntektNesteKalendermåned
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.helseTilAlleTyperJobb
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektNesteKalendermånedManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.jobbetUtenforNorgeManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.kanJobbeDeltid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.kanJobbeHvorSomHelst
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lærling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lønnsgaranti
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.minsteinntektfaktor12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.minsteinntektfaktor36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.oppfyllerMinsteinntektManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.ordinær
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.over67årFradato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.over67årManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.periodeOppbruktManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.permittert
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.permittertFiskeforedling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.reellArbeidssøkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPerioder
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.svangerskapsrelaterteSykepengerManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknad
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknadstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.uhåndterbartVirkningsdatoManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.villigTilÅBytteYrke
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.ønsketDato

internal object Seksjoner {

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
            boolsk(fangstOgFisk),
            boolsk(kanJobbeDeltid),
            boolsk(helseTilAlleTyperJobb),
            boolsk(kanJobbeHvorSomHelst),
            boolsk(villigTilÅBytteYrke),
            boolsk(fortsattRettKorona)
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

    private val manuellFlereArbeidsforhold = with(søknad) {
        Seksjon(
            "flere arbeidsforhold",
            Rolle.manuell,
            boolsk(flereArbeidsforholdManuell)
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

    private val manuellFortsattRettKorona = with(søknad) {
        Seksjon(
            "fortsatt rett korona",
            Rolle.manuell,
            boolsk(fortsattRettKoronaManuell)
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
            senesteMuligeVirkningsdatoSeksjon,
            minsteinntektKonstanter,
            grunnbeløpSeksjon,
            dataFraSøknad,
            arbeidsøkerPerioder,
            dagpengehistorikk,
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
            manuellFlereArbeidsforhold,
            manuellOppfyllerKraveneTilMinsteArbeidsinntekt,
            manuellHarInntektNesteKalendermåned,
            manuellErIkkeReellArbeidssøker,
            manuellErIkkeRegistrertArbeidssøker,
            manuellFortsattRettKorona,
            manuellOver67år,
            manuellJobbetUtenforNorge,
        )
}
