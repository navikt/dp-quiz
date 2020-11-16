package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.seksjon.Seksjon

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
    abstract fun spørsmål(seksjon: Seksjon): String
}

// Forstår hvordan den skal stille spørsmål til nav
class NavRolle : Rolle("nav") {
    override fun spørsmål(seksjon: Seksjon) = ""
}

class SøkerRolle : Rolle("søker") {
    override fun spørsmål(seksjon: Seksjon) = ""
}

class SaksbehandlerRolle : Rolle("saksbehandler") {
    override fun spørsmål(seksjon: Seksjon) = ""
}
