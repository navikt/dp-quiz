package no.nav.dagpenger.behov

import helpers.SeksjonEksempel.prototypeSøknad1
import helpers.SeksjonEksempel.seksjon1
import helpers.SeksjonEksempel.seksjon2
import helpers.SeksjonEksempel.seksjon3
import helpers.SeksjonEksempel.seksjon4
import helpers.januar
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class NavMediatorTest {

    private val rapid = TestRapid()

    private val fnr = "12345678910"
    private val søknadUuid = UUID.randomUUID()

    @BeforeEach
    fun reset() {
        rapid.reset()
    }

    @Test
    fun `Sende ut ett behov for uavhengig faktum`() {
        val navMediator = NavMediator(rapid)

        navMediator.sendBehov(seksjon1, fnr, søknadUuid)

        val message = rapid.inspektør.message(0)
        assertEquals(1, rapid.inspektør.size)
        assertEquals("12", message["@behov"].asText())
        assertEquals(fnr, message["fnr"].asText())
        assertEquals(søknadUuid, UUID.fromString(message["søknadUuid"].asText()))
    }

    @Test
    fun `Sende ut to behov for faktum verneplikt og egen næring`() {
        val navMediator = NavMediator(rapid)

        navMediator.sendBehov(seksjon2, fnr, søknadUuid)

        assertEquals(2, rapid.inspektør.size)
        val message = rapid.inspektør.message(0)
        assertEquals("12", message["@behov"].asText())
        assertEquals(fnr, message["fnr"].asText())
        assertEquals(søknadUuid, UUID.fromString(message["søknadUuid"].asText()))

        val message2 = rapid.inspektør.message(1)
        assertEquals("6", message2["@behov"].asText())
        assertEquals(fnr, message2["fnr"].asText())
        assertEquals(søknadUuid, UUID.fromString(message2["søknadUuid"].asText()))
    }

    @Test
    fun `Ignorere faktum med ubesvarte avhengigheter`() {
        val navMediator = NavMediator(rapid)

        navMediator.sendBehov(seksjon3, fnr, søknadUuid)

        assertEquals(1, rapid.inspektør.size)
        val message = rapid.inspektør.message(0)
        assertEquals("6", message["@behov"].asText())
        assertEquals(fnr, message["fnr"].asText())
        assertEquals(søknadUuid, UUID.fromString(message["søknadUuid"].asText()))
    }

    @Test
    fun `Sende ut behov med avhengig data`() {
        val navMediator = NavMediator(rapid)

        navMediator.sendBehov(seksjon4, fnr, søknadUuid)
        assertEquals(6, rapid.inspektør.size)

        prototypeSøknad1.dato(1).besvar(2.januar)
        prototypeSøknad1.dato(2).besvar(1.januar)
        prototypeSøknad1.dato(3).besvar(1.januar)
        prototypeSøknad1.dato(4).besvar(1.januar)
        prototypeSøknad1.dato(11).besvar(1.januar)
        prototypeSøknad1.ja(6).besvar(true)

        rapid.reset()
        navMediator.sendBehov(seksjon4, fnr, søknadUuid)
        assertEquals(1, rapid.inspektør.size)

        val message = rapid.inspektør.message(0)
        assertEquals(2.januar, message["Virkningstidspunkt"].asLocalDate())
        assertEquals(true, message["EgenNæring"].asBoolean())
    }

    @Test
    fun `Godkjenning av dokumentasjon for fangst og fisk`() {
        val navMediator = NavMediator(rapid)

        val seksjon = Seksjon(
            "seksjon",
            Rolle.nav,
            prototypeSøknad1.dokument(14)
        )

        prototypeSøknad1.ja(6).besvar(true)
        navMediator.sendBehov(seksjon, fnr, søknadUuid)

        assertEquals(1, rapid.inspektør.size)
        assertEquals("14", rapid.inspektør.message(0)["@behov"].asText())
    }

    @Language("json")
    private val eksempelOutput =
        """{
          "@behov": "Verneplikt",
          "@id": "12345",
          "fnr": "$fnr",
          "søknadUuid": "$søknadUuid"
        }"""
}
