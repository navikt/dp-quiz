package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.FaktaVersjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.quiz.mediator.db.FaktaPersistence
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.dagpenger.quiz.mediator.helpers.Testprosess
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class FaktumSvarServiceTest {
    @AfterEach
    fun resetSvar() {
        prototypeFakta.generator(10).besvar(0)
    }

    companion object {
        private val prosessVersjon = FaktaVersjon(Testprosess.Test, -3000)
        val prototypeFakta = Fakta(
            prosessVersjon,
            heltall faktum "generator" id 10 genererer 11 og 12,
            dato faktum "fom" id 11,
            dato faktum "tom" id 12
        )
        val versjon = Versjon.Bygger(
            prototypeFakta,
            prototypeFakta heltall 10 er 1 hvisOppfylt { prototypeFakta dato 11 etter (prototypeFakta dato 12) },
            Utredningsprosess(
                Seksjon(
                    "seksjon",
                    Rolle.nav,
                    *(prototypeFakta.map { it }.toTypedArray())
                )
            ),
        ).registrer()
    }

    val faktaPersistence = mockk<FaktaPersistence>().also {
        every { it.hent(any()) } returns Versjon.id(prosessVersjon).utredningsprosess(prototypeFakta)
        every { it.lagre(any() as Fakta) } returns true
    }
    val resultatPersistence = mockk<ResultatPersistence>(relaxed = true)
    val testRapid = TestRapid().also {
        FaktumSvarService(
            faktaPersistence = faktaPersistence,
            resultatPersistence = resultatPersistence,
            rapidsConnection = it
        )
    }

    @Test
    fun `skal ta imot liste svar generator faktum`() {
        testRapid.sendTestMessage(faktumSvarMedGeneratorFaktum)

        assertTrue(prototypeFakta.generator(10).erBesvart())
        assertTrue(prototypeFakta.dato("11.1").erBesvart())
        assertEquals("2020-01-01", prototypeFakta.dato("11.1").svar().toString())
        assertEquals("A123456", prototypeFakta.dato("11.1").besvartAv())
        assertTrue(prototypeFakta.dato("12.1").erBesvart())
        assertEquals("2020-01-08", prototypeFakta.dato("12.1").svar().toString())
        assertTrue(prototypeFakta.dato("11.2").erBesvart())
        assertEquals("2020-01-09", prototypeFakta.dato("11.2").svar().toString())
        assertTrue(prototypeFakta.dato("12.2").erBesvart())
        assertEquals("2020-01-16", prototypeFakta.dato("12.2").svar().toString())

        verify(exactly = 1) { faktaPersistence.hent(any()) }
        verify(exactly = 1) { faktaPersistence.lagre(any() as Fakta) }
        verify(exactly = 1) { resultatPersistence.lagreResultat(any(), any(), any()) }
    }

    @Test
    fun `skal ta imot liste med svar i generator faktum selvom ikke alle templates har svar`() {
        val json = jacksonObjectMapper().readTree(faktumSvarMedGeneratorFaktum)
        (json["fakta"][0]["svar"][1][1] as ObjectNode).remove("svar")
        testRapid.sendTestMessage(json.toString())
        assertFalse(prototypeFakta.dato("12.2").erBesvart())
    }

    //language=json
    private val faktumSvarMedGeneratorFaktum = """{
  "@event_name": "faktum_svar",
  "@opprettet": "2020-11-18T11:04:32.867824",
  "@id": "930e2beb-d394-4024-b713-dbeb6ad3d4bf",
  "fnr": "123",
  "søknad_uuid": "41621ac0-f5ee-4cce-b1f5-88a79f25f1a5",
  "fakta": [
    {
      "id": "10",
      "behov": "Registreringsperioder",
      "type": "generator",
      "templates": [
        {
          "id": "11",
          "navn": "fom",
          "type": "localdate"
        },
        {
          "id": "12",
          "navn": "tom",
          "type": "localdate"
        }
      ],
      "svar": [
        [
          {
            "id": "11",
            "navn": "fom",
            "type": "localdate",
            "svar": "2020-01-01",
            "besvartAv": "A123456"
          },
          {
            "id": "12",
            "navn": "tom",
            "type": "localdate",
            "svar": "2020-01-08"
          }
        ],
        [
          {
            "id": "11",
            "navn": "fom",
            "type": "localdate",
            "svar": "2020-01-09"
          },
          {
            "id": "12",
            "navn": "tom",
            "type": "localdate",
            "svar": "2020-01-16"
          }
        ]
      ]
    }
  ],
  "@behov": [
    "Registreringsperioder"
  ],
  "Søknadstidspunkt": "2020-11-09",
  "@løsning": {
    "Registreringsperioder": [
      {
        "fom": "2020-01-01",
        "tom": "2020-01-08"
      },
      {
        "fom": "2020-01-09",
        "tom": "2020-01-16"
      }
    ]
  },
  "system_read_count": 0,
  "@final": true
}
    """.trimIndent()
}
