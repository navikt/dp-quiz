package no.nav.dagpenger.quiz.mediator.meldinger

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import org.junit.jupiter.api.Test
import java.util.UUID

internal class ManuellBehandlingSinkTest {
    private val testRapid = TestRapid()

    @Test
    fun `kræsjer ikke når man kjører den opp, og logger`() {
        val resultatPersistence = mockk<ResultatPersistence>(relaxed = true)

        ManuellBehandlingSink(testRapid, resultatPersistence)

        val uuid = UUID.randomUUID()
        val manuellSeksjonNavn = "manuell"

        val melding =
            """
            { 
              "@event_name": "manuell_behandling",
              "søknad_uuid": "$uuid",
              "seksjon_navn": "$manuellSeksjonNavn"
            }
            """.trimIndent()

        testRapid.sendTestMessage(melding)

        verify(exactly = 1) { resultatPersistence.lagreManuellBehandling(uuid, manuellSeksjonNavn) }
    }
}
