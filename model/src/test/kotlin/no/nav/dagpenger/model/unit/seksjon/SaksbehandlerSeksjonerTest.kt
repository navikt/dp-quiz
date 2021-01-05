package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.godkjentAv
import no.nav.dagpenger.model.regel.gyldigGodkjentAv
import no.nav.dagpenger.model.regel.ugyldigGodkjentAv
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.så
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SaksbehandlerSeksjonerTest {
    companion object {
        internal val uuid = UUID.randomUUID()
    }

    private val prototypeSøknad = Søknad(
        0,
        boolsk faktum "f1" id 1,
        boolsk faktum "approve1" id 2 avhengerAv 1,
        boolsk faktum "f3" id 3,
        boolsk faktum "approve3" id 4 avhengerAv 3,
        boolsk faktum "f5" id 5,
        boolsk faktum "approve5" id 6 avhengerAv 5
    )
    private val prototypeSubsumsjon = (prototypeSøknad.boolsk(1) er true gyldigGodkjentAv prototypeSøknad.boolsk(2)) så
        (prototypeSøknad.boolsk(3) er true ugyldigGodkjentAv prototypeSøknad.boolsk(4)) eller
        (prototypeSøknad.boolsk(5) er true godkjentAv prototypeSøknad.boolsk(6))

    private val prototypeSøknadprosess = Søknadprosess(
        prototypeSøknad,
        Seksjon("søker", Rolle.søker, prototypeSøknad.boolsk(1), prototypeSøknad.boolsk(3), prototypeSøknad.boolsk(5)),
        Seksjon("saksbehandler1", Rolle.saksbehandler, prototypeSøknad.boolsk(2)),
        Seksjon("saksbehandler2", Rolle.saksbehandler, prototypeSøknad.boolsk(4), prototypeSøknad.boolsk(6))
    )

    private val søknadprosessTestBygger = Versjon.Bygger(
        prototypeSøknad,
        prototypeSubsumsjon,
        mapOf(Web to prototypeSøknadprosess)
    )

    private lateinit var seksjoner: Søknadprosess
    private lateinit var f1: Faktum<Boolean>
    private lateinit var f3: Faktum<Boolean>
    private lateinit var f5: Faktum<Boolean>
    private lateinit var approve1: Faktum<Boolean>
    private lateinit var approve3: Faktum<Boolean>
    private lateinit var approve5: Faktum<Boolean>

    @BeforeEach
    internal fun setup() {
        seksjoner = søknadprosessTestBygger.søknadprosess(testPerson, Web, uuid)
        f1 = seksjoner.boolsk(1)
        f3 = seksjoner.boolsk(3)
        f5 = seksjoner.boolsk(5)
        approve1 = seksjoner.boolsk(2)
        approve3 = seksjoner.boolsk(4)
        approve5 = seksjoner.boolsk(6)
    }

    @Test
    fun `sorter ut irrelevante saksbehandler seksjoner `() {
        assertEquals(listOf(seksjoner[0]), seksjoner.nesteSeksjoner())
        beTrue(f1)
        assertEquals(listOf(seksjoner[0]), seksjoner.nesteSeksjoner())
        beFalse(f3)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertTrue(approve1 in it[0] && !approve1.erBesvart())
            assertTrue(approve3 in it[1] && !seksjoner.boolsk(4).erBesvart())
        }
        beTrue(approve1)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertTrue(approve1 in it[0] && approve1.erBesvart())
            assertTrue(approve3 in it[1] && !approve3.erBesvart())
        }
        beTrue(approve3)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertTrue(approve1 in it[0] && approve1.erBesvart())
            assertTrue(approve3 in it[1] && approve3.erBesvart())
        }
        beFalse(f1)
        seksjoner.nesteSeksjoner().also {
            assertEquals(1, it.size)
            assertTrue(f5 in it[0] && !f5.erBesvart())
        }
        beTrue(f5)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertFalse(approve1 in it[0])
            assertTrue(approve5 in it[1] && !approve5.erBesvart())
        }
        beTrue(approve5)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertFalse(approve1 in it[0])
            assertTrue(approve5 in it[1] && approve5.erBesvart())
        }
        beFalse(f5)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertFalse(approve1 in it[0])
            assertTrue(approve5 in it[1] && !approve5.erBesvart())
        }
        beFalse(approve5)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertFalse(approve1 in it[0])
            assertTrue(approve5 in it[1] && approve5.erBesvart())
        }
    }

    private fun beTrue(vararg ider: Faktum<Boolean>) {
        ider.forEach {
            it.besvar(true)
        }
    }

    private fun beFalse(vararg ider: Faktum<Boolean>) {
        ider.forEach {
            it.besvar(false)
        }
    }
}
