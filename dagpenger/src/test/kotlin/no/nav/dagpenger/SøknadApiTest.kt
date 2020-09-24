package no.nav.dagpenger

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import no.nav.dagpenger.model.visitor.SøknadJsonBuilder
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class SøknadApiTest {
    private val mapper = ObjectMapper()

    @Test
    fun `hent neste-seksjon og besvar faktumene`() = withTestApplication({
        søknadApi()
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
                handleRequest(HttpMethod.Put, "/soknad/$søknadsId/faktum/${it["id"]}") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(mapper.writeValueAsString(Svar(LocalDate.now().toString(), it["clazz"].asText())))
                }
            ) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }

        søknader.forEach {
            println(SøknadJsonBuilder(it.value))
        }
    }

    @Test
    fun testSubsumsjontre() = withTestApplication({
        søknadApi()
    }) {
        with(handleRequest(HttpMethod.Get, "/soknad/${UUID.randomUUID()}/subsumsjoner")) {
            assertEquals(HttpStatusCode.OK, response.status())
            mapper.readTree(response.content).let {
                assertEquals(7, it["root"]["subsumsjoner"].size())
            }
        }
    }
}
