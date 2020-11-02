import db.SøknadPersistance
import meldinger.model.FaktumSvarMelding
import meldinger.model.ØnskerRettighetsavklaringMelding
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.helse.rapids_rivers.RapidsConnection
import java.util.UUID

internal class HendelseMediator(private val søknadPersistance: SøknadPersistance, rapidsConnection: RapidsConnection) {
    private val behovMediator = BehovMediator(rapidsConnection)

    fun behandle(melding: ØnskerRettighetsavklaringMelding, fnr: String, type: Versjon.FaktagrupperType) {
        søknadPersistance.ny(fnr, type).also {
            behovMediator.håndter(it.nesteSeksjon(), fnr, it.søknad.uuid)
        }
    }

    fun behandle(melding: FaktumSvarMelding, søknadId: UUID, faktumId: Int, svar: Boolean, type: Versjon.FaktagrupperType, rolle: Rolle) {
        søknadPersistance.hent(søknadId, type).also { faktagrupper ->
            faktagrupper.ja(faktumId).besvar(svar, rolle)
            søknadPersistance.lagre(faktagrupper.søknad)
            behovMediator.håndter(faktagrupper.nesteSeksjon(), melding.fødselsnummer, faktagrupper.søknad.uuid)
        }
    }
}
