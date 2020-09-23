package no.nav.dagpenger

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import no.nav.dagpenger.model.visitor.JsonBuilder
import no.nav.dagpenger.regelverk.inngangsvilkår
import no.nav.dagpenger.regelverk.ønsketDato
import no.nav.dagpenger.søknad.datamaskin
import java.time.LocalDate

data class Svar(
    val navn: String,
    val svar: String
)

fun Application.søknadApi() {
    install(ContentNegotiation) {
        jackson {}
    }

    routing {
        get("/neste-seksjon") {
            val seksjon = datamaskin.nesteSeksjon(inngangsvilkår)
            call.respond(seksjon)
        }
        get("/subsumsjoner") {
            call.respond(JsonBuilder(inngangsvilkår).resultat())
        }
        post("/faktum") {
            val svar = call.receive<Svar>()
            ønsketDato.besvar(LocalDate.parse(svar.svar))
            call.respond(HttpStatusCode.OK)
        }
    }
}
