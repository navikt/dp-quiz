package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.marshalling.ManuellBehandlingJsonBuilder
import no.nav.dagpenger.model.marshalling.NavJsonBuilder
import no.nav.dagpenger.model.marshalling.SaksbehandlerJsonBuilder
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder
import no.nav.dagpenger.model.seksjon.Utredningsprosess

// Forstår hvordan den skal stille spørsmål
abstract class Rolle {
    abstract val typeNavn: String

    companion object {
        val nav = Nav()
        val søker = Søker()
        val saksbehandler = Saksbehandler()
        val manuell = Manuell()
    }

    abstract fun spørsmål(utredningsprosess: Utredningsprosess, seksjonNavn: String): String
}

// Forstår hvordan den skal stille spørsmål til nav
class Nav internal constructor() : Rolle() {
    override val typeNavn = this.javaClass.simpleName.lowercase()

    override fun spørsmål(utredningsprosess: Utredningsprosess, seksjonNavn: String) =
        NavJsonBuilder(utredningsprosess, seksjonNavn).resultat().toString()
}

// Forstår hvordan den skal stille spørsmål til søker
class Søker internal constructor() : Rolle() {
    override val typeNavn = this.javaClass.simpleName.lowercase()
    override fun spørsmål(utredningsprosess: Utredningsprosess, seksjonNavn: String) =
        SøkerJsonBuilder(utredningsprosess).resultat().toString()
}

// Forstår hvordan den skal stille spørsmål til saksbehandler
class Saksbehandler internal constructor() : Rolle() {
    override val typeNavn = this.javaClass.simpleName.lowercase()
    override fun spørsmål(utredningsprosess: Utredningsprosess, seksjonNavn: String) =
        SaksbehandlerJsonBuilder(utredningsprosess, seksjonNavn).resultat().toString()
}

class Manuell internal constructor() : Rolle() {
    override val typeNavn = this.javaClass.simpleName.lowercase()
    override fun spørsmål(utredningsprosess: Utredningsprosess, seksjonNavn: String) =
        ManuellBehandlingJsonBuilder(utredningsprosess, seksjonNavn).resultat().toString()
}
