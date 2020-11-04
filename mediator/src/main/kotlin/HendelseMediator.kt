import db.SøknadPersistence
import mu.KotlinLogging
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.helse.rapids_rivers.RapidsConnection
import java.time.LocalDate
import java.util.UUID

private val log = KotlinLogging.logger {}

internal class HendelseMediator(private val søknadPersistence: SøknadPersistence, rapidsConnection: RapidsConnection) {
    private val behovMediator = BehovMediator(rapidsConnection)

    fun behandle(fnr: String, type: Versjon.FaktagrupperType) {
        søknadPersistence.ny(fnr, type).also {
            behovMediator.håndter(it.nesteSeksjon(), fnr, it.søknad.uuid)
        }
    }

    fun behandle(fnr: String, søknadId: UUID, faktumId: Int, svar: Any) {
        søknadPersistence.hent(søknadId, Versjon.FaktagrupperType.Web).also { faktagrupper ->
            besvar(faktagrupper, faktumId, svar)
            søknadPersistence.lagre(faktagrupper.søknad)
            if (faktagrupper.resultat() == null)
                behovMediator.håndter(
                    faktagrupper.nesteSeksjon(),
                    fnr,
                    faktagrupper.søknad.uuid
                )
            else
                behandleFerdigResultat()
        }
    }

    private fun behandleFerdigResultat() {
        log.info("Gratulerer, vi fikk et resultat, men denne metoden er ikke implementert")
    }

    private fun besvar(faktagrupper: Faktagrupper, faktumId: Int, svar: Any) {
        when (svar) {
            is Boolean -> faktagrupper.ja(faktumId).besvar(svar)
            is Int -> faktagrupper.heltall(faktumId).besvar(svar)
            is LocalDate -> faktagrupper.dato(faktumId).besvar(svar)
            is Inntekt -> faktagrupper.inntekt(faktumId).besvar(svar)
            is Dokument -> faktagrupper.dokument(faktumId).besvar(svar)
            else -> throw IllegalArgumentException("Ukjent svar-type: ${svar::class.java}")
        }
    }
}
