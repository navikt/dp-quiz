package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VilkårsvurderingLøserTest {
    private val testRapid = TestRapid()

    @Test
    fun `mottar behov om vilkårsvurdering av alder`() {
        testRapid.sendTestMessage(innsendingFerdigstiltJson)
        assertEquals(1, testRapid.inspektør.size)
    }

    @Language("JSON")
    val innsendingFerdigstiltJson =
        """{
          "@id": "3b85fff6-dee8-4ea2-a13b-096b85d8b592",
          "@opprettet": "2021-05-07T11:14:11.502435",
          "@event_name": "vilkårsvurdering",
          "@vilkår": ["øvreAldersgrense"]
        }"""
}
