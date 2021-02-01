package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.februar
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.NySøknadService
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.G1_5
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.G3
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.behandlingsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFisk
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.godkjenningSisteDagMedLønn
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.godkjenningSluttårsak
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harInntektNesteKalendermåned
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lærling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registreringsperioder
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sisteDagMedLønn
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknadstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.ønsketDato
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class AvslagPåMinsteinntektTest {
    private lateinit var testRapid: TestRapid

    @BeforeEach
    fun setup() {
        Postgres.withMigratedDb {
            AvslagPåMinsteinntektOppsett.registrer { søknad, versjonId -> FaktumTable(søknad, versjonId) }
            val persistence = SøknadRecord()
            testRapid = TestRapid().also {
                FaktumSvarService(
                    søknadPersistence = persistence,
                    rapidsConnection = it
                )
                NySøknadService(persistence, it, AvslagPåMinsteinntektOppsett.VERSJON_ID)
            }
        }
    }

    @Test
    fun `De som ikke oppfyller kravet til minsteinntekt får avslag`() {
        withSøknad { besvar ->
            besvar(behandlingsdato, 5.januar)
            besvar(senesteMuligeVirkningstidspunkt, 19.januar)
            assertGjeldendeSeksjon("datafrasøknad")
            besvar(eøsArbeid, false)
            besvar(fangstOgFisk, false)
            besvar(verneplikt, false)
            besvar(lærling, false)
            besvar(ønsketDato, 5.januar)
            besvar(sisteDagMedLønn, 5.januar)
            besvar(søknadstidspunkt, 2.januar)
            assertGjeldendeSeksjon("inntektsrapporteringsperioder")
            besvar(inntektsrapporteringsperiodeFom, 5.januar)
            besvar(inntektsrapporteringsperiodeTom, 5.februar)
            besvar(harInntektNesteKalendermåned, false)

            assertGjeldendeSeksjon("dagpengehistorikk")
            besvar(harHattDagpengerSiste36mnd, false)

            assertGjeldendeSeksjon("sykepengehistorikk")
            besvar(sykepengerSiste36mnd, false)

            assertGjeldendeSeksjon("arbeidsforhold")
            besvar(antallEndredeArbeidsforhold, listOf(listOf("24.1" to false, "25.1" to true, "26.1" to false, "27.1" to false)))

            assertGjeldendeSeksjon("grunnbeløp")
            besvar(G3, 300000.årlig)
            besvar(G1_5, 150000.årlig)

            assertGjeldendeSeksjon("arbeidsøkerperioder")
            besvar(registreringsperioder, listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))

            assertGjeldendeSeksjon("inntekter")
            besvar(inntektSiste36mnd, 20000.årlig)
            besvar(inntektSiste12mnd, 5000.årlig)

            assertGjeldendeSeksjon("godkjenn sluttårsak")
            besvar(godkjenningSluttårsak, true)

            assertEquals(
                "godkjenn siste dag med lønn",
                testRapid.inspektør.field(testRapid.inspektør.size - 2, "seksjon_navn").asText()
            )

            besvar(godkjenningSisteDagMedLønn, true)
            assertEquals(27, testRapid.inspektør.size)
            assertFalse(gjeldendeResultat())
        }
    }

    private fun assertGjeldendeSeksjon(expected: String) =
        assertEquals(expected, testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText())

    private fun gjeldendeResultat() = testRapid.inspektør.field(testRapid.inspektør.size - 1, "resultat").asBoolean()

    private fun withSøknad(
        block: (
            besvar: (faktumId: Int, svar: Any) -> Unit,
        ) -> Unit
    ) {
        val søknadsId = søknad()
        block { b: Int, c: Any ->
            val faktumId = b.toString()
            when (c) {
                is Inntekt -> besvarInntekt(søknadsId, faktumId, c)
                is List<*> -> besvarGenerator(søknadsId, faktumId, c as List<List<Pair<String, Any>>>)
                else -> besvar(søknadsId, faktumId, c)
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
