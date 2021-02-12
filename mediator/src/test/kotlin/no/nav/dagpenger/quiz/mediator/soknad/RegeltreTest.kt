package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import no.nav.dagpenger.quiz.mediator.helpers.februar
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.meldtSomArbeidssøker
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.regeltre
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.behandlingsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFisk
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.godkjenningSisteDagMedLønn
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.godkjenningSluttårsak
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.grunnbeløp
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harInntektNesteKalendermåned
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lærling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lønnsgaranti
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.nedreFaktor
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.oppfyllerMinsteinntektManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.ordinær
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.permittert
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.permittertFiskeforedling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registreringsperioder
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidsøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidsøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sisteDagMedLønn
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknad
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknadstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.ønsketDato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.øvreFaktor
import no.nav.dagpenger.quiz.mediator.soknad.Seksjoner.søknadprosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class RegeltreTest {
    lateinit var manglerInntekt: Søknadprosess

    @BeforeEach
    fun setup() {
        manglerInntekt = Versjon.Bygger(søknad, regeltre, mapOf(Versjon.UserInterfaceType.Web to søknadprosess))
            .søknadprosess(
                Person(UUID.randomUUID(), Identer.Builder().folkeregisterIdent("12345678910").build()),
                Versjon.UserInterfaceType.Web
            )

        manglerInntekt.apply {
            dato(behandlingsdato).besvar(5.januar)
            dato(ønsketDato).besvar(5.januar)
            dato(sisteDagMedLønn).besvar(5.januar)
            dato(søknadstidspunkt).besvar(2.januar)
            dato(senesteMuligeVirkningstidspunkt).besvar(19.januar)
            dato(inntektsrapporteringsperiodeFom).besvar(5.januar)
            dato(inntektsrapporteringsperiodeTom).besvar(5.februar)
            boolsk(harInntektNesteKalendermåned).besvar(false)

            generator(registreringsperioder).besvar(1)
            dato("$registrertArbeidsøkerPeriodeFom.1").besvar(1.januar(2018))
            dato("$registrertArbeidsøkerPeriodeTom.1").besvar(30.januar(2018))

            boolsk(harHattDagpengerSiste36mnd).besvar(false)
            boolsk(sykepengerSiste36mnd).besvar(false)

            boolsk(eøsArbeid).besvar(false)

            boolsk(fangstOgFisk).besvar(false)

            inntekt(grunnbeløp).besvar(100000.årlig)
            desimaltall(nedreFaktor).besvar(1.5)
            desimaltall(øvreFaktor).besvar(3.0)

            boolsk(verneplikt).besvar(false)
            boolsk(lærling).besvar(false)

            inntekt(inntektSiste36mnd).besvar(20000.årlig)
            inntekt(inntektSiste12mnd).besvar(5000.årlig)

            generator(antallEndredeArbeidsforhold).besvar(1)
            boolsk("$ordinær.1").besvar(false)
            boolsk("$permittert.1").besvar(true)
            boolsk("$lønnsgaranti.1").besvar(false)
            boolsk("$permittertFiskeforedling.1").besvar(false)

            boolsk(godkjenningSluttårsak).besvar(true)
            boolsk(godkjenningSisteDagMedLønn).besvar(true)
            // TODO: Nå sender vi alle som oppfyller kravene til minste arbeidsinntekt til manuell, vi setter denne til true så den bypasses
            manglerInntekt.boolsk(oppfyllerMinsteinntektManuell).besvar(true)
        }
    }

    @Test
    fun `De som ikke oppfyller kravet til minsteinntekt får avslag`() {
        class Visitor(avslagSøknad: Søknadprosess) : SøknadprosessVisitor {
            val saksbehandlerSeksjoner = mutableListOf<Seksjon>()

            init {
                avslagSøknad.accept(this)
            }

            override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>, indeks: Int) {
                if (rolle == Rolle.saksbehandler) saksbehandlerSeksjoner.add(seksjon)
            }
        }

        assertTrue(Søknadprosess.erFerdig(Visitor(manglerInntekt).saksbehandlerSeksjoner))
        assertEquals(false, manglerInntekt.resultat())
    }

    @Test
    fun `Søknader fra brukere som har hatt dagpenger de siste 36 månedene blir ikke behandlet`() {
        manglerInntekt.boolsk(harHattDagpengerSiste36mnd).besvar(true)
        assertEquals("mulig gjenopptak", manglerInntekt.nesteSeksjoner().first().navn)
    }

    @Test
    fun `De som oppfyller kravet til minsteinntekt får innvilget dagpenger`() {
        manglerInntekt.inntekt(inntektSiste36mnd).besvar(2000000.årlig)
        manglerInntekt.inntekt(inntektSiste12mnd).besvar(5000000.årlig)
        assertEquals(true, manglerInntekt.resultat())
    }

    @Test
    fun `De som har vært lærling får innvilget dagpenger`() {
        manglerInntekt.boolsk(lærling).besvar(true)
        assertEquals(true, manglerInntekt.resultat())
    }

    @Test
    fun `De som har avtjent verneplikt får innvilget dagpenger`() {
        manglerInntekt.boolsk(verneplikt).besvar(true)
        assertEquals(true, manglerInntekt.resultat())
    }

    @Test
    fun `Skal manuelt behandles når dagens dato er mer enn 14 dager før virkningstidspunkt`() {
        manglerInntekt.dato(ønsketDato).besvar(20.januar)
        assertEquals("virkningstidspunkt vi ikke kan håndtere", manglerInntekt.nesteSeksjoner().first().navn)
    }

    @Test
    fun `Skal manuelt behandles om virkningstidspunkt er fram i tid, men i annen rapporteringsperiode`() {
        manglerInntekt.dato(inntektsrapporteringsperiodeFom).besvar(1.januar)
        manglerInntekt.dato(inntektsrapporteringsperiodeTom).besvar(4.januar)
        assertEquals("virkningstidspunkt vi ikke kan håndtere", manglerInntekt.nesteSeksjoner().first().navn)
    }

    @Test
    fun `Skal manuelt behandles hvis har sykepenger`() {
        manglerInntekt.boolsk(sykepengerSiste36mnd).besvar(true)
        assertEquals("svangerskapsrelaterte sykepenger", manglerInntekt.nesteSeksjoner().first().navn)
    }

    @Test
    fun `Fangst og fisk skal manuelt behandles`() {
        manglerInntekt.boolsk(fangstOgFisk).besvar(true)
        assertEquals("mulige inntekter fra fangst og fisk", manglerInntekt.nesteSeksjoner().first().navn)
    }

    @Test
    fun `Eøs arbeid skal manuelt behandles`() {
        manglerInntekt.boolsk(eøsArbeid).besvar(true)
        assertEquals("EØS-arbeid", manglerInntekt.nesteSeksjoner().first().navn)
    }

    @Test
    fun `Inntekt neste kalendermåned skal manuelt behandles`() {
        manglerInntekt.boolsk(harInntektNesteKalendermåned).besvar(true)
        assertEquals("det er inntekt neste kalendermåned", manglerInntekt.nesteSeksjoner().first().navn)
    }

    @Test
    fun `Virkningstidspunkt er frem i tid og bruker er registrert som arbeiddsøker på vedtaksdato`() {
        val søknad = byggSøknad(meldtSomArbeidssøker)

        søknad.apply {
            dato(behandlingsdato).besvar(4.januar)
            dato(ønsketDato).besvar(5.januar)
            dato(sisteDagMedLønn).besvar(5.januar)
            dato(søknadstidspunkt).besvar(2.januar)

            generator(registreringsperioder).besvar(1)
            dato("$registrertArbeidsøkerPeriodeFom.1").besvar(1.januar(2018))
            dato("$registrertArbeidsøkerPeriodeTom.1").besvar(4.januar(2018))
        }

        assertEquals(true, søknad.resultat())
    }

    @Test
    fun `Virkningstidspunkt er tilbake i tid og bruker er registrert som arbeiddsøker på virkningstidspunkt`() {
        val søknad = byggSøknad(meldtSomArbeidssøker)

        søknad.apply {
            dato(behandlingsdato).besvar(6.januar)
            dato(ønsketDato).besvar(5.januar)
            dato(sisteDagMedLønn).besvar(5.januar)
            dato(søknadstidspunkt).besvar(2.januar)

            generator(registreringsperioder).besvar(1)
            dato("$registrertArbeidsøkerPeriodeFom.1").besvar(1.januar(2018))
            dato("$registrertArbeidsøkerPeriodeTom.1").besvar(6.januar(2018))
        }

        assertEquals(true, søknad.resultat())
    }

    @Test
    fun `Flere arbeidsforhold skal manuelt behandles`() {
        manglerInntekt.heltall(antallEndredeArbeidsforhold).besvar(2)
        assertEquals("flere arbeidsforhold", manglerInntekt.nesteSeksjoner().first().navn)
    }

    private fun byggSøknad(subsumsjon: Subsumsjon) =
        Versjon.Bygger(søknad, subsumsjon, mapOf(Versjon.UserInterfaceType.Web to søknadprosess))
            .søknadprosess(
                Person(UUID.randomUUID(), Identer.Builder().folkeregisterIdent("12345678910").build()),
                Versjon.UserInterfaceType.Web
            )
}
