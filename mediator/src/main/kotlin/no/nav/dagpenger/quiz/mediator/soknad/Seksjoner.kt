package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.dagensDato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeidManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFisk
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFiskManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.godkjenningRettighetstype
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.godkjenningSisteDagMedLønn
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
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sluttårsaker
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
            dato(dagensDato),
            dato(senesteMuligeVirkningstidspunkt),
            dato(inntektsrapporteringsperiodeFom),
            dato(inntektsrapporteringsperiodeTom)
        )
    }
    private val grunnbeløp = with(søknad) {
        Seksjon(
            "grunnbeløp",
            Rolle.nav,
            inntekt(AvslagPåMinsteinntektOppsett.G3),
            inntekt(AvslagPåMinsteinntektOppsett.G1_5),
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
    private val inntektsunntak = with(søknad) {
        Seksjon(
            "inntektsunntak",
            Rolle.nav,
            boolsk(verneplikt),
            boolsk(lærling)
        )
    }

    private val fangstOgfisk = with(søknad) {
        Seksjon(
            "fangstOgFisk",
            Rolle.nav,
            boolsk(fangstOgFisk),
        )
    }

    private val eøsArbeidSeksjon = with(søknad) {
        Seksjon(
            "eøsArbeid",
            Rolle.nav,
            boolsk(eøsArbeid),
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
            "godkjenn virkningstidspunkt",
            Rolle.saksbehandler,
            boolsk(godkjenningSisteDagMedLønn)
        )
    }
    internal val arbeidsforholdNav = with(søknad) {
        Seksjon(
            "rettighetstype",
            Rolle.nav,
            generator(sluttårsaker),
            boolsk(ordinær),
            boolsk(lønnsgaranti),
            boolsk(permittertFiskeforedling),
            boolsk(permittert),
        )
    }
    internal val arbeidsforholdSaksbehandler = with(søknad) {
        Seksjon(
            "godkjenn rettighetstype",
            Rolle.saksbehandler,
            boolsk(godkjenningRettighetstype),
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

    internal val søknadprosess: Søknadprosess =
        Søknadprosess(
            oppstart,
            grunnbeløp,
            datoer,
            ytelseshistorikk,
            inntektsunntak,
            fangstOgfisk,
            eøsArbeidSeksjon,
            inntekter,
            godkjennDato,
            arbeidsforholdNav,
            arbeidsforholdSaksbehandler,
            manuellGjenopptak,
            manuellSykepenger,
            manuellFangstOgFisk,
            manuellEøs,
            manuellDatoer
        )
}
