package no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt

import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.subsumsjon.hvisOppfyltManuell
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.eøsArbeidManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.fangstOgFiskInntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.fangstOgFiskManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.hattLukkedeSakerSiste8Uker
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.hattLukkedeSakerSiste8UkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.jobbetUtenforNorge
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.jobbetUtenforNorgeManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.periodeOppbruktManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.prototypeSøknad
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.svangerskapsrelaterteSykepengerManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.uhåndterbartVirkningsdatoManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.virkningsdato

internal object ManuellBehandling {
    private val hattInntektFraFangstOgFisk = with(prototypeSøknad) {
        boolsk(fangstOgFiskInntektSiste36mnd) er true hvisOppfyltManuell (boolsk(fangstOgFiskManuell))
    }

    private val harArbeidetEøs = with(prototypeSøknad) {
        boolsk(eøsArbeid) er true hvisOppfyltManuell (boolsk(eøsArbeidManuell))
    }

    private val harJobbetUtenforNorge = with(prototypeSøknad) {
        boolsk(jobbetUtenforNorge) er true hvisOppfyltManuell (boolsk(jobbetUtenforNorgeManuell))
    }

    private val virkningsdatoEtterNåværendeInntektsrapporteringsperiode = with(prototypeSøknad) {
        dato(virkningsdato) etter dato(inntektsrapporteringsperiodeTom) hvisOppfyltManuell (
            boolsk(
                uhåndterbartVirkningsdatoManuell
            )
            )
    }

    private val erMuligGjenopptak = with(prototypeSøknad) {
        boolsk(harHattDagpengerSiste36mnd) er true hvisOppfyltManuell (boolsk(periodeOppbruktManuell))
    }

    private val harHattSykepenger = with(prototypeSøknad) {
        boolsk(sykepengerSiste36mnd) er true hvisOppfyltManuell (boolsk(svangerskapsrelaterteSykepengerManuell))
    }

    private val harHattLukkedeSakerSiste8Uker = with(prototypeSøknad) {
        boolsk(hattLukkedeSakerSiste8Uker) er true hvisOppfyltManuell (boolsk(hattLukkedeSakerSiste8UkerManuell))
    }

    internal val `skal behandles av Arena` =
        "manuelt behandles".minstEnAv(
            virkningsdatoEtterNåværendeInntektsrapporteringsperiode,
            harArbeidetEøs,
            hattInntektFraFangstOgFisk,
            erMuligGjenopptak,
            harHattSykepenger,
            harJobbetUtenforNorge,
            harHattLukkedeSakerSiste8Uker
        )
}
