package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.G1_5
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.G3
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.behandlingsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeidManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFisk
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFiskManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.flereArbeidsforholdManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.godkjenningSisteDagMedLønn
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.godkjenningSluttårsak
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harInntektNesteKalendermåned
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektNesteKalendermånedManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lærling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lønnsgaranti
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.oppfyllerMinsteinntektManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.ordinær
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.periodeOppbruktManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.permittert
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.permittertFiskeforedling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registreringsperioder
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidsøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidsøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sisteDagMedLønn
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.svangerskapsrelaterteSykepengerManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknad
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknadstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.uhåndterbartVirkningstidspunktManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.ønsketDato

internal object Seksjoner {

    private val behandlingsdatoSeksjon = with(søknad) {
        Seksjon("behandlingsdato", Rolle.nav, dato(behandlingsdato))
    }

    private val senesteMuligeVirkningstidpunktSeksjon = with(søknad) {
        Seksjon(
            "senesteMuligeVirkningstidspunkt",
            Rolle.nav,
            dato(senesteMuligeVirkningstidspunkt)
        )
    }

    private val inntektsrapporteringsperioder = with(søknad) {
        Seksjon(
            "inntektsrapporteringsperioder",
            Rolle.nav,
            dato(inntektsrapporteringsperiodeFom),
            dato(inntektsrapporteringsperiodeTom)
        )
    }

    private val grunnbeløp = with(søknad) {
        Seksjon(
            "grunnbeløp",
            Rolle.nav,
            inntekt(G3),
            inntekt(G1_5),
        )
    }

    private val dataFraSøknad = with(søknad) {
        Seksjon(
            "datafrasøknad",
            Rolle.nav,
            dato(ønsketDato),
            dato(søknadstidspunkt),
            dato(sisteDagMedLønn),
            boolsk(verneplikt),
            boolsk(lærling),
            boolsk(eøsArbeid),
            boolsk(fangstOgFisk)
        )
    }

    private val arbeidsøkerPerioder = with(søknad) {
        Seksjon(
            "arbeidsøkerperioder",
            Rolle.nav,
            dato(registrertArbeidsøkerPeriodeFom),
            dato(registrertArbeidsøkerPeriodeTom),
            generator(registreringsperioder)
        )
    }

    private val dagpengehistorikk = with(søknad) {
        Seksjon(
            "dagpengehistorikk",
            Rolle.nav,
            boolsk(harHattDagpengerSiste36mnd),
        )
    }

    private val sykepengehistorikk = with(søknad) {
        Seksjon(
            "sykepengehistorikk",
            Rolle.nav,
            boolsk(sykepengerSiste36mnd)
        )
    }

    private val inntekter = with(søknad) {
        Seksjon(
            "inntekter",
            Rolle.nav,
            inntekt(inntektSiste12mnd),
            inntekt(inntektSiste36mnd),
            boolsk(harInntektNesteKalendermåned)
        )
    }

    private val godkjennDato = with(søknad) {
        Seksjon(
            "godkjenn siste dag med lønn",
            Rolle.saksbehandler,
            boolsk(godkjenningSisteDagMedLønn)
        )
    }

    private val endredeArbeidsforhold = with(søknad) {
        Seksjon(
            "arbeidsforhold",
            Rolle.nav,
            generator(antallEndredeArbeidsforhold),
            boolsk(ordinær),
            boolsk(lønnsgaranti),
            boolsk(permittertFiskeforedling),
            boolsk(permittert),
        )
    }

    private val sluttårsakSaksbehandler = with(søknad) {
        Seksjon(
            "godkjenn sluttårsak",
            Rolle.saksbehandler,
            boolsk(godkjenningSluttårsak),
        )
    }

    private val manuellGjenopptak = with(søknad) {
        Seksjon(
            "mulig gjenopptak manuell",
            Rolle.manuell,
            boolsk(periodeOppbruktManuell),
        )
    }

    private val manuellSykepenger = with(søknad) {
        Seksjon(
            "svangerskapsrelaterte sykepenger manuell",
            Rolle.manuell,
            boolsk(svangerskapsrelaterteSykepengerManuell)
        )
    }

    private val manuellFangstOgFisk = with(søknad) {
        Seksjon(
            "fangst og fisk manuell",
            Rolle.manuell,
            boolsk(fangstOgFiskManuell)
        )
    }

    private val manuellEøs = with(søknad) {
        Seksjon(
            "Eøs arbeid manuell",
            Rolle.manuell,
            boolsk(eøsArbeidManuell)
        )
    }

    private val manuellDatoer = with(søknad) {
        Seksjon(
            "datoer manuell",
            Rolle.manuell,
            boolsk(uhåndterbartVirkningstidspunktManuell)
        )
    }

    private val manuellFlereArbeidsforhold = with(søknad) {
        Seksjon(
            "flere arbeidsforhold manuell",
            Rolle.manuell,
            boolsk(flereArbeidsforholdManuell)
        )
    }

    private val manuellOppfyllerKraveneTilMinsteArbeidsinntekt = with(søknad) {
        Seksjon(
            "kravene til minste arbeidsforhold",
            Rolle.manuell,
            boolsk(oppfyllerMinsteinntektManuell)
        )
    }

    private val manuellHarInntektNesteKalendermåned = with(søknad) {
        Seksjon(
            "har inntekt neste kalendermåned manuell",
            Rolle.manuell,
            boolsk(inntektNesteKalendermånedManuell)
        )
    }

    internal val søknadprosess: Søknadprosess =
        Søknadprosess(
            behandlingsdatoSeksjon,
            senesteMuligeVirkningstidpunktSeksjon,
            inntektsrapporteringsperioder,
            grunnbeløp,
            dataFraSøknad,
            arbeidsøkerPerioder,
            dagpengehistorikk,
            sykepengehistorikk,
            inntekter,
            godkjennDato,
            endredeArbeidsforhold,
            sluttårsakSaksbehandler,
            manuellGjenopptak,
            manuellSykepenger,
            manuellFangstOgFisk,
            manuellEøs,
            manuellDatoer,
            manuellFlereArbeidsforhold,
            manuellOppfyllerKraveneTilMinsteArbeidsinntekt,
            manuellHarInntektNesteKalendermåned
        )
}
