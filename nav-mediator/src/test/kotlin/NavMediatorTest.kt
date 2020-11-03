import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class NavMediatorTest {
    private val testRapid = TestRapid().apply { NavMediator(this) }

    @Test
    fun `Skal kunne lese behov fra kafka`() {
        testRapid.sendTestMessage(
            """
             {
                "@behov": ["Inntekt"],
                "@event_name": "behov",
                "@id" : "12345", 
                "@opprettet" : "2020-11-03",
                "fødselsnummer" : "1234",
                "søknadId" : "12345",
                "fakta": ""
             }
            """.trimIndent()
        )

        val inspektør = testRapid.inspektør
        assertDoesNotThrow { inspektør.message(0) }
    }
}
