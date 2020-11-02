import db.SøknadPersistance
import meldinger.model.ØnskerRettighetsavklaringMelding
import no.nav.helse.rapids_rivers.RapidsConnection

internal class HendelseMediator(private val søknadPersistance: SøknadPersistance, rapidsConnection: RapidsConnection) {
    private val behovMediator = BehovMediator(rapidsConnection)

    fun behandle(melding: ØnskerRettighetsavklaringMelding, fødselsnummer: String) {
    }
}
