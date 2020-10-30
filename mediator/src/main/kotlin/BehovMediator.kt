import meldinger.model.BehovMelding
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.helse.rapids_rivers.RapidsConnection

internal class BehovMediator(private val rapidsConnection: RapidsConnection) {

    internal fun håndter(seksjon: Seksjon, fødselsnummer: String) {
        val behov = seksjon.map { it.navn }
        val melding = BehovMelding(fødselsnummer = fødselsnummer, seksjon = seksjon)
        rapidsConnection.publish(melding.toJson())
    }
}
