import db.SøknadPersistence
import meldinger.model.FaktumSvarMelding
import meldinger.model.ØnskerRettighetsavklaringMelding
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.helse.rapids_rivers.RapidsConnection
import java.time.LocalDate
import java.util.UUID

internal class HendelseMediator(private val søknadPersistence: SøknadPersistence, rapidsConnection: RapidsConnection) {
    private val behovMediator = BehovMediator(rapidsConnection)

    fun behandle(melding: ØnskerRettighetsavklaringMelding, fnr: String, type: Versjon.FaktagrupperType) {
        søknadPersistence.ny(fnr, type).also {
            behovMediator.håndter(it.nesteSeksjon(), fnr, it.søknad.uuid)
        }
    }

    fun behandle(melding: FaktumSvarMelding, søknadId: UUID, faktumId: Int, svar: Any, type: Versjon.FaktagrupperType, rolle: Rolle) {
        søknadPersistence.hent(søknadId, type).also { faktagrupper ->
            besvar(faktagrupper, faktumId, rolle, svar)
            søknadPersistence.lagre(faktagrupper.søknad)
            behovMediator.håndter(faktagrupper.nesteSeksjon(), melding.fødselsnummer, faktagrupper.søknad.uuid)
        }
    }

    private fun besvar(faktagrupper: Faktagrupper, faktumId: Int, rolle: Rolle, svar: Any) {
        when (svar) {
            is Boolean -> faktagrupper.ja(faktumId).besvar(svar, rolle)
            is Int -> faktagrupper.heltall(faktumId).besvar(svar, rolle)
            is LocalDate -> faktagrupper.dato(faktumId).besvar(svar, rolle)
            else -> throw IllegalArgumentException("Ukjent svar-type: ${svar::class.java}")
        }
    }
}
