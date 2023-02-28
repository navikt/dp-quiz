package no.nav.dagpenger.quiz.mediator.meldinger

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.model.seksjon.FaktaVersjonDingseboms
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository
import no.nav.dagpenger.quiz.mediator.helpers.testPerson
import no.nav.dagpenger.quiz.mediator.soknad.Prosesser
import no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering.Paragraf_4_23_alder_oppsett
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID
import java.util.UUID.randomUUID
import kotlin.test.assertTrue

class VilkårsvurderingLøserTest {
    private lateinit var søknadsprosess: Prosess
    private lateinit var testRapid: TestRapid
    private val vilkårsvurderingIdSlot = slot<UUID>()

    @BeforeEach
    fun setup() {
        Paragraf_4_23_alder_oppsett.registrer()
        søknadsprosess = FaktaVersjonDingseboms.prosess(testPerson, Prosesser.Paragraf_4_23_alder)

        val prosessPersistens = mockk<ProsessRepository>().also {
            every { it.ny(any(), any(), capture(vilkårsvurderingIdSlot), any()) } returns søknadsprosess
            every { it.lagre(any() as Prosess) } returns true
        }

        testRapid = TestRapid().also {
            VilkårsvurderingLøser(
                rapidsConnection = it,
                prosessPersistence = prosessPersistens,
            )
        }
    }

    @Test
    fun `mottar behov om vilkårsvurdering av alder`() {
        val vilkårsvurderingId = randomUUID()
        testRapid.sendTestMessage(`behov om vurdering av paragraf 4-23 alder`(vilkårsvurderingId = vilkårsvurderingId))
        assertTrue { vilkårsvurderingIdSlot.isCaptured }
        assertEquals(vilkårsvurderingId, vilkårsvurderingIdSlot.captured)
        assertEquals(2, testRapid.inspektør.size)
        assertDoesNotThrow {
            testRapid.inspektør.field(1, "@løsning")["Paragraf_4_23_alder"].asText().let { UUID.fromString(it) }
        }
    }

    @Language("JSON")
    fun `behov om vurdering av paragraf 4-23 alder`(vilkårsvurderingId: UUID) = """
        {
          "@event_name": "behov",
          "@behovId": "ee6d82f4-fad0-4d61-8d54-de57db2c0b73",
          "@behov": [
            "Paragraf_4_23_alder"
          ],
          "ident": "12345678911",
          "behandlingId": "c13f867d-0346-4e22-959b-0dfd9deab882",
          "type": "NyRettighetsbehandling",
          "søknad_uuid": "2a1a430c-2579-4a11-9793-0ec7266c9f78",
          "vilkårsvurderingId": "$vilkårsvurderingId",
          "Paragraf_4_23_alder": {},
          "@id": "1b28392d-2623-4d4a-bb90-6f130d724725",
          "@opprettet": "2023-01-20T08:58:47.706275552",
          "system_read_count": 0,
          "system_participating_services": [
            {
              "id": "1b28392d-2623-4d4a-bb90-6f130d724725",
              "time": "2023-01-20T08:58:47.706275552",
              "service": "dp-behandling",
              "instance": "dp-behandling-8579b578db-n5bsz",
              "image": "ghcr.io/navikt/dp-behandling:06f9ecd4ef2c8a0f6cb49055b3535fd096951ebc"
            }
          ]
        }
    """.trimIndent()
}
