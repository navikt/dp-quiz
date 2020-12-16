package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.februar
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.helpers.mars
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
    private lateinit var testRapid: TestRapid

    @BeforeEach
    fun setup() {
        Postgres.withMigratedDb {
            AvslagPåMinsteinntekt.registrer { søknad, versjonId -> FaktumTable(søknad, versjonId) }
            val persistence = SøknadRecord()
            testRapid = TestRapid().also {
                FaktumSvarService(
                    søknadPersistence = persistence,
                    rapidsConnection = it
                )
                NySøknadService(persistence, it, AvslagPåMinsteinntekt.VERSJON_ID)
            }
        }
    }

    @Test
    fun `De som ikke oppfyller kravet til minsteinntekt får avslag`() {
        withSøknad { besvar ->
            besvar("20", 5.januar)
            besvar("1", 5.januar)
            besvar("2", 5.januar)
            besvar("3", 5.januar)
            besvar("10", 2.januar)
            besvar("16", listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))

            assertGjeldendeSeksjon("fangstOgFisk")

            besvar("5", false)

            assertGjeldendeSeksjon("grunnbeløp")
            besvar("8", 300000.årlig)
            besvar("9", 150000.årlig)

            assertGjeldendeSeksjon("inntektsunntak")
            besvar("11", false)
            besvar("17", false)

            assertGjeldendeSeksjon("inntekter")
            besvar("6", 20000.årlig)
            besvar("7", 5000.årlig)

            assertEquals(14, testRapid.inspektør.size)
            assertGjeldendeSeksjon("Arbeidsforhold")
            besvar("23", listOf(listOf("24.1" to false, "25.1" to true, "26.1" to false, "27.1" to false, "28.1" to false)))
            assertEquals(17, testRapid.inspektør.size)
//            assertFalse(gjeldendeResultat())

            assertGjeldendeSeksjon("Godkjenn Arbeidsforhold")
            assertEquals(17, testRapid.inspektør.size)
            besvar("29", true)

            assertEquals("godkjenn virkningstidspunkt", testRapid.inspektør.field(testRapid.inspektør.size - 2, "seksjon_navn").asText())

            besvar("12", true)
            assertEquals(24, testRapid.inspektør.size)
            assertFalse(gjeldendeResultat())
        }
    }

    @Test
    fun `De som oppfyller kravet til minsteinntekt gir ingen seksjoner til saksbehandler`() {
        withSøknad { besvar ->
            besvar("20", 5.januar)
            besvar("1", 5.januar)
            besvar("2", 5.januar)
            besvar("3", 5.januar)
            besvar("10", 2.januar)
            besvar("16", listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))

            assertGjeldendeSeksjon("fangstOgFisk")

            besvar("5", false)

            assertGjeldendeSeksjon("grunnbeløp")
            besvar("8", 300000.årlig)
            besvar("9", 150000.årlig)

            assertGjeldendeSeksjon("inntektsunntak")
            besvar("11", false)
            besvar("17", false)

            assertGjeldendeSeksjon("inntekter")
            besvar("6", 2000000.årlig)
            besvar("7", 5000000.årlig)

            assertEquals(16, testRapid.inspektør.size)
            assertTrue(gjeldendeResultat())
        }
    }

    @Test
    fun `De som har fangstOgFisk men ikke oppfyller kravet til minsteinntekt gir to oppgaver til saksbehandler`() {
        withSøknad { besvar ->
            besvar("20", 5.januar)
            besvar("1", 5.januar)
            besvar("2", 5.januar)
            besvar("3", 5.januar)
            besvar("10", 2.januar)
            besvar("16", listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))

            assertGjeldendeSeksjon("fangstOgFisk")
            besvar("5", true)

            assertGjeldendeSeksjon("grunnbeløp")
            besvar("8", 300000.årlig)
            besvar("9", 150000.årlig)

            assertGjeldendeSeksjon("inntektsunntak")
            besvar("11", false)
            besvar("17", false)

            assertGjeldendeSeksjon("inntekter")
            besvar("6", 20000.årlig)
            besvar("7", 5000.årlig)

            assertEquals(
                "godkjenn fangst og fisk",
                testRapid.inspektør.field(testRapid.inspektør.size - 2, "seksjon_navn").asText()
            )
            assertGjeldendeSeksjon("godkjenn virkningstidspunkt")
            besvar("15", true)
            besvar("12", true)

            assertEquals(
                "godkjenn virkningstidspunkt",
                testRapid.inspektør.field(testRapid.inspektør.size - 2, "seksjon_navn").asText()
            )

            assertFalse(gjeldendeResultat())
        }
    }

    @Test
    fun `Skal ikke gi oppgaver til saksbehandler når dagens dato er før virkningstidspunkt`() {
        withSøknad { besvar ->
            besvar("20", 1.januar)
            besvar("1", 5.januar)
            besvar("2", 5.januar)
            besvar("3", 5.januar)
            besvar("10", 2.januar)
            besvar("16", listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))
            besvar("21", 5.januar)
            besvar("22", 5.februar)

            assertFalse(gjeldendeResultat())
        }
    }

    @Test
    fun `Skal gå videre om virkningstidspunkt er fram i tid i samme rapporteringsperiode`() {
        withSøknad { besvar ->
            besvar("20", 12.januar)
            besvar("1", 14.januar)
            besvar("2", 5.januar)
            besvar("3", 5.januar)
            besvar("10", 12.januar)
            besvar("16", listOf(listOf("18.1" to 1.januar, "19.1" to 30.januar)))
            besvar("21", 5.januar)
            besvar("22", 5.februar)

            assertGjeldendeSeksjon("fangstOgFisk")
        }
    }

    @Test
    fun `Skal ikke gå videre om virkningstidspunkt er fram i tid, men i annen rapporteringsperiode`() {
        withSøknad { besvar ->
            besvar("20", 12.januar) // Dagens dato
            besvar("1", 14.februar) // Ønsket dato
            besvar("2", 5.januar)
            besvar("3", 5.januar)
            besvar("10", 12.januar) // Søknadstidspunkt
            besvar("16", listOf(listOf("18.1" to 1.januar, "19.1" to 30.januar)))
            besvar("21", 5.februar)
            besvar("22", 5.mars)

            assertFalse(gjeldendeResultat())
        }
    }

    private fun assertGjeldendeSeksjon(expected: String) =
        assertEquals(expected, testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText())

    private fun gjeldendeResultat() = testRapid.inspektør.field(testRapid.inspektør.size - 1, "resultat").asBoolean()

    private fun withSøknad(
        block: (
            besvar: (faktumId: String, svar: Any) -> Unit,
        ) -> Unit
    ) {
        val søknadsId = søknad()
        block { b: String, c: Any ->
            when (c) {
                is Inntekt -> besvarInntekt(søknadsId, b, c)
                is List<*> -> besvarGenerator(søknadsId, b, c as List<List<Pair<String, Any>>>)
                else -> besvar(søknadsId, b, c)
            }
        }
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
            }
            """.trimIndent()
        )
    }

    private fun besvarInntekt(søknadsId: String, faktumId: String, svar: Inntekt) {
        val årligInntekt: Double = svar.reflection { d, _, _, _ -> return@reflection d }
        //language=JSON
        testRapid.sendTestMessage(
            """{
              "søknad_uuid": "$søknadsId",
              "@event_name": "faktum_svar",
              "fakta": [{
                "id": "$faktumId",
                "svar": "$årligInntekt",
                "clazz": "inntekt"
            }
              ],
              "@opprettet": "${LocalDateTime.now()}",
              "@id": "${UUID.randomUUID()}"
            }
            """.trimIndent()
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
            }
            """.trimIndent()
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
            }
            """.trimIndent()
        )
        return testRapid.inspektør.field(0, "søknad_uuid").asText()
    }
}
