package no.nav.dagpenger

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.Assert
import org.junit.jupiter.api.Test

class NaisApiTest {
    @Test
    fun `svarer på helsesjekker`() = withTestApplication({
        naisApi()
    }) {
        with(handleRequest(HttpMethod.Get, "/isAlive")) {
            Assert.assertEquals(HttpStatusCode.OK, response.status())
        }
        with(handleRequest(HttpMethod.Get, "/isReady")) {
            Assert.assertEquals(HttpStatusCode.OK, response.status())
        }
    }
}
