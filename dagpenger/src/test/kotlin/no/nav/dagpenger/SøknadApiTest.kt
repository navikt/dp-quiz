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
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.søknad.Versjon
import no.nav.dagpenger.model.søknad.Versjon.Type.Web
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class SøknadApiTest {
    private val mapper = ObjectMapper()

    private val fakta: Fakta = Fakta(
        dato faktum "Ønsker dagpenger fra dato" id 2,
        dato faktum "Fødselsdato" id 1,
        dato faktum "Dimisjonsdato" id 10

    )

    private val ønsketDato = fakta dato 2
    private val fødselsdato = fakta dato 1
    private val dimisjonsdato = fakta dato 10

    private val prototypeSubsumsjoner = "".alle(ønsketDato før fødselsdato, dimisjonsdato før fødselsdato)
    private val søknadPrototype = Søknad(
        Seksjon("seksjon1", Rolle.søker, ønsketDato, fødselsdato),
        Seksjon("seksjon2", Rolle.søker, dimisjonsdato),
    )
    private val søknad = Versjon(1, fakta, prototypeSubsumsjoner, mapOf(Web to søknadPrototype))
        .søknad("", Web)

    private val søknader = InMemorySøknader {
        søknad
    }

    @Test
    fun `hent neste-seksjon og besvar faktumene`() = withTestApplication({
        søknadApi(søknader, prototypeSubsumsjoner)
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
        søknadApi(søknader, prototypeSubsumsjoner)
    }) {
        with(handleRequest(HttpMethod.Get, "/soknad/${UUID.randomUUID()}/subsumsjoner")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(2, mapper.readTree(response.content)["root"]["subsumsjoner"].size())
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
