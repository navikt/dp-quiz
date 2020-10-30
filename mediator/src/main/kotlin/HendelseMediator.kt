import db.SøknadPersistance
import meldinger.model.ØnskerRettighetsavklaringMelding
import no.nav.helse.rapids_rivers.RapidsConnection
import soknad.Prototype

internal class HendelseMediator(private val søknadPersistance: SøknadPersistance, rapidsConnection: RapidsConnection) {
    private val behovMediator = BehovMediator(rapidsConnection)

    fun behandle(melding: ØnskerRettighetsavklaringMelding, fødselsnummer: String) {
        val prototype = Prototype()
        val enkelFaktaGrupper = prototype.faktagrupper(fnr = fødselsnummer)
        val nesteSeksjon = enkelFaktaGrupper.nesteSeksjon(prototype.inngangsvilkår.deepCopy(enkelFaktaGrupper))
        behovMediator.håndter(nesteSeksjon, fødselsnummer)
    }
}
