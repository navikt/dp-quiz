package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktagrupper.Versjon.FaktagrupperType.Web
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.godkjentAv
import no.nav.dagpenger.model.regel.gyldigGodkjentAv
import no.nav.dagpenger.model.regel.ugyldigGodkjentAv
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
        internal const val UNG_PERSON_FNR_2018 = "12020052345"
        internal val uuid = UUID.randomUUID()
    }
    private val prototypeSøknad = Søknad(
        ja nei "f1" id 1,
        ja nei "approve1" id 2 avhengerAv 1,
        ja nei "f3" id 3,
        ja nei "approve3" id 4 avhengerAv 3,
        ja nei "f5" id 5,
        ja nei "approve5" id 6 avhengerAv 5
    )
    private val prototypeSubsumsjon = (prototypeSøknad.ja(1) er true gyldigGodkjentAv prototypeSøknad.ja(2)) så
        (prototypeSøknad.ja(3) er true ugyldigGodkjentAv prototypeSøknad.ja(4)) eller
        (prototypeSøknad.ja(5) er true godkjentAv prototypeSøknad.ja(6))

    private val prototypeFaktagrupper = Faktagrupper(
        prototypeSøknad,
        Seksjon("søker", Rolle.søker, prototypeSøknad.ja(1), prototypeSøknad.ja(3), prototypeSøknad.ja(5) ),
        Seksjon("saksbehandler1", Rolle.saksbehandler, prototypeSøknad.ja(2)),
        Seksjon("saksbehandler2", Rolle.saksbehandler, prototypeSøknad.ja(4), prototypeSøknad.ja(6))
    )

    private val version = Versjon(1, prototypeSøknad, prototypeSubsumsjon, mapOf(Web to prototypeFaktagrupper))
    private  lateinit var seksjoner: Faktagrupper
    private lateinit var f1: Faktum<Boolean>
    private lateinit var f3: Faktum<Boolean>
    private lateinit var f5: Faktum<Boolean>
    private lateinit var approve1: Faktum<Boolean>
    private lateinit var approve3: Faktum<Boolean>
    private lateinit var approve5: Faktum<Boolean>

    @BeforeEach
    internal fun setup() {
        seksjoner = Versjon.siste.faktagrupper(UNG_PERSON_FNR_2018, Web, uuid)
        f1 = seksjoner.ja(1)
        f3 = seksjoner.ja(3)
        f5 = seksjoner.ja(5)
        approve1 = seksjoner.ja(2)
        approve3 = seksjoner.ja(4)
        approve5 = seksjoner.ja(6)

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
            assertTrue(approve3 in it[1] && !seksjoner.ja(4).erBesvart())
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

    private fun beTrue(vararg ider: Faktum<Boolean> ){
        ider.forEach {
            it.besvar(true)
        }
    }

    private fun beFalse(vararg ider: Faktum<Boolean> ){
        ider.forEach {
            it.besvar(false)
        }
    }
}
