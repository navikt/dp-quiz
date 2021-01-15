package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.februar
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.NySøknadService
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.G1_5
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.G3
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.dagensDato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFisk
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.godkjenningRettighetstype
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.godkjenningSisteDagMedLønn
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lærling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registreringsperioder
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sisteDagMedLønn
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sluttårsaker
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknadstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.ønsketDato
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AvslagPåMinsteinntektTest {
    private lateinit var testRapid: TestRapid

    lateinit var persistence: SøknadRecord
    lateinit var søknadsmal: Søknadprosess

    @BeforeEach
    fun setup() {

        Postgres.withMigratedDb {
            AvslagPåMinsteinntektOppsett.registrer { søknad, versjonId -> FaktumTable(søknad, versjonId) }
            persistence = SøknadRecord()
            testRapid = TestRapid().also {
                FaktumSvarService(
                    søknadPersistence = persistence,
                    rapidsConnection = it
                )
                NySøknadService(persistence, it, AvslagPåMinsteinntektOppsett.VERSJON_ID)
            }
        }

        søknadsmal = persistence.ny(
            Identer.Builder().folkeregisterIdent("12345678910").build(),
            Versjon.UserInterfaceType.Web,
            AvslagPåMinsteinntektOppsett.VERSJON_ID
        )

        søknadsmal.apply {
            dato(dagensDato).besvar(5.januar)
            dato(ønsketDato).besvar(5.januar)
            dato(sisteDagMedLønn).besvar(5.januar)
            dato(søknadstidspunkt).besvar(2.januar)
            dato(senesteMuligeVirkningstidspunkt).besvar(19.januar)
            dato(inntektsrapporteringsperiodeFom).besvar(5.januar)
            dato(inntektsrapporteringsperiodeTom).besvar(5.februar)

            generator(registreringsperioder).besvar(1)
            dato("18.1").besvar(1.januar(2018))
            dato("19.1").besvar(30.januar(2018))

            boolsk(harHattDagpengerSiste36mnd).besvar(false)
            boolsk(sykepengerSiste36mnd).besvar(false)

            boolsk(eøsArbeid).besvar(false)

            boolsk(fangstOgFisk).besvar(false)

            inntekt(G3).besvar(300000.årlig)
            inntekt(G1_5).besvar(150000.årlig)

            boolsk(verneplikt).besvar(false)
            boolsk(lærling).besvar(false)

            inntekt(inntektSiste36mnd).besvar(20000.årlig)
            inntekt(inntektSiste12mnd).besvar(5000.årlig)

            generator(sluttårsaker).besvar(1)
            boolsk("24.1").besvar(false)
            boolsk("25.1").besvar(true)
            boolsk("26.1").besvar(false)
            boolsk("27.1").besvar(false)

            boolsk(godkjenningRettighetstype).besvar(true)
            boolsk(godkjenningSisteDagMedLønn).besvar(true)
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

        assertTrue(Søknadprosess.erFerdig(Visitor(søknadsmal).saksbehandlerSeksjoner))
        assert(søknadsmal.resultat() == false)
    }

    @Test
    fun `Søknader fra brukere som har hatt dagpenger de siste 36 månedene blir ikke behandlet`() {
        søknadsmal.boolsk(harHattDagpengerSiste36mnd).besvar(true)
        assertEquals("mulig gjenopptak manuell", søknadsmal.nesteSeksjoner().first().navn)
    }

    @Test
    fun `De som oppfyller kravet til minsteinntekt får innvilget dagpenger`() {
        søknadsmal.inntekt(inntektSiste36mnd).besvar(2000000.årlig)
        søknadsmal.inntekt(inntektSiste12mnd).besvar(5000000.årlig)
        assert(søknadsmal.resultat() == true)
    }

    @Test
    fun `De som har vært lærling får innvilget dagpenger`() {
        søknadsmal.boolsk(lærling).besvar(true)
        assert(søknadsmal.resultat() == true)
    }

    @Test
    fun `Skal manuelt behandles når dagens dato er mer enn 14 dager før virkningstidspunkt`() {
        søknadsmal.dato(ønsketDato).besvar(20.januar)
        assertEquals("datoer manuell", søknadsmal.nesteSeksjoner().first().navn)
    }

    @Test
    fun `Skal manuelt behandles om virkningstidspunkt er fram i tid, men i annen rapporteringsperiode`() {
        søknadsmal.dato(inntektsrapporteringsperiodeFom).besvar(1.januar)
        søknadsmal.dato(inntektsrapporteringsperiodeTom).besvar(4.januar)
        assertEquals("datoer manuell", søknadsmal.nesteSeksjoner().first().navn)
    }

    @Test
    fun `Skal manuelt behandles hvis har sykepenger`() {
        søknadsmal.boolsk(sykepengerSiste36mnd).besvar(true)
        assertEquals("svangerskapsrelaterte sykepenger manuell", søknadsmal.nesteSeksjoner().first().navn)
    }

    @Test
    fun `Fangst og fisk skal manuelt behandles`() {
        søknadsmal.boolsk(fangstOgFisk).besvar(true)
        assertEquals("fangst og fisk manuell", søknadsmal.nesteSeksjoner().first().navn)
    }

    @Test
    fun `Eøs arbeid skal manuelt behandles`() {
        søknadsmal.boolsk(eøsArbeid).besvar(true)
        assertEquals("Eøs arbeid manuell", søknadsmal.nesteSeksjoner().first().navn)
    }

    @Test
    fun `Virkningstidspunkt er frem i tid og bruker er registrert som arbeiddsøker på vedtaksdato`() {
        søknadsmal.dato(dagensDato).besvar(4.januar)
        assertEquals(false, søknadsmal.resultat())
    }

    @Test
    fun `Virkningstidspunkt er tilbake i tid og bruker er registrert som arbeiddsøker på virkningstidspunkt`() {
        søknadsmal.dato(dagensDato).besvar(6.januar)
        assertEquals(false, søknadsmal.resultat())
    }
}
