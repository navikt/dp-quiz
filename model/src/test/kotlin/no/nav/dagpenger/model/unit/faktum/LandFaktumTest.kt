package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.FaktumVisitor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LandFaktumTest {

    private lateinit var søknadprosess: Søknadprosess
    private lateinit var landFaktum: Faktum<Land>

    @BeforeEach
    fun setup() {
        søknadprosess = Søknad(
            testversjon,
            land faktum "land" gruppe "eøs" med eøsEllerSveits() gruppe "norge-jan-mayen" med norge() id 1
        ).testSøknadprosess()

        landFaktum = søknadprosess.land(1)
    }

    @Test
    fun `Skal kunne lage land faktum med grupper av land der faktum er besvart`() {
        assertFalse { landFaktum.erBesvart() }
        landFaktum.besvar(Land("BEL"))
        assertTrue { landFaktum.erBesvart() }
        val forventetLandGrupper = LandFaktumVisitor(landFaktum).forventetLandGrupper
        assertTrue { forventetLandGrupper.containsKey("land.gruppe.eøs") }
        assertEquals(eøsEllerSveits(), forventetLandGrupper["land.gruppe.eøs"])
        assertTrue { forventetLandGrupper.containsKey("land.gruppe.norge-jan-mayen") }
        assertEquals(norge(), forventetLandGrupper["land.gruppe.norge-jan-mayen"])
    }

    @Test
    fun `Skal kunne lage land faktum med grupper av land der faktum ikke er besvart`() {
        assertFalse { landFaktum.erBesvart() }
        val forventetLandGrupper = LandFaktumVisitor(landFaktum).forventetLandGrupper
        assertTrue { forventetLandGrupper.containsKey("land.gruppe.eøs") }
        assertEquals(eøsEllerSveits(), forventetLandGrupper["land.gruppe.eøs"])
        assertTrue { forventetLandGrupper.containsKey("land.gruppe.norge-jan-mayen") }
        assertEquals(norge(), forventetLandGrupper["land.gruppe.norge-jan-mayen"])
    }

    private class LandFaktumVisitor(faktum: Faktum<*>) : FaktumVisitor {

        init {
            faktum.accept(this)
        }

        lateinit var forventetLandGrupper: LandGrupper

        override fun <R : Comparable<R>> visitMedSvar(
            faktum: GrunnleggendeFaktum<R>,
            tilstand: Faktum.FaktumTilstand,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            godkjenner: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            svar: R,
            besvartAv: String?,
            gyldigeValg: GyldigeValg?,
            landGrupper: LandGrupper?
        ) {
            forventetLandGrupper = landGrupper!!
        }

        override fun <R : Comparable<R>> visitUtenSvar(
            faktum: GrunnleggendeFaktum<R>,
            tilstand: Faktum.FaktumTilstand,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            godkjenner: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            gyldigeValg: GyldigeValg?,
            landGrupper: LandGrupper?
        ) {
            forventetLandGrupper = landGrupper!!
        }
    }

    private fun norge() = listOf(Land("NOR"), Land("SJM"))

    private fun eøsEllerSveits() = listOf(
        "BEL",
        "BGR",
        "DNK",
        "EST",
        "FIN",
        "FRA",
        "GRC",
        "IRL",
        "ISL",
        "ITA",
        "HRV",
        "CYP",
        "LVA",
        "LIE",
        "LTU",
        "LUX",
        "MLT",
        "NLD",
        "POL",
        "PRT",
        "ROU",
        "SVK",
        "SVN",
        "ESP",
        "CHE",
        "SWE",
        "CZE",
        "DEU",
        "HUN",
        "AUT"
    ).map { land ->
        Land(land)
    }
}
