import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals

class MinsteinntektTest {
    private val testRapid = TestRapid().apply {
        NavMediator(this)
    }

    @Test
    fun `Skal behandle melding fra modelmeditator `() {
        testRapid.sendTestMessage(eksempelBehov)

        val inspektør = testRapid.inspektør
        val behovMelding = inspektør.message(0)
        assertDoesNotThrow { behovMelding }

        assertEquals("2020-01-01", behovMelding["beregningsdato"].asText())
        assertEquals("Minsteinntekt", behovMelding["@behov"][0].asText())
    }

    @Test
    fun `Skal håndtere svar fra behov løsningen`() {
        testRapid.sendTestMessage(eksempelBehovInput)
        val inspektør = testRapid.inspektør

        assertEquals(2, inspektør.size)

        assertEquals(2, inspektør.message(0)["faktumId"].asInt())
        assertEquals(3, inspektør.message(1)["faktumId"].asInt())
        assertEquals(132.0, inspektør.message(0)["svar"].asDouble())
        assertEquals(1234.0, inspektør.message(1)["svar"].asDouble())
    }

    @Language("json")
    private val eksempelBehov =
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
              "rootId": 3,
              "indeks": 0,
              "roller": [
                "nav"
              ]
            },
            {
              "type": "GrunnleggendeFaktum",
              "navn": "Virkningstidspunkt",
              "id": "1",
              "avhengigFakta": [],
              "avhengerAvFakta": [],
              "clazz": "localdate",
              "rootId": 5,
              "indeks": 0,
              "roller": [
                "nav"
              ],
              "svar": "2020-01-01"
            },
            {
              "type": "GrunnleggendeFaktum",
              "navn": "InntektSiste3År",
              "id": "3",
              "avhengigFakta": [],
              "avhengerAvFakta": [
                "3, 5"
              ],
              "clazz": "inntekt",
              "rootId": 7,
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
                "3, 5"
              ],
              "clazz": "inntekt",
              "rootId": 8,
              "indeks": 0,
              "roller": [
                "nav"
              ]
            }
          ],
          "søknadUuid": "dfa1caa5-46d0-40f3-b1e7-365a112e1911",
          "seksjonsnavn": "minsteinntekt",
          "system_read_count": 0
        }
        """.trimIndent()

    @Language("json")
    private val eksempelOutput =
        """{
          "@behov": [
            "Minsteinntekt"
          ],
          "@id": "12345",
          "aktørId": "1234",
          "beregningsdato": "2020-04-21",
          "faktum": [
              "søknadUuid": "dfa1caa5-46d0-40f3-b1e7-365a112e1911"
          ]
        }"""

    @Language("json")
    private val eksempelBehovInput =
        """{
          "@behov": [
            "Minsteinntekt"
          ],
          "@løsning": {
            "Minsteinntekt": {
              "inntektSiste3År": 1234,
              "inntektSiste1År": 132
            }
          },
          "@id": "12345",
          "aktørId": "1234",
          "beregningsdato": "2020-04-21",
          "søknadUuid": "dfa1caa5-46d0-40f3-b1e7-365a112e1911"
        }"""

    @Language("json")
    private val eksempelBehovSvar =
        """
        {
          "@id": "cf89b67d-b99f-4abd-8573-e3fc84990bb4",
          "@event_name": "faktum_svar",
          "@opprettet": "2020-11-05T13:54:49.465475",
          "fnr": "fødselsnummer",
          "opprettet": "2020-11-05T13:54:49.465285",
          "faktumId": 2,
          "søknadUuid": "6ecf5d89-27c6-4357-896b-01aefb91bef0",
          "svar": 1234,
          "clazz": "inntekt",
          "system_read_count": 0
        }
            """
}
