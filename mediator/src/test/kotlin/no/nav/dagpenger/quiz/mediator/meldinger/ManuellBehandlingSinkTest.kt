package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

internal class ManuellBehandlingSinkTest {

    private val testRapid = TestRapid()

    @Test
    fun `kræsjer ikke når man kjører den opp, og logger`() {
        ManuellBehandlingSink(testRapid)

        //language=JSON
        val melding =
            """
            { "@event_name": "manuell_behandling" }
            """.trimIndent()

        testRapid.sendTestMessage(melding)
    }
}
