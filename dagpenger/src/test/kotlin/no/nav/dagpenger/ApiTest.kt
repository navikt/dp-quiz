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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class ApiTest {
    val mapper = ObjectMapper()

    @Test
    @Disabled
    fun testRequest() = withTestApplication({
        søknadApi()
    }) {
        with(handleRequest(HttpMethod.Get, "/neste-fakta")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(mapper.readTree(jsonResponse), mapper.readTree(response.content))
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
}

@Language("json")
val jsonResponse =
    """[
  {
    "navn": {
      "navn": "Ønsker dagpenger fra dato"
    }
  },
  {
      "navn": {
    "navn": "Fødselsdato"
}
  }
]
    """.trimIndent()

@Language("json")
val jsonBesvar =
    """  {
    "navn": "Fødselsdato",
    "svar": "2000-12-13"
  }
    """.trimIndent()
