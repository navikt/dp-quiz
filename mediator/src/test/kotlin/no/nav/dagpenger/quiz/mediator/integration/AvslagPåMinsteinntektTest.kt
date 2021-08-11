package no.nav.dagpenger.quiz.mediator.integration

import no.finn.unleash.FakeUnleash
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.quiz.mediator.FEATURE_MOTTA_SØKNAD
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.desember
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.MottattSøknadService
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.behandlingsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFiskInntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fortsattRettKorona
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.grunnbeløp
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harInntektNesteKalendermåned
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.hattLukkedeSakerSiste8Uker
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.helseTilAlleTyperJobb
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.jobbetUtenforNorge
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.kanJobbeDeltid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.kanJobbeHvorSomHelst
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lærling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lønnsgaranti
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.minsteinntektfaktor12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.minsteinntektfaktor36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.ordinær
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.over67årFradato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.permittert
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.permittertFiskeforedling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPerioder
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknadstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.villigTilÅBytteYrke
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
            val søknadPersistence = SøknadRecord()
            val resultatPersistence = ResultatRecord()
            val unleash = FakeUnleash().also { it.enable(FEATURE_MOTTA_SØKNAD) }
            testRapid = TestRapid().also {
                FaktumSvarService(
                    søknadPersistence = søknadPersistence,
                    resultatPersistence = resultatPersistence,
                    rapidsConnection = it,
                    unleash = unleash
                )
                MottattSøknadService(søknadPersistence, it, unleash, AvslagPåMinsteinntektOppsett.VERSJON_ID)
            }
        }
    }

    @Test
    fun `De som har virkningsdato for langt fram i tid går til manuell`() {
        withSøknad { besvar ->
            besvar(behandlingsdato, 5.januar)
            besvar(ønsketDato, 5.desember)
            besvar(søknadstidspunkt, 5.desember)
            besvar(senesteMuligeVirkningsdato, 19.januar)
            assertGjeldendeSeksjon("arbeidsforhold")
            besvar(
                antallEndredeArbeidsforhold,
                listOf(
                    listOf(
                        "$ordinær.1" to false,
                        "$permittert.1" to true,
                        "$lønnsgaranti.1" to false,
                        "$permittertFiskeforedling.1" to false
                    )
                )
            )
            assertGjeldendeSeksjon("virkningstidspunkt vi ikke kan håndtere")
        }
    }

    @Test
    fun `De som ikke oppfyller kravet til minsteinntekt får avslag`() {
        withSøknad { besvar ->

            assertGjeldendeSeksjon("arbeidsforhold")
            besvar(
                antallEndredeArbeidsforhold,
                listOf(
                    listOf(
                        "$ordinær.1" to false,
                        "$permittert.1" to true,
                        "$lønnsgaranti.1" to false,
                        "$permittertFiskeforedling.1" to false
                    ),
                    listOf(
                        "$ordinær.2" to false,
                        "$permittert.2" to true,
                        "$lønnsgaranti.2" to false,
                        "$permittertFiskeforedling.2" to false
                    ),

                )
            )

            besvar(behandlingsdato, 5.januar)
            besvar(senesteMuligeVirkningsdato, 19.januar)

            assertGjeldendeSeksjon("datafrasøknad")
            besvar(eøsArbeid, false)
            besvar(jobbetUtenforNorge, false)
            besvar(fangstOgFiskInntektSiste36mnd, false)
            besvar(verneplikt, false)
            besvar(lærling, false)
            besvar(ønsketDato, 5.januar)
            besvar(søknadstidspunkt, 2.januar)
            besvar(harInntektNesteKalendermåned, false)
            besvar(kanJobbeDeltid, true)
            besvar(helseTilAlleTyperJobb, true)
            besvar(villigTilÅBytteYrke, true)
            besvar(kanJobbeHvorSomHelst, true)
            besvar(fortsattRettKorona, false)

            assertGjeldendeSeksjon("alder")
            besvar(over67årFradato, 1.desember)

            assertGjeldendeSeksjon("inntektsrapporteringsperioder")
            besvar(inntektsrapporteringsperiodeTom, 10.januar)

            assertGjeldendeSeksjon("inntektshistorikk")
            besvar(fangstOgFiskInntektSiste36mnd, false)

            assertGjeldendeSeksjon("dagpengehistorikk")
            besvar(harHattDagpengerSiste36mnd, false)
            besvar(hattLukkedeSakerSiste8Uker, false)

            assertGjeldendeSeksjon("sykepengehistorikk")
            besvar(sykepengerSiste36mnd, false)

            assertGjeldendeSeksjon("arbeidsøkerperioder")
            besvar(
                registrertArbeidssøkerPerioder,
                listOf(
                    listOf(
                        "$registrertArbeidssøkerPeriodeFom.1" to 1.januar(2018),
                        "$registrertArbeidssøkerPeriodeTom.1" to 30.januar(2018)
                    )
                )
            )

            assertGjeldendeSeksjon("minsteinntektKonstanter")
            besvar(minsteinntektfaktor36mnd, 1.5)
            besvar(minsteinntektfaktor12mnd, 3.0)
            besvar(grunnbeløp, 100000.årlig)

            assertGjeldendeSeksjon("inntekter")
            besvar(inntektSiste36mnd, 20000.årlig)
            besvar(inntektSiste12mnd, 5000.årlig)

            assertFalse(gjeldendeResultat())

            assertFalse(gjeldendeFakta("27.1")!!)
        }
    }

    private fun assertGjeldendeSeksjon(expected: String) =
        assertEquals(expected, testRapid.inspektør.field(testRapid.inspektør.size - 1, "seksjon_navn").asText())

    private fun gjeldendeResultat() = testRapid.inspektør.field(testRapid.inspektør.size - 1, "resultat").asBoolean()

    private fun gjeldendeFakta(id: String) = testRapid.inspektør.field(testRapid.inspektør.size - 1, "fakta").find { it["id"].asText() == id }?.get("svar")?.asBoolean()

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
              "@event_name": "innsending_ferdigstilt",
              "fødselsnummer": "123456789",
              "aktørId": "",
              "søknadsId": "9876",
              "journalpostId": "493389306",
              "type": "NySøknad",
              "søknadsData": {
            "brukerBehandlingId": "10010WQMW"
          }
            }
            """.trimIndent()
        )
        return testRapid.inspektør.field(0, "søknad_uuid").asText()
    }
}
