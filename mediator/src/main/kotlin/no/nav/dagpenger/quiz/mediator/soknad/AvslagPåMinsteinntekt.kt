package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.førEllerLik
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfyltManuell
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfyltManuell
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
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
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.reellArbeidssøkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPerioder
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknad
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.uhåndterbartVirkningsdatoManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.villigTilÅBytteYrke
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.virkningsdato
import no.nav.dagpenger.quiz.mediator.soknad.ManuellBehandling.`skal behandles av Arena`

internal object AvslagPåMinsteinntekt {
    private val sjekkInntektNesteKalendermåned = with(søknad) {
        boolsk(harInntektNesteKalendermåned) er true hvisOppfyltManuell (boolsk(inntektNesteKalendermånedManuell))
    }
    private val `oppfyller krav til minste arbeidsinntekt eller unntakene` = with(søknad) {
        "minste arbeidsinntekt".minstEnAv(
            inntekt(inntektSiste36mnd) minst inntekt(minsteinntektsterskel36mnd),
            inntekt(inntektSiste12mnd) minst inntekt(minsteinntektsterskel12mnd),
            boolsk(verneplikt) er true,
            boolsk(lærling) er true
        ) hvisOppfyltManuell (boolsk(oppfyllerMinsteinntektManuell)) hvisIkkeOppfylt {
            sjekkInntektNesteKalendermåned
        }
    }

    private val `må være registrert som arbeidssøker ved virkningstidspunkt eller behandlingsdato` = with(søknad) {
        generator(registrertArbeidssøkerPerioder) har "arbeidsøkerregistrering".deltre {
            dato(førsteAvVirkningsdatoOgBehandlingsdato) mellom
                dato(registrertArbeidssøkerPeriodeFom) og dato(registrertArbeidssøkerPeriodeTom)
        } hvisIkkeOppfyltManuell (boolsk(registrertArbeidssøkerManuell))
    }

    private val `er reell arbeidssøker` = with(søknad) {
        "er reell arbeidssøker".alle(
            boolsk(kanJobbeDeltid) er true,
            boolsk(helseTilAlleTyperJobb) er true,
            boolsk(kanJobbeHvorSomHelst) er true,
            boolsk(villigTilÅBytteYrke) er true
        ) hvisIkkeOppfyltManuell (boolsk(reellArbeidssøkerManuell))
    }

    private val `søkeren må være under 67 år ved virkningstidspunkt` = with(søknad) {
        "under 67år" deltre {
            dato(virkningsdato) før dato(over67årFradato)
        }
    }

    private val `virkningstidspunkt kan ikke være mer enn 14 dager frem i tid` = with(søknad) {
        dato(virkningsdato) førEllerLik dato(senesteMuligeVirkningsdato) hvisIkkeOppfyltManuell (
            boolsk(
                uhåndterbartVirkningsdatoManuell
            )
            )
    }

    private val `har hentet arbeidsforhold` = with(søknad) {
        heltall(antallEndredeArbeidsforhold) minst (0)
    }

    internal val regeltre =
        `har hentet arbeidsforhold` hvisOppfylt {
            `virkningstidspunkt kan ikke være mer enn 14 dager frem i tid` hvisOppfylt {
                `søkeren må være under 67 år ved virkningstidspunkt` hvisOppfylt {
                    `skal behandles av Arena` hvisIkkeOppfylt {
                        "inngangsvilkår".alle(
                            `må være registrert som arbeidssøker ved virkningstidspunkt eller behandlingsdato`,
                            `er reell arbeidssøker`,
                            `oppfyller krav til minste arbeidsinntekt eller unntakene`
                        )
                    }
                }
            }
        }
}
