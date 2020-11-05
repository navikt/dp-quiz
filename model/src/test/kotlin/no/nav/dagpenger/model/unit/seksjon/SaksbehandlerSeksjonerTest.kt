package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktagrupper.Versjon.FaktagrupperType.Web
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.gyldigGodkjentAv
import no.nav.dagpenger.model.subsumsjon.så
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
        ja nei "approve3" id 4 avhengerAv 3
    )
    private val prototypeSubsumsjon = (prototypeSøknad.ja(1) er true gyldigGodkjentAv prototypeSøknad.ja(2)) så
        (prototypeSøknad.ja(3) er true gyldigGodkjentAv prototypeSøknad.ja(4))

    private val prototypeFaktagrupper = Faktagrupper(
        prototypeSøknad,
        Seksjon("søker", Rolle.søker, prototypeSøknad.ja(1), prototypeSøknad.ja(3)),
        Seksjon("saksbehandler1", Rolle.saksbehandler, prototypeSøknad.ja(2)),
        Seksjon("saksbehandler1", Rolle.saksbehandler, prototypeSøknad.ja(4))
    )

    private val version = Versjon(1, prototypeSøknad, prototypeSubsumsjon, mapOf(Web to prototypeFaktagrupper))

    @Test
    fun `sorter ut irrelevante saksbehandler seksjoner `() {
        val seksjoner = Versjon.siste.faktagrupper(UNG_PERSON_FNR_2018, Web, uuid)
        assertEquals(listOf(seksjoner[0]), seksjoner.nesteSeksjoner())
        seksjoner.ja(1).besvar(true, Rolle.søker)
        assertEquals(listOf(seksjoner[0]), seksjoner.nesteSeksjoner())
        seksjoner.ja(3).besvar(true, Rolle.søker)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertTrue(seksjoner.ja(2) in it[0] && !seksjoner.ja(2).erBesvart())
            assertTrue(seksjoner.ja(4) in it[1] && !seksjoner.ja(4).erBesvart())
        }
        seksjoner.ja(2).besvar(true, Rolle.saksbehandler)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertTrue(seksjoner.ja(2) in it[0] && seksjoner.ja(2).erBesvart())
            assertTrue(seksjoner.ja(4) in it[1] && !seksjoner.ja(4).erBesvart())
        }
        seksjoner.ja(4).besvar(true, Rolle.saksbehandler)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertTrue(seksjoner.ja(2) in it[0] && seksjoner.ja(2).erBesvart())
            assertTrue(seksjoner.ja(4) in it[1] && seksjoner.ja(4).erBesvart())
        }
        seksjoner.ja(1).besvar(false, Rolle.søker)
        seksjoner.nesteSeksjoner().also {
            assertEquals(2, it.size)
            assertFalse(seksjoner.ja(2) in it[0])
            assertTrue(seksjoner.ja(4) in it[1] && seksjoner.ja(4).erBesvart())
        }
    }
}
