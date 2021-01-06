package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.marshalling.NavJsonBuilder
import no.nav.dagpenger.model.marshalling.SaksbehandlerJsonBuilder
import no.nav.dagpenger.model.seksjon.Søknadprosess

// Forstår hvordan den skal stille spørsmål
abstract class Rolle {
    abstract val typeNavn: String

    companion object {
        val nav = Nav()
        val søker = Søker()
        val saksbehandler = Saksbehandler()
        val manuell = Manuell()
    }

    abstract fun spørsmål(søknadprosess: Søknadprosess, seksjonNavn: String): String
}

// Forstår hvordan den skal stille spørsmål til nav
class Nav internal constructor() : Rolle() {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()

    override fun spørsmål(søknadprosess: Søknadprosess, seksjonNavn: String) =
        NavJsonBuilder(søknadprosess, seksjonNavn).resultat().toString()
}

class Søker internal constructor() : Rolle() {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun spørsmål(søknadprosess: Søknadprosess, seksjonNavn: String) = "søker"
}

class Saksbehandler internal constructor() : Rolle() {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun spørsmål(søknadprosess: Søknadprosess, seksjonNavn: String) =
        SaksbehandlerJsonBuilder(søknadprosess, seksjonNavn).resultat().toString()
}

class Manuell internal constructor() : Rolle() {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun spørsmål(søknadprosess: Søknadprosess, seksjonNavn: String) = "manuell"
}
