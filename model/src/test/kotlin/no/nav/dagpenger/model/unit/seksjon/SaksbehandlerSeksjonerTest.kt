package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.TestProsesser
import no.nav.dagpenger.model.helpers.testProsess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.ikkeOppfyltGodkjentAv
import no.nav.dagpenger.model.subsumsjon.oppfyltGodkjentAv
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SaksbehandlerSeksjonerTest {
    private val prototypeFakta =
        Fakta(
            testversjon,
            boolsk faktum "f1" id 1,
            boolsk faktum "approve1" id 2 avhengerAv 1,
            boolsk faktum "f3" id 3,
            boolsk faktum "approve3" id 4 avhengerAv 3,
            boolsk faktum "f5" id 5,
            boolsk faktum "approve5" id 6 avhengerAv 5,
        )
    private val prototypeSubsumsjon =
        ((prototypeFakta.boolsk(1) er true).oppfyltGodkjentAv(prototypeFakta.boolsk(2))) hvisOppfylt {
            (prototypeFakta.boolsk(3) er true).ikkeOppfyltGodkjentAv(prototypeFakta.boolsk(4))
        } hvisIkkeOppfylt {
            (prototypeFakta.boolsk(5) er true).godkjentAv(prototypeFakta.boolsk(6))
        }
    private val prototypeProsess =
        Prosess(
            TestProsesser.Test,
            prototypeFakta,
            Seksjon("søker", Rolle.søker, prototypeFakta.boolsk(1), prototypeFakta.boolsk(3), prototypeFakta.boolsk(5)),
            Seksjon("saksbehandler1", Rolle.saksbehandler, prototypeFakta.boolsk(2)),
            Seksjon("saksbehandler2", Rolle.saksbehandler, prototypeFakta.boolsk(4), prototypeFakta.boolsk(6)),
            rootSubsumsjon = prototypeSubsumsjon,
        )
    private lateinit var seksjoner: Prosess
    private val f1 get() = seksjoner.boolsk(1)
    private val f3 get() = seksjoner.boolsk(3)
    private val f5 get() = seksjoner.boolsk(5)
    private val approve1 get() = seksjoner.boolsk(2)
    private val approve3 get() = seksjoner.boolsk(4)
    private val approve5 get() = seksjoner.boolsk(6)

    @BeforeEach
    internal fun setup() {
        seksjoner = prototypeProsess.testProsess()
    }

    @Test
    fun `sorter ut irrelevante saksbehandler seksjoner `() {
        assertEquals(listOf(seksjoner[0]), seksjoner.nesteSeksjoner())
        f1.besvar(true)
        assertEquals(listOf(seksjoner[0]), seksjoner.nesteSeksjoner())
        f3.besvar(false)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertTrue(approve1 in it[0] && !approve1.erBesvart())
            assertTrue(approve3 in it[1] && !seksjoner.boolsk(4).erBesvart())
        }

        approve1.besvar(true)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertTrue(approve1 in it[0] && approve1.erBesvart())
            assertTrue(approve3 in it[1] && !approve3.erBesvart())
        }

        approve3.besvar(true)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertTrue(approve1 in it[0] && approve1.erBesvart())
            assertTrue(approve3 in it[1] && approve3.erBesvart())
        }
        f1.besvar(false)
        seksjoner.nesteSeksjoner().also {
            assertEquals(1, it.size)
            assertTrue(f5 in it[0] && !f5.erBesvart())
        }
        f5.besvar(true)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertFalse(approve1 in it[0])
            assertTrue(approve5 in it[1] && !approve5.erBesvart())
        }
        approve5.besvar(true)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertFalse(approve1 in it[0])
            assertTrue(approve5 in it[1] && approve5.erBesvart())
        }
        f5.besvar(false)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertFalse(approve1 in it[0])
            assertTrue(approve5 in it[1] && !approve5.erBesvart())
        }
        approve5.besvar(false)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertFalse(approve1 in it[0])
            assertTrue(approve5 in it[1] && approve5.erBesvart())
        }
    }
}
