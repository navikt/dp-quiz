import meldinger.model.BehovMelding
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.helse.rapids_rivers.RapidsConnection
import java.util.UUID

internal class BehovMediator(private val rapidsConnection: RapidsConnection) {

    internal fun håndter(seksjon: Seksjon, fødselsnummer: String, søknadId: UUID) {
        val behov = seksjon.map { it.navn }
        val melding = BehovMelding(fødselsnummer = fødselsnummer, seksjon = seksjon, søknadId)
        rapidsConnection.publish(melding.toJson())
    }
}
