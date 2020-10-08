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
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SeksjonJsonBuilder
import no.nav.dagpenger.model.visitor.SubsumsjonJsonBuilder
import no.nav.dagpenger.model.visitor.SøknadVisitor
import java.time.LocalDate
import java.util.UUID

internal data class Svar(
    val verdi: String,
    val type: String
)

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
                val (verdi, type) = call.receive<Svar>()
                val søknad = søknader.søknad(UUID.fromString(call.parameters["søknadsId"]!!))
                val id = call.parameters["faktumId"]!!

                val faktum = when (type.toLowerCase()) {
                    "localdate" -> søknad.finnFaktum<LocalDate>(id).besvar(LocalDate.parse(verdi))
                    "string" -> søknad.finnFaktum<String>(id).besvar(verdi)
                    "boolean" -> søknad.finnFaktum<Boolean>(id).besvar(verdi.toBoolean())
                    "int" -> søknad.finnFaktum<Int>(id).besvar(verdi.toInt())
                    "inntekt" -> søknad.finnFaktum<Inntekt>(id).besvar(verdi.toInt().årlig)
                    else -> throw IllegalArgumentException("BOOM")
                }
                call.respond(SeksjonJsonBuilder(faktum.finnSeksjon(søknad)).resultat())
            }
            get("/subsumsjoner") {
                val søknad = søknader.søknad(UUID.fromString(call.parameters["søknadsId"]!!))
                val subsumsjoner = template.deepCopy(søknad)
                call.respond(SubsumsjonJsonBuilder.mulige(subsumsjoner).resultat())
            }
        }
    }
}

internal fun Faktum<*>.finnSeksjon(søknad: Søknad): Seksjon =
        // TODO: This method will find the LAST Seksjon with the Factum. Is that what you want?
    object : SøknadVisitor {
        lateinit var seksjon: Seksjon

        override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>) {
            if (this::seksjon.isInitialized) return
            fakta.flatMap { it.grunnleggendeFakta() }.find { it.id == this@finnSeksjon.id }?.let {
                this.seksjon = seksjon
            }
        }
    }.let {
        søknad.accept(it)
        it.seksjon
    }
