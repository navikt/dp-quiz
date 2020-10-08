package no.nav.dagpenger

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import no.nav.dagpenger.FaktumBesvarelse.Kontekst
import no.nav.dagpenger.FaktumBesvarelse.Svar
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class SøknadApiTest {
    private val mapper = ObjectMapper()

    private val ønsketDato = FaktumNavn<LocalDate>(2, "Ønsker dagpenger fra dato").faktum()
    private val fødselsdato = FaktumNavn<LocalDate>(1, "Fødselsdato").faktum(LocalDate::class.java)
    private val dimisjonsdato = FaktumNavn<LocalDate>(10, "Dimisjonsdato").faktum(LocalDate::class.java)

    private val subsumsjoner = "".alle(ønsketDato før fødselsdato, dimisjonsdato før fødselsdato)
    private val søknader = InMemorySøknader {
        Søknad(
            Seksjon("seksjon1", Rolle.søker, ønsketDato, fødselsdato),
            Seksjon("seksjon2", Rolle.søker, dimisjonsdato),
        )
    }

    @Test
    fun `hent neste-seksjon og besvar faktumene`() = withTestApplication({
        søknadApi(søknader, subsumsjoner)
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
                    setBody(
                        mapper.writeValueAsString(
                            FaktumBesvarelse(
                                svar = Svar(LocalDate.now().toString(), it["clazz"].asText()),
                                kontekst = Kontekst("seksjon1")
                            )
                        )
                    )
                }
            ) {
                assertEquals(HttpStatusCode.OK, response.status())
                mapper.readTree(response.content).let { response ->
                    assertEquals(2, response["fakta"].size())
                    assertEquals(2, response["fakta"][0]["id"].asInt())
                    assertEquals("seksjon1", response["root"]["navn"].asText())
                }
            }
        }
    }

    @Test
    fun testSubsumsjontre() = withTestApplication({
        søknadApi(søknader, subsumsjoner)
    }) {
        with(handleRequest(HttpMethod.Get, "/soknad/${UUID.randomUUID()}/subsumsjoner")) {
            assertEquals(HttpStatusCode.OK, response.status())
            mapper.readTree(response.content).let {
                assertEquals(2, it["root"]["subsumsjoner"].size())
            }
        }
    }

    @Test
    fun `Tomt for seksjoner`() {
        val søknader = InMemorySøknader { Søknad() }
        withTestApplication({
            søknadApi(søknader, "tom subsumsjon".alle())
        }) {
            val søknadsId = UUID.randomUUID()
            with(handleRequest(HttpMethod.Get, "/soknad/$søknadsId/neste-seksjon")) {
                assertEquals(HttpStatusCode.ResetContent, response.status())
            }
        }
    }
}
