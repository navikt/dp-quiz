package no.nav.dagpenger

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import no.nav.dagpenger.regelverk.inngangsvilkår
import no.nav.dagpenger.søknad.datamaskin

fun Application.søknadApi() {
    install(ContentNegotiation) {
        jackson {}
    }

    routing {
        get("/neste-fakta") {
            val seksjon = datamaskin.nesteSeksjon(inngangsvilkår)
            call.respond(seksjon)
        }
    }
}
