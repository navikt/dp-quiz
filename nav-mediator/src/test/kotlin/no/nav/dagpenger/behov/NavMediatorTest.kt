package no.nav.dagpenger.behov

import helpers.SeksjonEksempel.seksjon1
import helpers.SeksjonEksempel.seksjon2
import helpers.SeksjonEksempel.seksjon3
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

internal class NavMediatorTest {

    private val rapid = TestRapid()
    private val navMediator = NavMediator(rapid)

    private val fnr = "12345678910"
    private val søknadUuid = UUID.randomUUID()

    @Test
    fun `Sende ut ett behov for faktum verneplikt`() {

        navMediator.sendBehov(seksjon1, fnr, søknadUuid)

        val message = rapid.inspektør.message(0)
        assertEquals(1, rapid.inspektør.size)
        assertEquals("Verneplikt", message["@behov"].asText())
        assertEquals(fnr, message["fnr"].asText())
        assertEquals(søknadUuid, UUID.fromString(message["søknadUuid"].asText()))
    }

    @Test
    fun `Sende ut to behov for faktum verneplikt og egen næring`() {

        navMediator.sendBehov(seksjon2, fnr, søknadUuid)

        assertEquals(2, rapid.inspektør.size)
        val message = rapid.inspektør.message(0)
        assertEquals("Verneplikt", message["@behov"].asText())
        assertEquals(fnr, message["fnr"].asText())
        assertEquals(søknadUuid, UUID.fromString(message["søknadUuid"].asText()))

        val message2 = rapid.inspektør.message(1)
        assertEquals("EgenNæring", message2["@behov"].asText())
        assertEquals(fnr, message2["fnr"].asText())
        assertEquals(søknadUuid, UUID.fromString(message2["søknadUuid"].asText()))
    }

    @Test
    fun `Sende behov for faktum med avhengigheter`() {
        navMediator.sendBehov(seksjon3, fnr, søknadUuid)

        assertEquals(1, rapid.inspektør.size)
        val message = rapid.inspektør.message(0)
        assertEquals("EgenNæring", message["@behov"].asText())
        assertEquals(fnr, message["fnr"].asText())
        assertEquals(søknadUuid, UUID.fromString(message["søknadUuid"].asText()))
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
