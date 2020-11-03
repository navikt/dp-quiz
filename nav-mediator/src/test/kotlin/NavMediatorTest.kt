import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class NavMediatorTest {
    private val testRapid = TestRapid().apply { NavMediator(this) }

    @Test
    fun `Skal kunne lese behov fra kafka`() {
        testRapid.sendTestMessage(eksempelBehov)

        val inspektør = testRapid.inspektør
        assertDoesNotThrow { inspektør.message(0) }
    }

    @Language("json")
    private val eksempelBehov =
        """
        {
          "@event_name": "behov",
          "@opprettet": "2020-11-03T12:30:24.254756",
          "@id": "23d8ca5a-a31b-435c-af26-fc37f253340b",
          "@behov": [
            "InntektSiste3År",
            "InntektSisteÅr"
          ],
          "fødselsnummer": "12345678910",
          "fakta": [
            {
              "type": "GrunnleggendeFaktum",
              "navn": "InntektSiste3År",
              "id": "3",
              "avhengigFakta": [],
              "avhengerAvFakta": [
                "1"
              ],
              "clazz": "inntekt",
              "rootId": 3,
              "indeks": 0,
              "roller": [
                "nav"
              ]
            },
            {
              "type": "GrunnleggendeFaktum",
              "navn": "InntektSisteÅr",
              "id": "2",
              "avhengigFakta": [],
              "avhengerAvFakta": [
                "1"
              ],
              "clazz": "inntekt",
              "rootId": 2,
              "indeks": 0,
              "roller": [
                "nav"
              ]
            }
          ],
          "søknadId": "e0e6293c-68ab-4127-9da2-2fc56c29be81",
          "system_read_count": 0
        }


        """.trimIndent()
}
