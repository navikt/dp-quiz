package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DslFaktaseksjonTest {

    object Testseksjon : DslFaktaseksjon {
        const val id1 = 1
        const val id2 = 2
        const val id3 = 3
        override val fakta = listOf(
            tekst faktum "faktum 1" id id1,
            tekst faktum "faktum 2" id id2,
            tekst faktum "faktum 3" id id3,
        )
    }

    @Test
    fun `Skal kunne lese ut verdien av alle heltall som er definert i klassen som bruker interface-et`() {
        val databaseIder = Testseksjon.databaseIder()

        assertEquals(3, databaseIder.size)
        databaseIder.forEachIndexed { index, variabel ->
            val forventetId = index + 1
            assertEquals(forventetId, variabel)
        }
        assertEquals(1 + 2 + 3, databaseIder.sum())
    }
}
