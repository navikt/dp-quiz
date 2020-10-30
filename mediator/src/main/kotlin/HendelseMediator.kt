import db.SøknadPersistance
import meldinger.model.ØnskerRettighetsavklaringMelding
import no.nav.helse.rapids_rivers.RapidsConnection
import soknad.Prototype

internal class HendelseMediator(private val søknadPersistance: SøknadPersistance, rapidsConnection: RapidsConnection) {
    private val behovMediator = BehovMediator(rapidsConnection)

    fun behandle(melding: ØnskerRettighetsavklaringMelding, fødselsnummer: String) {
        val prototype = Prototype()
        val enkelFaktagrupper = prototype.faktagrupper(fnr = fødselsnummer)
        val nesteSeksjon = enkelFaktagrupper.nesteSeksjon(prototype.inngangsvilkår.deepCopy(enkelFaktagrupper))
        behovMediator.håndter(nesteSeksjon, fødselsnummer)
    }
}
