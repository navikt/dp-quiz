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
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.G1_5
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.G3
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.dagensDato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.fangstOgFisk
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.godkjenningRettighetstype
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.godkjenningSisteDagMedLønn
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.grenseDato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.inntektsrapporteringsperiodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.lærling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.registreringsperioder
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.sisteDagMedLønn
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.sluttårsaker
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.søknadstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.ønsketDato
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
            besvar(dagensDato, 5.januar)
            besvar(grenseDato, 19.januar)
            besvar(ønsketDato, 5.januar)
            besvar(sisteDagMedLønn, 5.januar)
            besvar(søknadstidspunkt, 2.januar)
            besvar(inntektsrapporteringsperiodeFom, 5.januar)
            besvar(inntektsrapporteringsperiodeTom, 5.februar)
            besvar(registreringsperioder, listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))

            assertGjeldendeSeksjon("ytelsehistorikk")
            besvar(harHattDagpengerSiste36mnd, false)
            besvar(sykepengerSiste36mnd, false)

            assertGjeldendeSeksjon("eøsArbeid")
            besvar(eøsArbeid, false)

            assertGjeldendeSeksjon("fangstOgFisk")
            besvar(fangstOgFisk, false)

            assertGjeldendeSeksjon("grunnbeløp")
            besvar(G3, 300000.årlig)
            besvar(G1_5, 150000.årlig)

            assertGjeldendeSeksjon("inntektsunntak")
            besvar(verneplikt, false)
            besvar(lærling, false)

            assertGjeldendeSeksjon("inntekter")
            besvar(inntektSiste36mnd, 20000.årlig)
            besvar(inntektSiste12mnd, 5000.årlig)

            assertGjeldendeSeksjon("rettighetstype")
            besvar(sluttårsaker, listOf(listOf("24.1" to false, "25.1" to true, "26.1" to false, "27.1" to false)))

            assertGjeldendeSeksjon("godkjenn rettighetstype")
            besvar(godkjenningRettighetstype, true)

            assertEquals(
                "godkjenn virkningstidspunkt",
                testRapid.inspektør.field(testRapid.inspektør.size - 2, "seksjon_navn").asText()
            )

            besvar(godkjenningSisteDagMedLønn, true)
            assertEquals(26, testRapid.inspektør.size)
            assertFalse(gjeldendeResultat())
        }
    }

    @Test
    fun `Søknader fra brukere som har hatt dagpeneger de siste 36 månedene blir ikke behandlet `() {
        withSøknad { besvar ->
            besvar(dagensDato, 5.januar)
            besvar(grenseDato, 19.januar)
            besvar(ønsketDato, 5.januar)
            besvar(sisteDagMedLønn, 5.januar)
            besvar(søknadstidspunkt, 2.januar)
            besvar(inntektsrapporteringsperiodeFom, 5.januar)
            besvar(inntektsrapporteringsperiodeTom, 5.februar)
            besvar(registreringsperioder, listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))

            assertGjeldendeSeksjon("ytelsehistorikk")

            testRapid.inspektør.size.also {
                besvar(harHattDagpengerSiste36mnd, true)
                assertEquals((it + 1), testRapid.inspektør.size)
                assertGjeldendeSeksjon("mulig gjenopptak")
            }
        }
    }

    @Test
    fun `De som oppfyller kravet til minsteinntekt gir ingen seksjoner til saksbehandler`() {
        withSøknad { besvar ->
            besvar(dagensDato, 5.januar)
            besvar(grenseDato, 19.januar)
            besvar(ønsketDato, 5.januar)
            besvar(sisteDagMedLønn, 5.januar)
            besvar(søknadstidspunkt, 2.januar)
            besvar(inntektsrapporteringsperiodeFom, 5.januar)
            besvar(inntektsrapporteringsperiodeTom, 5.februar)
            besvar(registreringsperioder, listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))

            assertGjeldendeSeksjon("ytelsehistorikk")
            besvar(harHattDagpengerSiste36mnd, false)
            besvar(sykepengerSiste36mnd, false)

            assertGjeldendeSeksjon("eøsArbeid")
            besvar(eøsArbeid, false)

            assertGjeldendeSeksjon("fangstOgFisk")
            besvar(fangstOgFisk, false)

            assertGjeldendeSeksjon("grunnbeløp")
            besvar(G3, 300000.årlig)
            besvar(G1_5, 150000.årlig)

            assertGjeldendeSeksjon("inntektsunntak")
            besvar(verneplikt, false)
            besvar(lærling, false)

            assertGjeldendeSeksjon("inntekter")
            besvar(inntektSiste36mnd, 2000000.årlig)
            besvar(inntektSiste12mnd, 5000000.årlig)

            assertGjeldendeSeksjon("rettighetstype")
            besvar(sluttårsaker, listOf(listOf("24.1" to false, "25.1" to true, "26.1" to false, "27.1" to false)))

            assertTrue(gjeldendeResultat(), "gjeldende resultat er false")
        }
    }

    @Test
    fun `De som har vært lærling gir ingen seksjoner til saksbehandler`() {
        withSøknad { besvar ->
            besvar(dagensDato, 5.januar)
            besvar(grenseDato, 19.januar)
            besvar(ønsketDato, 5.januar)
            besvar(sisteDagMedLønn, 5.januar)
            besvar(søknadstidspunkt, 2.januar)
            besvar(inntektsrapporteringsperiodeFom, 5.januar)
            besvar(inntektsrapporteringsperiodeTom, 5.februar)
            besvar(registreringsperioder, listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))

            assertGjeldendeSeksjon("ytelsehistorikk")
            besvar(harHattDagpengerSiste36mnd, false)
            besvar(sykepengerSiste36mnd, false)

            assertGjeldendeSeksjon("eøsArbeid")
            besvar(eøsArbeid, false)

            assertGjeldendeSeksjon("fangstOgFisk")
            besvar(fangstOgFisk, false)

            assertGjeldendeSeksjon("grunnbeløp")
            besvar(G3, 300000.årlig)
            besvar(G1_5, 150000.årlig)

            assertGjeldendeSeksjon("inntektsunntak")
            besvar(verneplikt, false)
            besvar(lærling, true)

            assertGjeldendeSeksjon("inntekter")
            besvar(inntektSiste36mnd, 20000.årlig)
            besvar(inntektSiste12mnd, 5000.årlig)

            assertGjeldendeSeksjon("rettighetstype")
            besvar(sluttårsaker, listOf(listOf("24.1" to false, "25.1" to true, "26.1" to false, "27.1" to false)))

            assertTrue(gjeldendeResultat(), "gjeldende resultat er false")
        }
    }

    @Test
    fun `Skal ikke gi oppgaver til saksbehandler når dagens dato mer enn 14 dager før virkningstidspunkt`() {
        withSøknad { besvar ->
            besvar(dagensDato, 1.januar)
            besvar(grenseDato, 15.januar)
            besvar(ønsketDato, 20.januar)
            besvar(sisteDagMedLønn, 5.januar)
            besvar(søknadstidspunkt, 2.januar)
            besvar(registreringsperioder, listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))
            besvar(inntektsrapporteringsperiodeFom, 5.januar)
            besvar(inntektsrapporteringsperiodeTom, 5.februar)

            assertGjeldendeSeksjon("datoer manuell")
        }
    }

    @Test
    fun `Skal gå videre om virkningstidspunkt er fram i tid i samme rapporteringsperiode`() {
        withSøknad { besvar ->
            besvar(dagensDato, 12.januar)
            besvar(grenseDato, 19.januar)
            besvar(ønsketDato, 14.januar)
            besvar(sisteDagMedLønn, 5.januar)
            besvar(søknadstidspunkt, 12.januar)
            besvar(registreringsperioder, listOf(listOf("18.1" to 1.januar, "19.1" to 30.januar)))
            besvar(inntektsrapporteringsperiodeFom, 5.januar)
            besvar(inntektsrapporteringsperiodeTom, 5.februar)
            besvar(harHattDagpengerSiste36mnd, false)
            besvar(sykepengerSiste36mnd, false)
            besvar(eøsArbeid, false)

            assertGjeldendeSeksjon("fangstOgFisk")
        }
    }

    @Test
    fun `Skal ikke gå videre om virkningstidspunkt er fram i tid, men i annen rapporteringsperiode`() {
        withSøknad { besvar ->
            besvar(dagensDato, 3.februar)
            besvar(grenseDato, 17.februar)
            besvar(ønsketDato, 14.februar)
            besvar(sisteDagMedLønn, 5.februar)
            besvar(søknadstidspunkt, 2.februar)
            besvar(inntektsrapporteringsperiodeFom, 5.januar)
            besvar(inntektsrapporteringsperiodeTom, 5.februar)
            besvar(registreringsperioder, listOf(listOf("18.1" to 1.januar, "19.1" to 30.januar)))

            assertGjeldendeSeksjon("datoer manuell")
        }
    }

    @Test
    fun `Skal ikke gi oppgaver til saksbehandler hvis har sykepenger`() {
        withSøknad { besvar ->
            besvar(dagensDato, 5.januar)
            besvar(grenseDato, 19.januar)
            besvar(ønsketDato, 5.januar)
            besvar(sisteDagMedLønn, 5.januar)
            besvar(søknadstidspunkt, 2.januar)
            besvar(inntektsrapporteringsperiodeFom, 5.januar)
            besvar(inntektsrapporteringsperiodeTom, 5.februar)
            besvar(registreringsperioder, listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))

            besvar(sluttårsaker, listOf(listOf("24.1" to false, "25.1" to true, "26.1" to false, "27.1" to false)))
            besvar(harHattDagpengerSiste36mnd, false)
            besvar(sykepengerSiste36mnd, true)
            besvar(eøsArbeid, false)
            besvar(fangstOgFisk, false)

            assertGjeldendeSeksjon("svangerskapsrelaterte sykepenger")
        }
    }

    @Test
    fun `Fangst og fisk skal manuelt behandles`() {
        withSøknad { besvar ->
            besvar(dagensDato, 5.januar)
            besvar(grenseDato, 19.januar)
            besvar(ønsketDato, 5.januar)
            besvar(sisteDagMedLønn, 5.januar)
            besvar(søknadstidspunkt, 2.januar)
            besvar(inntektsrapporteringsperiodeFom, 5.januar)
            besvar(inntektsrapporteringsperiodeTom, 5.februar)
            besvar(registreringsperioder, listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))

            besvar(sluttårsaker, listOf(listOf("24.1" to false, "25.1" to true, "26.1" to false, "27.1" to false)))
            besvar(harHattDagpengerSiste36mnd, false)
            besvar(eøsArbeid, false)
            besvar(fangstOgFisk, true)

            assertGjeldendeSeksjon("fangst og fisk manuell")
        }
    }

    @Test
    fun `Eøs arbeid skal manuelt behandles`() {
        withSøknad { besvar ->
            besvar(dagensDato, 5.januar)
            besvar(grenseDato, 19.januar)
            besvar(ønsketDato, 5.januar)
            besvar(sisteDagMedLønn, 5.januar)
            besvar(søknadstidspunkt, 2.januar)
            besvar(inntektsrapporteringsperiodeFom, 5.januar)
            besvar(inntektsrapporteringsperiodeTom, 5.februar)
            besvar(registreringsperioder, listOf(listOf("18.1" to 1.januar(2018), "19.1" to 30.januar(2018))))

            besvar(sluttårsaker, listOf(listOf("24.1" to false, "25.1" to true, "26.1" to false, "27.1" to false)))
            besvar(harHattDagpengerSiste36mnd, false)
            besvar(eøsArbeid, true)
            assertGjeldendeSeksjon("Eøs arbeid manuell")
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
