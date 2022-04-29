package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DslFaktaseksjonTest {

    object Testseksjon : DslFaktaseksjon {
        const val id1 = 1
        const val id2 = 2
        const val id3 = 3
        override val fakta = listOf(
            dato faktum "faktum 1" id id1,
            tekst faktum "faktum 2" id id2,
            heltall faktum "faktum 3" id id3,
        )

        override fun seksjon(søknad: Søknad) =
            listOf(søknad.seksjon("dummy-seksjon", Rolle.søker, *this.databaseIder()))
    }

    @Test
    fun `Skal kunne lese ut verdien av alle heltall som er definert i en klasse som bruker interface-et`() {
        val databaseIder = Testseksjon.databaseIder()

        assertEquals(3, databaseIder.size)
        databaseIder.forEachIndexed { index, variabel ->
            val forventetId = index + 1
            assertEquals(forventetId, variabel)
        }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Testseksjon.verifiserFeltsammensetting(3, 1 + 2 + 3)
    }
}

fun DslFaktaseksjon.verifiserFeltsammensetting(forventetAntallDatabaseIder: Int, forventetSumAvAlleDatabaseIder: Int) {
    val databaseIder = databaseIder()
    assertEquals(
        forventetAntallDatabaseIder,
        databaseIder.size,
        "Antall felter har endret seg, har du oppdatert versjonsnummeret for søknader som bruker denne seksjonen?"
    )
    assertEquals(
        forventetSumAvAlleDatabaseIder,
        databaseIder.sum(),
        "Det ser ut som at feltsammensettingen har endret seg, har du oppdatert versjonsnummeret for søknader som bruker denne seksjonen?"
    )
}
