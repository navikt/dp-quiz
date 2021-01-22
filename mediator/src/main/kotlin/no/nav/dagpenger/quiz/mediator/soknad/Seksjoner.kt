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
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lærling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lønnsgaranti
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
    private val oppstart = with(søknad) {
        Seksjon(
            "oppstart",
            Rolle.nav,
            dato(behandlingsdato),
            dato(senesteMuligeVirkningstidspunkt),
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

    private val datoer = with(søknad) {
        Seksjon(
            "datoer",
            Rolle.nav,
            dato(ønsketDato),
            dato(søknadstidspunkt),
            dato(sisteDagMedLønn),
            dato(registrertArbeidsøkerPeriodeFom),
            dato(registrertArbeidsøkerPeriodeTom),
            generator(registreringsperioder)
        )
    }

    private val ytelseshistorikk = with(søknad) {
        Seksjon(
            "ytelsehistorikk",
            Rolle.nav,
            boolsk(harHattDagpengerSiste36mnd),
            boolsk(sykepengerSiste36mnd)
        )
    }

    private val søknadsunntak = with(søknad) {
        Seksjon(
            "søknadsunntak",
            Rolle.nav,
            boolsk(verneplikt),
            boolsk(lærling),
            boolsk(eøsArbeid),
            boolsk(fangstOgFisk)
        )
    }

    private val inntekter = with(søknad) {
        Seksjon(
            "inntekter",
            Rolle.nav,
            inntekt(inntektSiste12mnd),
            inntekt(inntektSiste36mnd),
        )
    }

    private val godkjennDato = with(søknad) {
        Seksjon(
            "godkjenn siste dag med lønn",
            Rolle.saksbehandler,
            boolsk(godkjenningSisteDagMedLønn)
        )
    }

    private val arbeidsforholdNav = with(søknad) {
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

    internal val søknadprosess: Søknadprosess =
        Søknadprosess(
            oppstart,
            grunnbeløp,
            datoer,
            ytelseshistorikk,
            søknadsunntak,
            inntekter,
            godkjennDato,
            arbeidsforholdNav,
            sluttårsakSaksbehandler,
            manuellGjenopptak,
            manuellSykepenger,
            manuellFangstOgFisk,
            manuellEøs,
            manuellDatoer,
            manuellFlereArbeidsforhold
        )
}
