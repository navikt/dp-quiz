package no.nav.dagpenger

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.regelverk.dimisjonsdato
import no.nav.dagpenger.regelverk.fødselsdato
import no.nav.dagpenger.regelverk.ønsketDato
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class SøknadApiTest {
    private val mapper = ObjectMapper()

    private val søknader = InMemorySøknader {
        Søknad(
            Seksjon(Rolle.søker, ønsketDato, fødselsdato),
            Seksjon(Rolle.søker, dimisjonsdato),
        )
    }

    @Test
    fun `hent neste-seksjon og besvar faktumene`() = withTestApplication({
        søknadApi(søknader)
    }) {
        val søknadsId = UUID.randomUUID()
        val fakta = with(handleRequest(HttpMethod.Get, "/soknad/$søknadsId/neste-seksjon")) {
            assertEquals(HttpStatusCode.OK, response.status())
            mapper.readTree(response.content).let { response ->
                assertEquals(2, response["fakta"].size())
                assertEquals(2, response["fakta"][0]["id"].asInt())

                response["fakta"]
            }
        }

        fakta.forEach {
            with(
                handleRequest(HttpMethod.Put, "/soknad/$søknadsId/faktum/${it["id"].asText()}") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(mapper.writeValueAsString(Svar(LocalDate.now().toString(), it["clazz"].asText())))
                }
            ) {
                assertEquals(HttpStatusCode.OK, response.status())
                mapper.readTree(response.content).let { response ->
                    assertEquals(2, response["fakta"].size())
                    assertEquals(2, response["fakta"][0]["id"].asInt())
                }
            }
        }
    }

    @Test
    fun testSubsumsjontre() = withTestApplication({
        søknadApi(søknader)
    }) {
        with(handleRequest(HttpMethod.Get, "/soknad/${UUID.randomUUID()}/subsumsjoner")) {
            assertEquals(HttpStatusCode.OK, response.status())
            mapper.readTree(response.content).let {
                assertEquals(2, it["root"]["subsumsjoner"].size())
            }
        }
    }

    @Test
    fun `Kan finne seksjon og fakta via faktumid`() {
        val faktum = FaktumNavn(123, "testfaktum").faktum(Int::class.java)

        val seksjon = Seksjon(Rolle.søker, faktum)
        val søknad = Søknad(
            Seksjon(Rolle.søker, ønsketDato, fødselsdato),
            seksjon,
            Seksjon(Rolle.søker, dimisjonsdato),
        )
        søknad.finnFaktum<Int>("123").also {
            assertEquals(faktum, it)
            assertEquals(seksjon, it.finnSeksjon(søknad))
        }
    }

    @Test
    fun `Tomt for seksjoner`() {
        val søknader = InMemorySøknader { Søknad() }
        withTestApplication({
            søknadApi(søknader)
        }) {
            val søknadsId = UUID.randomUUID()
            with(handleRequest(HttpMethod.Get, "/soknad/$søknadsId/neste-seksjon")) {
                assertEquals(HttpStatusCode.ResetContent, response.status())
            }
        }
    }
}
