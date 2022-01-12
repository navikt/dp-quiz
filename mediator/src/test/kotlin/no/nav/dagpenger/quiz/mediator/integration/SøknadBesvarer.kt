package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions
import java.time.LocalDateTime
import java.util.UUID

abstract class SøknadBesvarer {

    protected lateinit var testRapid: TestRapid

    protected fun assertGjeldendeSeksjon(expected: String) =
        Assertions.assertEquals(
            expected,
            testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText()
        )

    protected fun gjeldendeResultat() = testRapid.inspektør.field(testRapid.inspektør.size - 1, "resultat").asBoolean()

    protected fun gjeldendeFakta(id: String) =
        testRapid.inspektør.field(testRapid.inspektør.size - 1, "fakta").find { it["id"].asText() == id }?.get("svar")
            ?.asBoolean()

    @Suppress("UNCHECKED_CAST")
    protected fun withSøknad(
        event: String,
        block: (
            besvar: (faktumId: Int, svar: Any) -> Unit,
        ) -> Unit
    ) {
        val søknadsId = søknad(event)
        block { faktumId: Int, svar: Any ->
            val _faktumId = faktumId.toString()
            when (svar) {
                is Inntekt -> besvarInntekt(søknadsId, _faktumId, svar)
                is Dokument -> besvarDokument(søknadsId, _faktumId, svar)
                is List<*> -> besvarGenerator(søknadsId, _faktumId, svar as List<List<Pair<String, Any>>>)
                else -> besvar(søknadsId, _faktumId, svar)
            }
        }
    }

    protected fun besvarDokument(søknadsId: String, faktumId: String, svar: Dokument) {
        val (lastOppTidsstempel, url) = svar.reflection { localDateTime, url -> Pair(localDateTime, url) }
        //language=JSON
        testRapid.sendTestMessage(
            """{
              "søknad_uuid": "$søknadsId",
              "@event_name": "faktum_svar",
              "fakta": [{
                "id": "$faktumId",
                "svar": {
                    "lastOppTidsstempel": "$lastOppTidsstempel",
                    "ur": "$url"
                },
                "clazz": "inntekt"
            }
              ],
              "@opprettet": "${LocalDateTime.now()}",
              "@id": "${UUID.randomUUID()}"
            }
            """.trimIndent()
        )
    }

    protected fun besvar(søknadsId: String, faktumId: String, svar: Any) {
        //language=JSON
        testRapid.sendTestMessage(
            """{
              "søknad_uuid": "$søknadsId",
              "@event_name": "faktum_svar",
              "fakta": [{
                "id": "$faktumId",
                "svar": "$svar",
                "clazz": "${svar::class.java.simpleName.lowercase()}"
            }
              ],
              "@opprettet": "${LocalDateTime.now()}",
              "@id": "${UUID.randomUUID()}"
            }
            """.trimIndent()
        )
    }

    protected fun besvarInntekt(søknadsId: String, faktumId: String, svar: Inntekt) {
        val årligInntekt: Double = svar.reflection { årlig, _, _, _ -> return@reflection årlig }
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

    protected fun besvarGenerator(søknadsId: String, faktumId: String, svar: List<List<Pair<String, Any>>>) {
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

    protected fun lagSvar(faktumId: String, svar: Any) =
        """{"id": "$faktumId", "svar": "$svar", "clazz": "${svar::class.java.simpleName.lowercase()}"}"""

    protected fun søknad(
        event: String
    ): String {
        testRapid.sendTestMessage(event)
        return testRapid.inspektør.field(0, "søknad_uuid").asText()
    }
}
