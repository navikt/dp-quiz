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
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SøknadApiTest {
    private val mapper = ObjectMapper()

    @Test
    fun `hent neste-seksjon og besvar faktumene`() = withTestApplication({
        søknadApi()
    }) {
        val fakta = with(handleRequest(HttpMethod.Get, "/søknad/1/neste-seksjon")) {
            assertEquals(HttpStatusCode.OK, response.status())
            mapper.readTree(response.content).let { response ->
                assertEquals(2, response.size())
                assertEquals(2, response[0]["id"].asInt())

                response.map { it["id"].asInt() }
            }
        }

        fakta.forEach {
            with(
                handleRequest(HttpMethod.Post, "/søknad/2/faktum/") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(mapper.writeValueAsString(Svar(it, LocalDate.now().toString(), "localdate")))
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
        with(handleRequest(HttpMethod.Get, "/søknad/123/subsumsjoner")) {
            assertEquals(HttpStatusCode.OK, response.status())
            mapper.readTree(response.content).let {
                assertEquals(7, it["root"]["subsumsjoner"].size())
            }
        }
    }
}
