import db.Søknader
import meldinger.model.ØnskerRettighetsavklaring
import meldinger.model.ØnskerRettighetsavklaringMelding
import no.nav.dagpenger.EnkelSøknad
import no.nav.dagpenger.regelverk.inngangsvilkår
import no.nav.helse.rapids_rivers.RapidsConnection

internal class HendelseMediator(private val søknader: Søknader, private val rapidsConnection: RapidsConnection) {
    private val behovMediator = BehovMediator(rapidsConnection)

    fun behandle(melding: ØnskerRettighetsavklaringMelding, ønskerRettighetsavklaring: ØnskerRettighetsavklaring) {
        val enkelSøknad = EnkelSøknad().søknad()
        søknader.nySøknad(enkelSøknad)
        val nesteSeksjon = enkelSøknad.nesteSeksjon(inngangsvilkår.deepCopy(enkelSøknad))
    }
}
