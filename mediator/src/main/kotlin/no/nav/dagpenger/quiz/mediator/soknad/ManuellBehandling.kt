package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.erIkke
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.subsumsjon.hvisGyldigManuell
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeidManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFisk
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFiskManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.flereArbeidsforholdManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fortsattRettKorona
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fortsattRettKoronaManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.periodeOppbruktManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.svangerskapsrelaterteSykepengerManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknad
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.uhåndterbartVirkningsdatoManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.virkningsdato

internal object ManuellBehandling {

    private val hattInntektFraFangstOgFisk = with(søknad) {
        boolsk(fangstOgFisk) er true hvisGyldigManuell(boolsk(fangstOgFiskManuell))
    }

    private val harArbeidetEøs = with(søknad) {
        boolsk(eøsArbeid) er true hvisGyldigManuell (boolsk(eøsArbeidManuell))
    }

    private val harFlereArbeidsforhold = with(søknad) {
        heltall(antallEndredeArbeidsforhold) erIkke 1 hvisGyldigManuell (boolsk(flereArbeidsforholdManuell))
    }

    private val virkningsdatoEtterNåværendeInntektsrapporteringsperiode = with(søknad) {
        dato(virkningsdato) etter dato(inntektsrapporteringsperiodeTom) hvisGyldigManuell (boolsk(uhåndterbartVirkningsdatoManuell))
    }

    private val erMuligGjenopptak = with(søknad) {
        boolsk(harHattDagpengerSiste36mnd) er true hvisGyldigManuell (boolsk(periodeOppbruktManuell))
    }

    private val harHattSykepenger = with(søknad) {
        boolsk(sykepengerSiste36mnd) er true hvisGyldigManuell (boolsk(svangerskapsrelaterteSykepengerManuell))
    }

    private val harFortsattRettKorona = with(søknad) {
        boolsk(fortsattRettKorona) er true hvisGyldigManuell (boolsk(fortsattRettKoronaManuell))
    }

    internal val skalManueltBehandles =
        "manuelt behandles".minstEnAv(
            harFortsattRettKorona,
            virkningsdatoEtterNåværendeInntektsrapporteringsperiode,
            harArbeidetEøs,
            hattInntektFraFangstOgFisk,
            harFlereArbeidsforhold,
            erMuligGjenopptak,
            harHattSykepenger
        )
}
