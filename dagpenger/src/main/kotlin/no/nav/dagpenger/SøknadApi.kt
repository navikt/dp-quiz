package no.nav.dagpenger

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.JsonBuilder
import no.nav.dagpenger.model.visitor.SøknadVisitor
import no.nav.dagpenger.regelverk.fødselsdato
import no.nav.dagpenger.regelverk.inngangsvilkår
import no.nav.dagpenger.regelverk.ønsketDato
import java.time.LocalDate

data class Svar(
    val id: Int,
    val navn: String,
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
                val søknad = getOrCreateSøknad(call.parameters["søknadsId"]!!.toInt())
                val seksjon = søknad.nesteSeksjon(inngangsvilkår)

                call.respond(seksjon)
            }
            post<Svar>("/faktum") { (id, type, verdi) ->
                val søknad = getOrCreateSøknad(call.parameters["søknadsId"]!!.toInt())

                when (type) {
                    "LocalDate" -> søknad.finnFaktum<LocalDate>(id).besvar(LocalDate.parse(verdi)) // bør løses et annet sted
                    "String" -> søknad.finnFaktum<String>(id).besvar(verdi)
                    "Boolean" -> søknad.finnFaktum<Boolean>(id).besvar(verdi.toBoolean())
                    "Int" -> søknad.finnFaktum<Int>(id).besvar(verdi.toInt())
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

internal val søknader = mutableMapOf<Int, Søknad>()
private fun getOrCreateSøknad(id: Int) =
    søknader.getOrPut(id) {
        // val fødselsdato = FaktumNavn(1, "Fødselsdato").faktum<LocalDate>()
        // val ønsketDato = FaktumNavn(2, "Ønsker dagpenger fra dato").faktum<LocalDate>()

        Søknad(
            Seksjon(Rolle.søker, ønsketDato, fødselsdato),
        )
    }
