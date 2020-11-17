package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.marshalling.NavJsonBuilder
import no.nav.dagpenger.model.marshalling.SaksbehandlerJsonBuilder
import no.nav.dagpenger.model.seksjon.Søknadprosess

// Forstår hvordan den skal stille spørsmål
abstract class Rolle(val name: String) {
    companion object {
        val nav = NavRolle()
        val søker = SøkerRolle()
        val saksbehandler = SaksbehandlerRolle()

        fun valueOf(thing: String) = when (thing) {
            "nav" -> nav
            "søker" -> søker
            "saksbehandler" -> saksbehandler
            else -> throw IllegalArgumentException()
        }
    }
    abstract fun spørsmål(søknadprosess: Søknadprosess, seksjonNavn: String): String
}

// Forstår hvordan den skal stille spørsmål til nav
class NavRolle : Rolle("nav") {
    override fun spørsmål(søknadprosess: Søknadprosess, seksjonNavn: String) = NavJsonBuilder(søknadprosess, seksjonNavn).resultat().toString()
}

class SøkerRolle : Rolle("søker") {
    override fun spørsmål(søknadprosess: Søknadprosess, seksjonNavn: String) = "søker"
}

class SaksbehandlerRolle : Rolle("saksbehandler") {
    override fun spørsmål(søknadprosess: Søknadprosess, seksjonNavn: String) = SaksbehandlerJsonBuilder(søknadprosess, seksjonNavn).resultat().toString()
}
