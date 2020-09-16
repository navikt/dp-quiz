package no.nav.dagpenger

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
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
        /* with(handleRequest(HttpMethod.Post, "/faktum")) {
             assertEquals(HttpStatusCode.OK, response.status())
             assertEquals("Random: 7", response.content)
         }*/
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