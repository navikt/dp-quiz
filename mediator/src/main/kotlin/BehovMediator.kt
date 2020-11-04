import meldinger.model.BehovMelding
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.helse.rapids_rivers.RapidsConnection
import java.util.UUID

internal class BehovMediator(private val rapidsConnection: RapidsConnection) {

    internal fun håndter(seksjon: Seksjon, fnr: String, søknadId: UUID) {
        val melding = BehovMelding(fnr = fnr, seksjon = seksjon, søknadId)
        rapidsConnection.publish(melding.toJson())
    }
}
