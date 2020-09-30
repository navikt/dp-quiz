package no.nav.dagpenger

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SeksjonJsonBuilder
import no.nav.dagpenger.model.visitor.SubsumsjonJsonBuilder
import no.nav.dagpenger.model.visitor.SøknadVisitor
import no.nav.dagpenger.regelverk.dimisjonsdato
import no.nav.dagpenger.regelverk.fødselsdato
import no.nav.dagpenger.regelverk.inngangsvilkår
import no.nav.dagpenger.regelverk.utestengt
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
        route("/soknad/{søknadsId}") {
            get("/neste-seksjon") {
                val søknad = getOrCreateSøknad(UUID.fromString(call.parameters["søknadsId"]!!))
                val seksjon = søknad.nesteSeksjon(inngangsvilkår)

                call.respond(SeksjonJsonBuilder(seksjon).resultat())
            }
            put("/faktum/{faktumId}") {
                val (verdi, type) = call.receive<Svar>()
                val søknad = getOrCreateSøknad(UUID.fromString(call.parameters["søknadsId"]!!))
                val id = call.parameters["faktumId"]!!

                val faktum = when (type.toLowerCase()) {
                    "localdate" -> søknad.finnFaktum<LocalDate>(id).besvar(LocalDate.parse(verdi))
                    "string" -> søknad.finnFaktum<String>(id).besvar(verdi)
                    "boolean" -> søknad.finnFaktum<Boolean>(id).besvar(verdi.toBoolean())
                    "int" -> søknad.finnFaktum<Int>(id).besvar(verdi.toInt())
                    else -> throw IllegalArgumentException("BOOM")
                }
                call.respond(SeksjonJsonBuilder(faktum.finnSeksjon(søknad)).resultat())
            }
            get("/subsumsjoner") {
                call.respond(SubsumsjonJsonBuilder(inngangsvilkår).resultat())
            }
        }
    }
}

internal fun <R : Comparable<R>> Søknad.finnFaktum(id: String): Faktum<R> =
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

internal fun Faktum<*>.finnSeksjon(søknad: Søknad): Seksjon =
    object : SøknadVisitor {
        lateinit var seksjon: Seksjon

        override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>) {
            if (this::seksjon.isInitialized) return
            fakta.find { it.id == this@finnSeksjon.id }?.let {
                this.seksjon = seksjon
            }
        }
    }.let {
        søknad.accept(it)
        it.seksjon
    }

internal val søknader = mutableMapOf<UUID, Søknad>()
private fun getOrCreateSøknad(id: UUID) =
    søknader.getOrPut(id) {
        Søknad(
            Seksjon(Rolle.søker, ønsketDato, fødselsdato),
            Seksjon(Rolle.søker, dimisjonsdato),
            Seksjon(Rolle.søker, utestengt),
        )
    }
