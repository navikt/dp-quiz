package no.nav.dagpenger.quiz.mediator.meldinger

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class FaktumSvarServiceTest {

    private val versjonId = 1458
    val prototypeFakta = Søknad(
        versjonId,
        heltall faktum "generator" id 10 genererer 11 og 12,
        dato faktum "fom" id 11,
        dato faktum "tom" id 12,

    )

    val versjon = Versjon.Bygger(
        prototypeFakta,
        prototypeFakta heltall 10 er 1 hvisOppfylt { prototypeFakta dato 11 etter (prototypeFakta dato 12) },
        mapOf(
            Versjon.UserInterfaceType.Web to Søknadprosess(
                Seksjon(
                    "seksjon",
                    Rolle.nav,
                    *(prototypeFakta.map { it }.toTypedArray())
                )
            )
        )
    ).registrer()

    val søknadPersistence = mockk<SøknadPersistence>().also {
        every { it.hent(any(), any()) } returns Versjon.id(versjonId)
            .søknadprosess(prototypeFakta, Versjon.UserInterfaceType.Web)
        every { it.lagre(any() as Søknad) } returns true
    }

    val resultatPersistence = mockk<ResultatPersistence>(relaxed = true)

    val testRapid = TestRapid().also {
        FaktumSvarService(
            søknadPersistence = søknadPersistence,
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

        verify(exactly = 1) { søknadPersistence.hent(any(), any()) }
        verify(exactly = 1) { søknadPersistence.lagre(any() as Søknad) }
        verify(exactly = 1) { resultatPersistence.lagreResultat(any(), any(), any()) }
    }

    //language=json
    private val faktumSvarMedGeneratorFaktum =
        """{
  "@event_name": "faktum_svar",
  "@opprettet": "2020-11-18T11:04:32.867824",
  "@id": "930e2beb-d394-4024-b713-dbeb6ad3d4bf",
  "fnr": "123",
  "søknad_uuid": "41621ac0-f5ee-4cce-b1f5-88a79f25f1a5",
  "fakta": [
    {
      "id": "10",
      "behov": "Registreringsperioder",
      "clazz": "generator",
      "templates": [
        {
          "id": "11",
          "navn": "fom",
          "clazz": "localdate"
        },
        {
          "id": "12",
          "navn": "tom",
          "clazz": "localdate"
        }
      ],
      "svar": [
        [
          {
            "id": "11",
            "navn": "fom",
            "clazz": "localdate",
            "svar": "2020-01-01",
            "besvartAv": "A123456"
          },
          {
            "id": "12",
            "navn": "tom",
            "clazz": "localdate",
            "svar": "2020-01-08"
          }
        ],
        [
          {
            "id": "11",
            "navn": "fom",
            "clazz": "localdate",
            "svar": "2020-01-09"
          },
          {
            "id": "12",
            "navn": "tom",
            "clazz": "localdate",
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
