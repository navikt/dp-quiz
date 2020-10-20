import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SeksjonJsonBuilder
import no.nav.helse.rapids_rivers.RapidsConnection

class Mediator(private val connection: RapidsConnection) {
    private val publishedIds = mutableMapOf<String, Søknad>()
    fun håndter(søknad: Søknad, subsumsjon: Subsumsjon) {
        val nesteSeksjon = søknad.nesteSeksjon(subsumsjon)
        val id = søknad.toString()
        publishedIds[id] = søknad
        connection.publish(id, SeksjonJsonBuilder(nesteSeksjon).toString())
    }
}
