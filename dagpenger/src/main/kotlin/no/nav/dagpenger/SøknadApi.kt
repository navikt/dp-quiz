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
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.JsonBuilder
import no.nav.dagpenger.model.visitor.SeksjonJsonBuilder
import no.nav.dagpenger.model.visitor.SøknadVisitor
import no.nav.dagpenger.regelverk.fødselsdato
import no.nav.dagpenger.regelverk.inngangsvilkår
import no.nav.dagpenger.regelverk.ønsketDato
import java.time.LocalDate
import java.util.UUID

data class Svar(
    val verdi: String,
    val type: String
)

fun Application.søknadApi() {
    install(ContentNegotiation) {
        jackson {}
    }

    routing {
        route("/søknad/{søknadsId}") {

            get("/neste-seksjon") {
                val søknad = getOrCreateSøknad(UUID.fromString(call.parameters["søknadsId"]!!))
                val seksjon = søknad.nesteSeksjon(inngangsvilkår)

                call.respond(SeksjonJsonBuilder(seksjon).resultat())
            }
            put("/faktum/{faktumId}") {
                val (verdi, type) = call.receive<Svar>()
                val søknad = getOrCreateSøknad(UUID.fromString(call.parameters["søknadsId"]!!))
                val id = call.parameters["faktumId"]!!.toInt()

                when (type.toLowerCase()) {
                    "localdate" -> søknad.finnFaktum<LocalDate>(id).besvar(LocalDate.parse(verdi)) // bør løses et annet sted?
                    "string" -> søknad.finnFaktum<String>(id).besvar(verdi)
                    "boolean" -> søknad.finnFaktum<Boolean>(id).besvar(verdi.toBoolean())
                    "int" -> søknad.finnFaktum<Int>(id).besvar(verdi.toInt())
                    else -> throw IllegalArgumentException("BOOM")
                }
                call.respond(HttpStatusCode.OK)
            }
            get("/subsumsjoner") {
                call.respond(JsonBuilder(inngangsvilkår).resultat())
            }
        }
    }
}

private fun <R : Comparable<R>> Søknad.finnFaktum(id: Int): Faktum<R> =
    object : SøknadVisitor {
        lateinit var faktum: Faktum<R>

        override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>) {
            if (this::faktum.isInitialized) return
            fakta.find { it.id == id }?.let {
                faktum = it as Faktum<R>
            }
        }
    }.let {
        this@finnFaktum.accept(it)
        it.faktum
    }

internal val søknader = mutableMapOf<UUID, Søknad>()
private fun getOrCreateSøknad(id: UUID) =
    søknader.getOrPut(id) {
        // val fødselsdato = FaktumNavn(1, "Fødselsdato").faktum<LocalDate>()
        // val ønsketDato = FaktumNavn(2, "Ønsker dagpenger fra dato").faktum<LocalDate>()

        Søknad(
            Seksjon(Rolle.søker, ønsketDato, fødselsdato),
        )
    }
