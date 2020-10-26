import db.FaktaPersistance
import meldinger.model.ØnskerRettighetsavklaringMelding
import no.nav.dagpenger.Prototype
import no.nav.helse.rapids_rivers.RapidsConnection

internal class HendelseMediator(private val faktaPersistance: FaktaPersistance, rapidsConnection: RapidsConnection) {
    private val behovMediator = BehovMediator(rapidsConnection)

    fun behandle(melding: ØnskerRettighetsavklaringMelding, fødselsnummer: String) {

        val prototype = Prototype()
        val enkelSøknad = prototype.søknad(fnr = fødselsnummer) // todo: Replace by Versjon
        faktaPersistance.persister(enkelSøknad)
        val nesteSeksjon = enkelSøknad.nesteSeksjon(prototype.inngangsvilkår.deepCopy(enkelSøknad))
        behovMediator.håndter(nesteSeksjon)
    }
}
