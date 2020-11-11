package no.nav.dagpenger.behov

import helpers.januar
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

internal class NavMediatorTest {

    private val rapid = TestRapid()
    private val navMediator = NavMediator(rapid)

    private val fnr = "12345678910"
    private val søknadUuid = UUID.randomUUID()

    fun prototypeSøknad() = Søknad(
        dato faktum "Ønsker dagpenger fra dato" id 1,
        dato faktum "Siste dag med arbeidsplikt" id 2,
        dato faktum "Registreringsdato" id 3,
        dato faktum "Siste dag med lønn" id 4,
        maks dato "Virkningstidspunkt" av 1 og 2 og 3 og 4 og 11 id 5,
        ja nei "EgenNæring" id 6,
        inntekt faktum "Inntekt siste 3 år" id 7 avhengerAv 5 og 6,
        inntekt faktum "Inntekt siste 12 mnd" id 8 avhengerAv 5 og 6,
        inntekt faktum "3G" id 9,
        inntekt faktum "1,5G" id 10,
        dato faktum "Søknadstidspunkt" id 11,
        ja nei "Verneplikt" id 12,
        dokument faktum "dokumentasjon for fangst og fisk" id 14 avhengerAv 6,
        ja nei "Boolean" id 100
    ).also {
        it.ja(100).besvar(true)
    }

    @Test
    fun `Sende ut ett behov for faktum verneplikt`() {
        val prototypeSøknad = prototypeSøknad()
        val seksjon = Seksjon(
            "seksjon1",
            Rolle.nav,
            prototypeSøknad.ja(12),
            prototypeSøknad.ja(100)
        )

        navMediator.sendBehov(seksjon, fnr, søknadUuid)

        val message = rapid.inspektør.message(0)
        assertEquals(1, rapid.inspektør.size)
        assertEquals("Verneplikt", message["@behov"].asText())
        assertEquals(fnr, message["fnr"].asText())
        assertEquals(søknadUuid, UUID.fromString(message["søknadUuid"].asText()))
    }

    @Test
    fun `Sende ut to behov for faktum verneplikt og egen næring`() {
        val prototypeSøknad = prototypeSøknad()
        val seksjon = Seksjon(
            "seksjon2",
            Rolle.nav,
            prototypeSøknad.ja(12),
            prototypeSøknad.ja(100),
            prototypeSøknad.ja(6)
        )

        navMediator.sendBehov(seksjon, fnr, søknadUuid)

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
    fun `Ignorere faktum med ubesvarte avhengigheter`() {
        val prototypeSøknad = prototypeSøknad()

        val seksjon = Seksjon(
            "seksjon3",
            Rolle.nav,
            prototypeSøknad.ja(6),
            prototypeSøknad.ja(7)
        )

        navMediator.sendBehov(seksjon, fnr, søknadUuid)

        assertEquals(1, rapid.inspektør.size)
        val message = rapid.inspektør.message(0)
        assertEquals("EgenNæring", message["@behov"].asText())
        assertEquals(fnr, message["fnr"].asText())
        assertEquals(søknadUuid, UUID.fromString(message["søknadUuid"].asText()))
    }

    @Test
    fun `Sende ut behov med avhengig data`() {
        val prototypeSøknad = prototypeSøknad()
        val seksjon = Seksjon(
            "seksjon4",
            Rolle.nav,
            prototypeSøknad.dato(1),
            prototypeSøknad.dato(2),
            prototypeSøknad.dato(3),
            prototypeSøknad.dato(4),
            prototypeSøknad.dato(5),
            prototypeSøknad.ja(6),
            prototypeSøknad.inntekt(7),
            prototypeSøknad.dato(11)
        )

        navMediator.sendBehov(seksjon, fnr, søknadUuid)
        assertEquals(6, rapid.inspektør.size)

        prototypeSøknad.dato(1).besvar(2.januar)
        prototypeSøknad.dato(2).besvar(1.januar)
        prototypeSøknad.dato(3).besvar(1.januar)
        prototypeSøknad.dato(4).besvar(1.januar)
        prototypeSøknad.dato(11).besvar(1.januar)
        prototypeSøknad.ja(6).besvar(true)

        rapid.reset()
        navMediator.sendBehov(seksjon, fnr, søknadUuid)
        assertEquals(1, rapid.inspektør.size)

        val message = rapid.inspektør.message(0)
        assertEquals(2.januar, message["Virkningstidspunkt"].asLocalDate())
        assertEquals(true, message["EgenNæring"].asBoolean())
    }

    @Test
    fun `Godkjenning av dokumentasjon for fangst og fisk`() {
        val prototypeSøknad = prototypeSøknad()
        val seksjon = Seksjon(
            "seksjon",
            Rolle.nav,
            prototypeSøknad.dokument(14)
        )

        prototypeSøknad.ja(6).besvar(true)
        navMediator.sendBehov(seksjon, fnr, søknadUuid)

        assertEquals(1, rapid.inspektør.size)
        assertEquals("GodkjenningDokumentasjonFangstOgFisk", rapid.inspektør.message(0)["@behov"].asText())
    }

}
