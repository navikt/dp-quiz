package no.nav.dagpenger.quiz.mediator.meldinger

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering.Paragraf_4_23_alder_oppsett
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID

class VilkårsvurderingLøserTest {

    private lateinit var søknadsprosess: Søknadprosess
    private lateinit var testRapid: TestRapid

    @BeforeEach
    fun setup() {
        Paragraf_4_23_alder_oppsett.registrer { prototypeSøknad ->
            søknadsprosess = Versjon.id(Paragraf_4_23_alder_oppsett.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }

        val prosessPersistens = mockk<SøknadPersistence>().also {
            every { it.ny(any(), any(), any(), any()) } returns søknadsprosess
            every { it.lagre(any() as Søknad) } returns true
        }

        testRapid = TestRapid().also {
            VilkårsvurderingLøser(
                prosessPersistence = prosessPersistens,
                rapidsConnection = it
            )
        }
    }

    @Test
    fun `mottar behov om vilkårsvurdering av alder`() {
        testRapid.sendTestMessage(`behov om vurdering av paragraf 4-23 alder`)
        assertEquals(2, testRapid.inspektør.size)
        assertDoesNotThrow {
            testRapid.inspektør.field(1, "@løsning")["Paragraf_4_23_alder"].asText().let { UUID.fromString(it) }
        }
    }

    @Language("JSON")
    val `behov om vurdering av paragraf 4-23 alder` =
        """{
          "@id": "3b85fff6-dee8-4ea2-a13b-096b85d8b592",
          "@opprettet": "2021-05-07T11:14:11.502435",
          "@event_name": "vilkårsvurdering",
          "@behov": ["Paragraf_4_23_alder"],
          "ident": "12345123456",
          "behandlingId": "${UUID.randomUUID()}"
        }"""
}
