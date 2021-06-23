package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.erIkke
import no.nav.dagpenger.model.regel.førEllerLik
import no.nav.dagpenger.model.subsumsjon.hvisGyldigManuell
import no.nav.dagpenger.model.subsumsjon.hvisUgyldigManuell
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeidManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFisk
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFiskManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.flereArbeidsforholdManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harInntektNesteKalendermåned
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.helseTilAlleTyperJobb
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektNesteKalendermånedManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.kanJobbeDeltid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.kanJobbeHvorSomHelst
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.periodeOppbruktManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.reellArbeidssøkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.svangerskapsrelaterteSykepengerManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknad
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.uhåndterbartVirkningsdatoManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.villigTilÅBytteYrke
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.virkningsdato

internal object ManuellBehandling {

    private val sjekkFangstOgFisk = with(søknad) {
        boolsk(fangstOgFisk) er true hvisGyldigManuell(boolsk(fangstOgFiskManuell))
    }

    private val sjekkEøsArbeid = with(søknad) {
        boolsk(eøsArbeid) er true hvisGyldigManuell(boolsk(eøsArbeidManuell))
    }

    private val sjekkGjenopptak = with(søknad) {
        boolsk(harHattDagpengerSiste36mnd) er true hvisGyldigManuell(boolsk(periodeOppbruktManuell))
    }

    private val sjekkSykepenger = with(søknad) {
        boolsk(sykepengerSiste36mnd) er true hvisGyldigManuell(boolsk(svangerskapsrelaterteSykepengerManuell))
    }

    private val sjekkAntallArbeidsforhold = with(søknad) {
        heltall(antallEndredeArbeidsforhold) erIkke 1 hvisGyldigManuell(boolsk(flereArbeidsforholdManuell))
    }

    internal val sjekkInntektNesteKalendermåned = with(søknad) {
        boolsk(harInntektNesteKalendermåned) er true hvisGyldigManuell(boolsk(inntektNesteKalendermånedManuell))
    }

    internal val sjekkVirkningsdato = with(søknad) {
        dato(virkningsdato) førEllerLik dato(senesteMuligeVirkningsdato) hvisUgyldigManuell(boolsk(uhåndterbartVirkningsdatoManuell))
    }

    private val sjekkReellArbeidssøker = with(søknad) {
        "er ikke reell arbeidssøker".minstEnAv(
            boolsk(kanJobbeDeltid) er false,
            boolsk(helseTilAlleTyperJobb) er false,
            boolsk(kanJobbeHvorSomHelst) er false,
            boolsk(villigTilÅBytteYrke) er false
        ) hvisGyldigManuell(boolsk(reellArbeidssøkerManuell))
    }

    internal val skalManueltBehandles =
        "manuelt behandles".minstEnAv(
            sjekkGjenopptak,
            sjekkEøsArbeid,
            sjekkFangstOgFisk,
            sjekkSykepenger,
            sjekkAntallArbeidsforhold,
            sjekkReellArbeidssøker
        )
}
