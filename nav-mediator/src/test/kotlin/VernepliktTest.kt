import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals

class VernepliktTest {

    private val testRapid = TestRapid().apply {
        NavMediator(this)
    }

    @Test
    fun `Skal behandle melding fra model-mediator`() {
        testRapid.sendTestMessage(modelBehov)

        val inspektør = testRapid.inspektør
        val behovMelding = inspektør.message(0)
        assertDoesNotThrow { behovMelding }

        assertEquals("Verneplikt", behovMelding["@behov"][0].asText())
    }

    @Test
    fun `Skal håndtere svar fra behov løsningen`() {
        testRapid.sendTestMessage(behovSystemSvar)
        val inspektør = testRapid.inspektør

        assertEquals(1, inspektør.size)
        assertEquals(12, inspektør.message(0)["faktumId"].asInt())
        assertEquals(true, inspektør.message(0)["svar"].asBoolean())
    }

    @Language("json")
    private val modelBehov =
        """
        {
          "@event_name": "behov",
          "@opprettet": "2020-11-05T14:05:22.553847",
          "@id": "ea00dbde-2651-44fe-8909-2010a55503c6",
          "fnr": "12345678910",
          "fakta": [
            {
              "type": "GrunnleggendeFaktum",
              "navn": "Verneplikt",
              "id": "12",
              "avhengigFakta": [],
              "avhengerAvFakta": [],
              "clazz": "boolean",
              "rootId": 12,
              "indeks": 0,
              "roller": [
                "nav"
              ]
            }
          ],
          "søknadUuid": "dfa1caa5-46d0-40f3-b1e7-365a112e1911",
          "seksjonsnavn": "verneplikt",
          "system_read_count": 0
        }
        """.trimIndent()

    @Language("json")
    private val behovSystemSvar =
        """{
          "@behov": [
            "Verneplikt"
          ],
          "@løsning": {
            "Verneplikt": {
              "avtjentVerneplikt": true
            }
          },
          "@id": "12345",
          "aktørId": "1234",
          "søknadUuid": "dfa1caa5-46d0-40f3-b1e7-365a112e1911"
        }"""
}
