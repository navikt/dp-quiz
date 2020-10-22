import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.helse.rapids_rivers.RapidsConnection

internal class BehovMediator(private val rapidsConnection: RapidsConnection) {

    internal fun håndter(seksjon: Seksjon) {
        rapidsConnection.publish("""{}""")
    }
}
