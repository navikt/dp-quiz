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
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.marshalling.SeksjonJsonBuilder
import no.nav.dagpenger.model.marshalling.SubsumsjonJsonBuilder
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.time.LocalDate
import java.util.UUID

internal data class FaktumBesvarelse(
    val svar: Svar,
    val kontekst: Kontekst
) {
    internal data class Svar(
        val verdi: String,
        val type: String
    )

    internal data class Kontekst(
        val seksjon: String
    )
}

internal fun Application.søknadApi(søknader: Søknader, template: Subsumsjon) {
    install(ContentNegotiation) {
        jackson {}
    }

    routing {
        route("/soknad/{søknadsId}") {
            get("/neste-seksjon") {
                val søknad = søknader.søknad(UUID.fromString(call.parameters["søknadsId"]!!))
                val subsumsjoner = template.deepCopy(søknad)
                try {
                    val seksjon = søknad.nesteSeksjon(subsumsjoner)
                    call.respond(SeksjonJsonBuilder(seksjon).resultat())
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.ResetContent)
                }
            }
            put("/faktum/{faktumId}") {
                val (svar, kontekst) = call.receive<FaktumBesvarelse>()
                val søknad = søknader.søknad(UUID.fromString(call.parameters["søknadsId"]!!))
                val id = call.parameters["faktumId"]!!

                with(svar) {
                    when (type.toLowerCase()) {
                        "localdate" -> søknad.faktum<LocalDate>(id).besvar(LocalDate.parse(verdi))
                        "string" -> søknad.faktum<String>(id).besvar(verdi)
                        "boolean" -> søknad.faktum<Boolean>(id).besvar(verdi.toBoolean())
                        "int" -> søknad.faktum<Int>(id).besvar(verdi.toInt())
                        "inntekt" -> søknad.faktum<Inntekt>(id).besvar(verdi.toInt().årlig)
                        else -> throw IllegalArgumentException("BOOM")
                    }
                }

                call.respond(SeksjonJsonBuilder(søknad.seksjon(kontekst.seksjon)).resultat())
            }
            get("/subsumsjoner") {
                val søknad = søknader.søknad(UUID.fromString(call.parameters["søknadsId"]!!))
                val subsumsjoner = template.deepCopy(søknad)
                call.respond(SubsumsjonJsonBuilder.mulige(subsumsjoner).resultat())
            }
        }
    }
}
