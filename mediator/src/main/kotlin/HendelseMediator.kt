import db.SøknadPersistance
import meldinger.model.ØnskerRettighetsavklaringMelding
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.helse.rapids_rivers.RapidsConnection

internal class HendelseMediator(private val søknadPersistance: SøknadPersistance, rapidsConnection: RapidsConnection) {
    private val behovMediator = BehovMediator(rapidsConnection)

    fun behandle(melding: ØnskerRettighetsavklaringMelding, fnr: String, type: Versjon.FaktagrupperType) {
        søknadPersistance.ny(fnr, type).also {
            behovMediator.håndter(it.nesteSeksjon(), fnr)
        }
    }
}
