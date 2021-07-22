package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisGyldig
import no.nav.dagpenger.model.subsumsjon.hvisGyldigManuell
import no.nav.dagpenger.model.subsumsjon.hvisUgyldig
import no.nav.dagpenger.model.subsumsjon.hvisUgyldigManuell
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.flereArbeidsforholdManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.førsteAvVirkningsdatoOgBehandlingsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harInntektNesteKalendermåned
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.helseTilAlleTyperJobb
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektNesteKalendermånedManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.kanJobbeDeltid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.kanJobbeHvorSomHelst
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lærling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.minsteinntektsterskel12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.minsteinntektsterskel36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.oppfyllerMinsteinntektManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.over67årFradato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.over67årManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.reellArbeidssøkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPerioder
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknad
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.villigTilÅBytteYrke
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.virkningsdato
import no.nav.dagpenger.quiz.mediator.soknad.ManuellBehandling.skalManueltBehandles

internal object AvslagPåMinsteinntekt {
    private val sjekkInntektNesteKalendermåned = with(søknad) {
        boolsk(harInntektNesteKalendermåned) er true hvisGyldigManuell (boolsk(inntektNesteKalendermånedManuell))
    }

    private val minsteArbeidsinntekt = with(søknad) {
        "minste arbeidsinntekt".minstEnAv(
            inntekt(inntektSiste36mnd) minst inntekt(minsteinntektsterskel36mnd),
            inntekt(inntektSiste12mnd) minst inntekt(minsteinntektsterskel12mnd),
            boolsk(verneplikt) er true,
            boolsk(lærling) er true
        ) hvisGyldigManuell (boolsk(oppfyllerMinsteinntektManuell)) hvisUgyldig {
            sjekkInntektNesteKalendermåned
        }
    }

    private val erRegistrertArbeidssøker = with(søknad) {
        generator(registrertArbeidssøkerPerioder) har "arbeidsøkerregistrering".deltre {
            dato(førsteAvVirkningsdatoOgBehandlingsdato) mellom
                dato(registrertArbeidssøkerPeriodeFom) og dato(registrertArbeidssøkerPeriodeTom)
        } hvisUgyldigManuell (boolsk(registrertArbeidssøkerManuell))
    }

    private val erReellArbeidssøker = with(søknad) {
        "er reell arbeidssøker".alle(
            boolsk(kanJobbeDeltid) er true,
            boolsk(helseTilAlleTyperJobb) er true,
            boolsk(kanJobbeHvorSomHelst) er true,
            boolsk(villigTilÅBytteYrke) er true
        ) hvisUgyldigManuell (boolsk(reellArbeidssøkerManuell))
    }

    private val under67år = with(søknad) {
        "under 67år" deltre {
            dato(virkningsdato) før dato(over67årFradato)
        } hvisUgyldigManuell (boolsk(over67årManuell))
    }

    private val harEttArbeidsforhold = with(søknad) {
        heltall(antallEndredeArbeidsforhold) er 1 hvisUgyldigManuell (boolsk(flereArbeidsforholdManuell))
    }

    internal val regeltre =
        harEttArbeidsforhold hvisGyldig {
            under67år hvisGyldig {
                skalManueltBehandles hvisUgyldig {
                    "inngangsvilkår".alle(
                        erRegistrertArbeidssøker,
                        erReellArbeidssøker,
                        minsteArbeidsinntekt
                    )
                }
            }
        }
}
