package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.førEllerLik
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.hvisGyldig
import no.nav.dagpenger.model.subsumsjon.hvisUgyldig
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.behandlingsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lærling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.minsteinntektsterskel12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.minsteinntektsterskel36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.oppfyllerMinsteinntektManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registreringsperioder
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidsøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidsøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknad
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.uhåndterbartVirkningstidspunktManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.virkningstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.ManuellBehandling.sjekkInntektNesteKalendermåned
import no.nav.dagpenger.quiz.mediator.soknad.ManuellBehandling.skalManueltBehandles

internal object AvslagPåMinsteinntekt {
    private val sjekkVirkningstidspunkt = with(søknad) {
        "virkningstidspunkt" makro {
            dato(virkningstidspunkt) førEllerLik dato(senesteMuligeVirkningstidspunkt) hvisGyldig {
                dato(virkningstidspunkt) mellom
                    dato(inntektsrapporteringsperiodeFom) og dato(inntektsrapporteringsperiodeTom)
            }
        } hvisUgyldig { boolsk(uhåndterbartVirkningstidspunktManuell) er true }
    }
    private val minsteArbeidsinntekt = with(søknad) {
        "oppfyller krav til minste arbeidsinntekt".makro {
            "minste arbeidsinntekt".minstEnAv(
                inntekt(inntektSiste36mnd) minst inntekt(minsteinntektsterskel36mnd),
                inntekt(inntektSiste12mnd) minst inntekt(minsteinntektsterskel12mnd),
                boolsk(verneplikt) er true,
                boolsk(lærling) er true
            ) hvisGyldig { boolsk(oppfyllerMinsteinntektManuell) er true } hvisUgyldig {
                sjekkInntektNesteKalendermåned
            }
        }
       /*
            Skrudd av godkjenning, for å teste kvalitet uten saksbehandler. Skriving til arena er skrudd av.
            .godkjentAv(
              boolsk(godkjenningSluttårsak)
        )
       */
    }
    internal val meldtSomArbeidssøker = with(søknad) {
        generator(registreringsperioder) har "registrert arbeidssøker".makro {
            dato(virkningstidspunkt) etter dato(behandlingsdato) hvisGyldig {
                dato(behandlingsdato) mellom dato(registrertArbeidsøkerPeriodeFom) og
                    dato(registrertArbeidsøkerPeriodeTom)
            } hvisUgyldig {
                dato(virkningstidspunkt) mellom dato(registrertArbeidsøkerPeriodeFom) og
                    dato(registrertArbeidsøkerPeriodeTom)
            }
        }
    }
    internal val regeltre =
        sjekkVirkningstidspunkt hvisGyldig {
            skalManueltBehandles hvisUgyldig {
                "inngangsvilkår".alle(
                    minsteArbeidsinntekt,
                    meldtSomArbeidssøker
                )
            }
        }
}
