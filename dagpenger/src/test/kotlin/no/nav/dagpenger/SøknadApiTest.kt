package no.nav.dagpenger

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test

internal class SøknadApiTest {
    private val mapper = ObjectMapper()

    @Test
    fun testRequest() = withTestApplication({
        søknadApi()
    }) {
        with(handleRequest(HttpMethod.Get, "/neste-seksjon")) {
            assertEquals(HttpStatusCode.OK, response.status())
            mapper.readTree(response.content).let {
                assertEquals(2, it.size())
                assertEquals(2, it[0]["id"].asInt())
            }
        }
        with(
            handleRequest(HttpMethod.Post, "/faktum/") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(jsonBesvar)
            }
        ) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun testSubsumsjontre() = withTestApplication({
        søknadApi()
    }) {
        with(handleRequest(HttpMethod.Get, "/subsumsjoner")) {
            assertEquals(HttpStatusCode.OK, response.status())
            mapper.readTree(response.content).let {
                assertEquals(7, it["root"]["subsumsjoner"].size())
            }
        }
    }
}

@Language("json")
val jsonBesvar =
    """  {
    "navn": "Fødselsdato",
    "svar": "2000-12-13"
  }
    """.trimIndent()
