package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.subsumsjon.hvisOppfyltManuell
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeidManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFiskInntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFiskManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fortsattRettKorona
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fortsattRettKoronaManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.hattLukkedeSakerSiste8Uker
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.hattLukkedeSakerSiste8UkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.jobbetUtenforNorge
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.jobbetUtenforNorgeManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.periodeOppbruktManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.svangerskapsrelaterteSykepengerManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknad
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.uhåndterbartVirkningsdatoManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.virkningsdato

internal object ManuellBehandling {
    private val hattInntektFraFangstOgFisk = with(søknad) {
        boolsk(fangstOgFiskInntektSiste36mnd) er true hvisOppfyltManuell (boolsk(fangstOgFiskManuell))
    }

    private val harArbeidetEøs = with(søknad) {
        boolsk(eøsArbeid) er true hvisOppfyltManuell (boolsk(eøsArbeidManuell))
    }

    private val harJobbetUtenforNorge = with(søknad) {
        boolsk(jobbetUtenforNorge) er true hvisOppfyltManuell (boolsk(jobbetUtenforNorgeManuell))
    }

    private val virkningsdatoEtterNåværendeInntektsrapporteringsperiode = with(søknad) {
        dato(virkningsdato) etter dato(inntektsrapporteringsperiodeTom) hvisOppfyltManuell (
            boolsk(
                uhåndterbartVirkningsdatoManuell
            )
            )
    }

    private val erMuligGjenopptak = with(søknad) {
        boolsk(harHattDagpengerSiste36mnd) er true hvisOppfyltManuell (boolsk(periodeOppbruktManuell))
    }

    private val harHattSykepenger = with(søknad) {
        boolsk(sykepengerSiste36mnd) er true hvisOppfyltManuell (boolsk(svangerskapsrelaterteSykepengerManuell))
    }

    private val harFortsattRettKorona = with(søknad) {
        boolsk(fortsattRettKorona) er true hvisOppfyltManuell (boolsk(fortsattRettKoronaManuell))
    }

    private val harHattLukkedeSakerSiste8Uker = with(søknad) {
        boolsk(hattLukkedeSakerSiste8Uker) er true hvisOppfyltManuell (boolsk(hattLukkedeSakerSiste8UkerManuell))
    }

    internal val skalManueltBehandles =
        "manuelt behandles".minstEnAv(
            harFortsattRettKorona,
            virkningsdatoEtterNåværendeInntektsrapporteringsperiode,
            harArbeidetEøs,
            hattInntektFraFangstOgFisk,
            erMuligGjenopptak,
            harHattSykepenger,
            harJobbetUtenforNorge,
            harHattLukkedeSakerSiste8Uker
        )
}
