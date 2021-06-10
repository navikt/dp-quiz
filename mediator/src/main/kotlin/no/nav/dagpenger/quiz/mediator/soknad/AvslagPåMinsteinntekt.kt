package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisGyldig
import no.nav.dagpenger.model.subsumsjon.hvisUgyldig
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.førsteAvVirkningsdatoOgBehandlingsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lærling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.minsteinntektsterskel12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.minsteinntektsterskel36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.oppfyllerMinsteinntektManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPerioder
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknad
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.ManuellBehandling.sjekkInntektNesteKalendermåned
import no.nav.dagpenger.quiz.mediator.soknad.ManuellBehandling.sjekkVirkningsdato
import no.nav.dagpenger.quiz.mediator.soknad.ManuellBehandling.skalManueltBehandles

internal object AvslagPåMinsteinntekt {
    private val minsteArbeidsinntekt = with(søknad) {
        "oppfyller krav til minste arbeidsinntekt".deltre {
            "minste arbeidsinntekt".minstEnAv(
                inntekt(inntektSiste36mnd) minst inntekt(minsteinntektsterskel36mnd),
                inntekt(inntektSiste12mnd) minst inntekt(minsteinntektsterskel12mnd),
                boolsk(verneplikt) er true,
                boolsk(lærling) er true
            ) hvisGyldig { boolsk(oppfyllerMinsteinntektManuell) er true } hvisUgyldig {
                sjekkInntektNesteKalendermåned
            }
        }
    }

    internal val meldtSomArbeidssøker = with(søknad) {
        generator(registrertArbeidssøkerPerioder) har "arbeidsøkerregistrering".deltre {
            dato(førsteAvVirkningsdatoOgBehandlingsdato) mellom dato(registrertArbeidssøkerPeriodeFom) og
                dato(registrertArbeidssøkerPeriodeTom)
        }
    }

    internal val regeltre =
        sjekkVirkningsdato hvisGyldig {
            skalManueltBehandles hvisUgyldig {
                "inngangsvilkår".alle(
                    minsteArbeidsinntekt,
                    meldtSomArbeidssøker
                )
            }
        }
}
