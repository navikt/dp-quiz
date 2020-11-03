import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class NavMediatorTest {
    private val testRapid = TestRapid().apply { NavMediator(this) }

    @Test
    fun `Skal kunne lese behov fra kafka`(){
        testRapid.sendTestMessage("""
             {
                "@behov": ["Inntekt"],
                "@event_name": "innhentFaktum",
                "@id" : "12345", 
                "aktørId" : "1234",
                "fødselsnummer" : "1234",
                "vedtakId" : "12345",
                "beregningsdato": "2020-04-21"
             }
            """.trimIndent())

        val inspektør = testRapid.inspektør
        assertDoesNotThrow { inspektør.message(0) }
    }
}