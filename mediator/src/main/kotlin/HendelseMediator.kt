import db.Søknader
import meldinger.model.ØnskerRettighetsavklaring
import meldinger.model.ØnskerRettighetsavklaringMelding
import no.nav.dagpenger.EnkelSøknad

internal class HendelseMediator(private val søknader: Søknader) {
    fun behandle(melding: ØnskerRettighetsavklaringMelding, ønskerRettighetsavklaring: ØnskerRettighetsavklaring) {
        val enkelSøknad = EnkelSøknad().søknad()
        søknader.nySøknad(enkelSøknad)
    }
}
