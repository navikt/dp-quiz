package no.nav.dagpenger.quiz.mediator.behovløsere

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.quiz.mediator.meldinger.ProsessRepositoryFake
import no.nav.dagpenger.quiz.mediator.soknad.Prosesser
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DinSituasjon
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.UUID

class VirkningsdatoerBehovLøserTest {
    private val identer = Identer.Builder().folkeregisterIdent("12020052345").aktørId("aktørId").build()
    private val prosessRepository = ProsessRepositoryFake(Prosesser.Søknad, Dagpenger.VERSJON_ID)
    private val rapid = TestRapid().apply {
        VirkningsdatoerBehovLøser(this, prosessRepository)
    }

    @Test
    fun `at vi får ønsketdato som del av løsningen`() {
        val prosessId = UUID.randomUUID()
        prosessRepository.ny(identer, Prosesser.Søknad, prosessId).also {
            it.fakta.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
                .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
            it.fakta.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)
            prosessRepository.lagre(it)
        }
        //language=JSON
        rapid.sendTestMessage(
            """{
              "@event_name": "behov",
              "@behov": [ "Virkningsdatoer" ],
              "søknad_uuid": "$prosessId"
            }
            """.trimIndent(),
        )

        with(rapid.inspektør) {
            assertNotNull(field(0, "@løsning"))
            assertEquals(1.januar, field(0, "@løsning")["Virkningsdatoer"]["ønsketdato"].asLocalDate())
        }
    }

    @Test
    fun `at vi får ønsketdato som del av løsningen for gjenopptak`() {
        val gjenopptakProsessId = UUID.randomUUID()
        prosessRepository.ny(identer, Prosesser.Søknad, gjenopptakProsessId).also {
            it.fakta.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
                .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
            it.fakta.dato(DinSituasjon.`gjenopptak søknadsdato`).besvar(2.januar)
            prosessRepository.lagre(it)
        }
        //language=JSON
        rapid.sendTestMessage(
            """{
              "@event_name": "behov",
              "@behov": [ "Virkningsdatoer" ],
              "søknad_uuid": "$gjenopptakProsessId"
            }
            """.trimIndent(),
        )

        with(rapid.inspektør) {
            assertNotNull(field(0, "@løsning"))
            assertEquals(2.januar, field(0, "@løsning")["Virkningsdatoer"]["ønsketdato"].asLocalDate())
        }
    }
}
