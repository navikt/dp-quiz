package no.nav.dagpenger.quiz.mediator.integration

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.faktum.Valg
import org.junit.jupiter.api.Assertions
import java.time.LocalDateTime
import java.util.UUID

abstract class SøknadBesvarer {
    protected lateinit var testRapid: TestRapid

    protected fun assertGjeldendeSeksjon(expected: String) =
        Assertions.assertEquals(
            expected,
            testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText(),
        )

    protected fun gjeldendeResultat() = testRapid.inspektør.field(testRapid.inspektør.size - 1, "resultat").asBoolean()

    protected fun gjeldendeFakta(id: String) =
        testRapid.inspektør.field(testRapid.inspektør.size - 1, "fakta").find { it["id"].asText() == id }?.get("svar")
            ?.asBoolean()

    protected fun melding(indeks: Int) = testRapid.inspektør.message(indeks)

    @Suppress("UNCHECKED_CAST")
    protected fun withSøknad(
        førsteEvent: String,
        block: (
            besvar: (faktumId: Int, svar: Any) -> Unit,
        ) -> Unit,
    ) {
        val søknadsId = triggNySøknadsprosess(førsteEvent)
        block { faktumId: Int, svar: Any ->
            besvar(søknadsId, faktumId, svar)
        }
    }

    protected fun besvar(
        søknadsId: String,
        faktumId: Int,
        svar: Any,
    ) = when (svar) {
        is Inntekt -> besvarInntekt(søknadsId, faktumId, svar)
        is Periode -> besvarPeriode(søknadsId, faktumId, svar)
        is Tekst -> besvarTekst(søknadsId, faktumId, svar)
        is Envalg, is Flervalg -> besvarValg(søknadsId, faktumId, svar as Valg)
        is Dokument -> besvarDokument(søknadsId, faktumId, svar)
        is List<*> -> besvarGenerator(søknadsId, faktumId, svar as List<List<Pair<String, Any>>>)
        is Land -> besvarLand(søknadsId, faktumId, svar)
        else -> besvarSimpeltFaktum(søknadsId, faktumId, svar)
    }

    protected fun withSøknadsId(
        søknadsId: UUID = UUID.randomUUID(),
        block: (
            besvar: (faktumId: Int, svar: Any) -> Unit,
        ) -> Unit,
    ) {
        block { faktumId: Int, svar: Any ->
            besvar(søknadsId.toString(), faktumId, svar)
        }
    }

    protected fun besvarDokument(
        søknadsId: String,
        faktumId: Int,
        svar: Dokument,
    ) {
        //language=JSON
        val message =
            svar.reflection { lastOppTidsstempel, urn: String ->
                """
                {
                  "søknad_uuid": "$søknadsId",
                  "@event_name": "faktum_svar",
                  "fakta": [{
                    "id": "$faktumId",
                    "svar": {
                        "lastOppTidsstempel": "$lastOppTidsstempel",
                        "urn": "$urn"
                    },
                    "type": "inntekt"
                }
                  ],
                  "@opprettet": "${LocalDateTime.now()}",
                  "@id": "${UUID.randomUUID()}"
                }
                """.trimIndent()
            }
        testRapid.sendTestMessage(message)
    }

    private fun besvarSimpeltFaktum(
        søknadsId: String,
        faktumId: Int,
        svar: Any,
    ) {
        //language=JSON
        val message =
            """
            {
              "søknad_uuid": "$søknadsId",
              "@event_name": "faktum_svar",
              "fakta": [{
                "id": "$faktumId",
                "svar": "$svar",
                "type": "${svar::class.java.simpleName.lowercase()}"
            }
              ],
              "@opprettet": "${LocalDateTime.now()}",
              "@id": "${UUID.randomUUID()}"
            }
            """.trimIndent()
        testRapid.sendTestMessage(message)
    }

    protected fun besvarLand(
        søknadsId: String,
        faktumId: Int,
        svar: Land,
    ) {
        //language=JSON
        val message =
            """
            {
              "søknad_uuid": "$søknadsId",
              "@event_name": "faktum_svar",
              "fakta": [{
                "id": "$faktumId",
                "svar": "${svar.alpha3Code}",
                "type": "land"
            }
              ],
              "@opprettet": "${LocalDateTime.now()}",
              "@id": "${UUID.randomUUID()}"
            }
            """.trimIndent()
        testRapid.sendTestMessage(message)
    }

    protected fun besvarTekst(
        søknadsId: String,
        faktumId: Int,
        svar: Tekst,
    ) {
        //language=JSON
        val message =
            """
            {
              "søknad_uuid": "$søknadsId",
              "@event_name": "faktum_svar",
              "fakta": [{
                "id": "$faktumId",
                "svar": "${svar.verdi}",
                "type": "tekst"
            }
              ],
              "@opprettet": "${LocalDateTime.now()}",
              "@id": "${UUID.randomUUID()}"
            }
            """.trimIndent()
        testRapid.sendTestMessage(message)
    }

    protected fun besvarValg(
        søknadsId: String,
        faktumId: Int,
        svar: Valg,
    ) {
        //language=JSON
        val message =
            """
            {
              "søknad_uuid": "$søknadsId",
              "@event_name": "faktum_svar",
              "fakta": [{
                "id": "$faktumId",
                "svar": ["${svar.joinToString("""","""")}"],
                "type": "${svar::class.java.simpleName.lowercase()}"
            }
              ],
              "@opprettet": "${LocalDateTime.now()}",
              "@id": "${UUID.randomUUID()}"
            }
            """.trimIndent()
        testRapid.sendTestMessage(message)
    }

    protected fun besvarPeriode(
        søknadsId: String,
        faktumId: Int,
        svar: Periode,
    ) {
        //language=JSON
        val message =
            """
            {
              "søknad_uuid": "$søknadsId",
              "@event_name": "faktum_svar",
              "fakta": [{
                "id": "$faktumId",
                "svar": { "fom": "${svar.fom}", "tom": "${svar.tom}"  },
                "type": "periode"
            }
              ],
              "@opprettet": "${LocalDateTime.now()}",
              "@id": "${UUID.randomUUID()}"
            }
            """.trimIndent()
        testRapid.sendTestMessage(message)
    }

    protected fun besvarInntekt(
        søknadsId: String,
        faktumId: Int,
        svar: Inntekt,
    ) {
        val årligInntekt: Double = svar.reflection { årlig, _, _, _ -> return@reflection årlig }
        //language=JSON
        val message =
            """
            {
              "søknad_uuid": "$søknadsId",
              "@event_name": "faktum_svar",
              "fakta": [{
                "id": "$faktumId",
                "svar": "$årligInntekt",
                "type": "inntekt"
            }
              ],
              "@opprettet": "${LocalDateTime.now()}",
              "@id": "${UUID.randomUUID()}"
            }
            """.trimIndent()
        testRapid.sendTestMessage(message)
    }

    protected fun besvarGenerator(
        søknadsId: String,
        faktumId: Int,
        svar: List<List<Pair<String, Any>>>,
    ) {
        val alleSvar =
            svar.map { faktum ->
                faktum.map { besvarelse ->
                    val templateId = besvarelse.first
                    val svarverdi = besvarelse.second
                    lagSvar(templateId, svarverdi)
                }
            }
        val fakta = mutableListOf("""{"id": "$faktumId", "svar": $alleSvar, "type": "generator"}""")
        //language=JSON
        val message =
            """
            {
              "søknad_uuid": "$søknadsId",
              "@event_name": "faktum_svar",
              "fakta": $fakta,
              "@opprettet": "${LocalDateTime.now()}",
              "@id": "${UUID.randomUUID()}"
            }
            """.trimIndent()
        testRapid.sendTestMessage(message)
    }

    protected fun lagSvar(
        faktumId: String,
        svar: Any,
    ): String {
        return when (svar) {
            is Periode -> lagPeriodeGeneratorSvar(svar, faktumId)
            is Tekst -> """{"id": "$faktumId", "svar": "${svar.verdi}", "type": "${svar::class.java.simpleName.lowercase()}"}"""
            is Envalg, is Flervalg -> lagValgGeneratorSvar(svar as Valg, faktumId)
            else -> """{"id": "$faktumId", "svar": "$svar", "type": "${svar::class.java.simpleName.lowercase()}"}"""
        }
    }

    private fun lagValgGeneratorSvar(
        svar: Valg,
        faktumId: String,
    ): String {
        val valgene = """["${svar.joinToString("""","""")}"]"""
        return """{"id": "$faktumId", "svar": $valgene, "type": "${svar::class.java.simpleName.lowercase()}"}"""
    }

    private fun lagPeriodeGeneratorSvar(
        svar: Periode,
        faktumId: String,
    ): String {
        val perioden =
            svar.reflection { fom, tom ->
                """
                {
                "fom": "$fom",
                "tom": "$tom"          
                }
                """.trimIndent()
            }
        return """{"id": "$faktumId", "svar": $perioden, "type": "${svar::class.java.simpleName.lowercase()}"}"""
    }

    protected fun triggNySøknadsprosess(event: String): String {
        testRapid.sendTestMessage(event)
        return testRapid.inspektør.field(0, "søknad_uuid").asText()
    }
}
