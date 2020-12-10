package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.NySøknadService
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class AvslagPåMinsteinntektTest {
    private lateinit var søknadprosess: Søknadprosess

    private lateinit var testRapid: TestRapid

    companion object {
        private val avslagPåMinsteinntekt = AvslagPåMinsteinntekt()
    }

    @BeforeEach
    fun setup() {
        Postgres.withMigratedDb {
            avslagPåMinsteinntekt.registrer { søknad, versjonId -> FaktumTable(søknad, versjonId) }
            søknadprosess =
                avslagPåMinsteinntekt.søknadprosess(Person(Identer.Builder().folkeregisterIdent("123123123").build()))
            val persistence = SøknadRecord()
            testRapid = TestRapid().also {
                FaktumSvarService(
                    søknadPersistence = persistence,
                    rapidsConnection = it
                )
                NySøknadService(persistence, it)
            }
        }
    }

    @Test
    fun `De som ikke oppfyller kravet til minsteinntekt får avslag`() {

        val søknadsId = søknad()
        besvar(søknadsId, "20", 5.januar)
        besvar(søknadsId, "1", 5.januar)
        besvar(søknadsId, "2", 5.januar)
        besvar(søknadsId, "3", 5.januar)
        besvar(søknadsId, "10", 2.januar)
        besvarGenerator(søknadsId, "16", listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))

        assertEquals("fangstOgFisk", testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText())
        assertEquals(7, testRapid.inspektør.size)

        besvar(søknadsId, "5", false)
        assertEquals(8, testRapid.inspektør.size)

        assertEquals("grunnbeløp", testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText())
        besvarInntekt(søknadsId, "8", 300000.0)
        besvarInntekt(søknadsId, "9", 150000.0)
        assertEquals(10, testRapid.inspektør.size)

        assertEquals("inntektsunntak", testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText())
        besvar(søknadsId, "11", false)
        besvar(søknadsId, "17", false)
        assertEquals(12, testRapid.inspektør.size)

        assertEquals("inntekter", testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText())
        besvarInntekt(søknadsId, "6", 20000.0)
        besvarInntekt(søknadsId, "7", 5000.0)
        assertEquals(14, testRapid.inspektør.size)

        assertEquals(
            "godkjenn virkningstidspunkt",
            testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText()
        )
        assertEquals(14, testRapid.inspektør.size)
        besvar(søknadsId, "12", true)
        assertEquals(16, testRapid.inspektør.size)
        assertFalse(testRapid.inspektør.field(testRapid.inspektør.size - 1, "resultat").asBoolean())
    }

    @Test
    fun `De som oppfyller kravet til minsteinntekt gir ingen seksjoner til saksbehandler`() {

        val søknadsId = søknad()
        besvar(søknadsId, "20", 5.januar)
        besvar(søknadsId, "1", 5.januar)
        besvar(søknadsId, "2", 5.januar)
        besvar(søknadsId, "3", 5.januar)
        besvar(søknadsId, "10", 2.januar)
        besvarGenerator(søknadsId, "16", listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))

        assertEquals("fangstOgFisk", testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText())
        assertEquals(7, testRapid.inspektør.size)

        besvar(søknadsId, "5", false)
        assertEquals(8, testRapid.inspektør.size)

        assertEquals("grunnbeløp", testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText())
        besvarInntekt(søknadsId, "8", 300000.0)
        besvarInntekt(søknadsId, "9", 150000.0)
        assertEquals(10, testRapid.inspektør.size)

        assertEquals("inntektsunntak", testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText())
        besvar(søknadsId, "11", false)
        besvar(søknadsId, "17", false)
        assertEquals(12, testRapid.inspektør.size)

        assertEquals("inntekter", testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText())
        besvarInntekt(søknadsId, "6", 2000000.0)
        besvarInntekt(søknadsId, "7", 5000000.0)
        assertEquals(15, testRapid.inspektør.size)

        assertTrue(testRapid.inspektør.field(testRapid.inspektør.size - 1, "resultat").asBoolean())
    }

    private fun besvar(søknadsId: String, faktumId: String, svar: Any) {
        //language=JSON
        testRapid.sendTestMessage(
            """{
  "søknad_uuid": "$søknadsId",
  "@event_name": "faktum_svar",
  "fakta": [{
    "id": "$faktumId",
    "svar": "$svar",
    "clazz": "${svar::class.java.simpleName.toLowerCase()}"
}
  ],
  "@opprettet": "${LocalDateTime.now()}",
  "@id": "${UUID.randomUUID()}"
}"""
        )
    }

    private fun besvarInntekt(søknadsId: String, faktumId: String, svar: Double) {
        //language=JSON
        testRapid.sendTestMessage(
            """{
  "søknad_uuid": "$søknadsId",
  "@event_name": "faktum_svar",
  "fakta": [{
    "id": "$faktumId",
    "svar": "$svar",
    "clazz": "inntekt"
}
  ],
  "@opprettet": "${LocalDateTime.now()}",
  "@id": "${UUID.randomUUID()}"
}"""
        )
    }

    private fun besvarGenerator(søknadsId: String, faktumId: String, svar: List<List<Pair<String, Any>>>) {

        val noe = svar.map { it.map { lagSvar(it.first, it.second) } }

        val fakta = mutableListOf("""{"id": "$faktumId", "svar": $noe, "clazz": "generator"}""")

        //language=JSON
        testRapid.sendTestMessage(
            """{
  "søknad_uuid": "$søknadsId",
  "@event_name": "faktum_svar",
  "fakta": $fakta,
  "@opprettet": "${LocalDateTime.now()}",
  "@id": "${UUID.randomUUID()}"
}""".also {
                println(it)
            }
        )
    }

    private fun lagSvar(faktumId: String, svar: Any) =
        """{"id": "$faktumId", "svar": "$svar", "clazz": "${svar::class.java.simpleName.toLowerCase()}"}"""

    private fun søknad(): String {
        testRapid.sendTestMessage(
            """{
  "@event_name": "Søknad",
  "fnr": "123456789",
  "aktørId": "",
  "søknadsId": "9876"
}"""
        )
        return testRapid.inspektør.field(0, "søknad_uuid").asText()
    }
}
