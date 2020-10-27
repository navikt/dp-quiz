import meldinger.model.BehovMelding
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.helse.rapids_rivers.RapidsConnection

internal class BehovMediator(private val rapidsConnection: RapidsConnection) {

    internal fun håndter(seksjon: Seksjon, fødselsnummer: String) {
        val behov = seksjon.map { it.navn }
        val melding = BehovMelding(fødselsnummer = fødselsnummer, behov = *behov.toTypedArray())
        rapidsConnection.publish("", melding.toJson())
    }
}
