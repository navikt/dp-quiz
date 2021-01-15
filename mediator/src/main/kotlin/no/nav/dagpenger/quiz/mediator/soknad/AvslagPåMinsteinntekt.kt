package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.førEllerLik
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.regel.ugyldigGodkjentAv
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.bareEnAv
import no.nav.dagpenger.model.subsumsjon.hvisGyldig
import no.nav.dagpenger.model.subsumsjon.hvisUgyldig
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.G1_5
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.G3
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.dagensDato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.godkjenningRettighetstype
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.godkjenningSisteDagMedLønn
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lærling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lønnsgaranti
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.ordinær
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.permittert
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.permittertFiskeforedling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registreringsperioder
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidsøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidsøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sluttårsaker
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknad
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.uhåndterbartVirkningstidspunktManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.virkningstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.ManuellBehandling.sjekkManuell

internal object AvslagPåMinsteinntekt {
    private val rettighetstype = with(søknad) {
        generator(sluttårsaker) med "sluttårsak".makro {
            "bare en av".bareEnAv(
                boolsk(ordinær) er true,
                boolsk(permittertFiskeforedling) er true,
                boolsk(lønnsgaranti) er true,
                boolsk(permittert) er true
            )
        }
    }

    private val sjekkVirkningstidspunkt = with(søknad) {
        "virkningstidspunkt" makro {
            dato(virkningstidspunkt) førEllerLik dato(senesteMuligeVirkningstidspunkt) hvisGyldig {
                dato(virkningstidspunkt) mellom dato(inntektsrapporteringsperiodeFom) og dato(
                    inntektsrapporteringsperiodeTom
                )
            }
        } hvisUgyldig { boolsk(uhåndterbartVirkningstidspunktManuell) er true }
    }

    private val minsteArbeidsinntekt = with(søknad) {
        "minste arbeidsinntekt".minstEnAv(
            inntekt(inntektSiste36mnd) minst inntekt(G3),
            inntekt(inntektSiste12mnd) minst inntekt(G1_5),
            boolsk(verneplikt) er true,
            boolsk(lærling) er true
        ).ugyldigGodkjentAv(boolsk(godkjenningSisteDagMedLønn), boolsk(godkjenningRettighetstype))
    }

    internal val meldtSomArbeidssøker = with(søknad) {
        generator(registreringsperioder) har "registrert arbeidssøker".makro {
            dato(virkningstidspunkt) etter dato(dagensDato) hvisGyldig {
                dato(dagensDato) mellom dato(registrertArbeidsøkerPeriodeFom) og
                    dato(registrertArbeidsøkerPeriodeTom)
            } hvisUgyldig {
                dato(virkningstidspunkt) mellom dato(registrertArbeidsøkerPeriodeFom) og
                    dato(registrertArbeidsøkerPeriodeTom)
            }
        }
    }

    internal val regeltre = sjekkVirkningstidspunkt hvisGyldig {
        sjekkManuell hvisUgyldig {
            "inngangsvilkår".alle(
                minsteArbeidsinntekt,
                meldtSomArbeidssøker,
                rettighetstype
            )
        }
    }
}
