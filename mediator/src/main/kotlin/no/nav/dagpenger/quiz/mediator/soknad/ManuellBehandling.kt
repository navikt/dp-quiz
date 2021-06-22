package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.erIkke
import no.nav.dagpenger.model.regel.førEllerLik
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisGyldig
import no.nav.dagpenger.model.subsumsjon.hvisUgyldig
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
        "fangst og fisk" deltre {
            boolsk(fangstOgFisk) er true hvisGyldig { boolsk(fangstOgFiskManuell) er true }
        }
    }

    private val sjekkEøsArbeid = with(søknad) {
        "eøs arbeid" deltre {
            boolsk(eøsArbeid) er true hvisGyldig { boolsk(eøsArbeidManuell) er true }
        }
    }

    private val sjekkGjenopptak = with(søknad) {
        "skal ha gjenopptak" deltre {
            boolsk(harHattDagpengerSiste36mnd) er true hvisGyldig { boolsk(periodeOppbruktManuell) er true }
        }
    }

    private val sjekkSykepenger = with(søknad) {
        "svangerskapsrelaterte sykepenger" deltre {
            boolsk(sykepengerSiste36mnd) er true hvisGyldig { boolsk(svangerskapsrelaterteSykepengerManuell) er true }
        }
    }

    private val sjekkAntallArbeidsforhold = with(søknad) {
        "antall arbeidsforhold" deltre {
            heltall(antallEndredeArbeidsforhold) erIkke 1 hvisGyldig { boolsk(flereArbeidsforholdManuell) er true }
        }
    }

    internal val sjekkInntektNesteKalendermåned = with(søknad) {
        "har inntekt neste kalendermåned" deltre {
            boolsk(harInntektNesteKalendermåned) er true hvisGyldig { boolsk(inntektNesteKalendermånedManuell) er true }
        }
    }

    internal val sjekkVirkningsdato = with(søknad) {
        dato(virkningsdato) førEllerLik dato(senesteMuligeVirkningsdato) hvisUgyldig {
            boolsk(uhåndterbartVirkningsdatoManuell) er true
        }
    }

    private val sjekkReellArbeidssøker = with(søknad) {
        "aa" deltre {
            "er reell arbeidssøker".minstEnAv(
                boolsk(kanJobbeDeltid) er false,
                boolsk(helseTilAlleTyperJobb) er false,
                boolsk(kanJobbeHvorSomHelst) er false,
                boolsk(villigTilÅBytteYrke) er false
            ) hvisGyldig {
                boolsk(reellArbeidssøkerManuell) er true
            }
        }
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
