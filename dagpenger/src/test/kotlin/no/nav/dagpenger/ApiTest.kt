package no.nav.dagpenger

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.server.testing.*
import junit.framework.Assert.assertEquals
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

internal class ApiTest {
    val mapper = ObjectMapper()

    @Test
    fun testRequest() = withTestApplication({
        søknadApi()
    }) {
        with(handleRequest(HttpMethod.Get, "/neste-fakta")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(mapper.readTree(jsonResponse), mapper.readTree(response.content))
        }
         with(handleRequest(HttpMethod.Post, "/faktum/"){
             addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
             setBody(jsonBesvar)
         }) {
             assertEquals(HttpStatusCode.OK, response.status())
         }
    }
}

@Language("json")
val jsonResponse = """[
  {
    "navn": "Ønsker dagpenger fra dato"
  },
  {
    "navn": "Fødselsdato"
  }
]""".trimIndent()

@Language("json")
val jsonBesvar = """  {
    "navn": "Fødselsdato",
    "svar": "2000-12-13"
  }""".trimIndent()