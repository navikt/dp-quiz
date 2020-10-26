import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.helse.rapids_rivers.RapidsConnection

internal class BehovMediator(private val rapidsConnection: RapidsConnection) {

    private val kafkaBehov = mapOf<String, String>(
        "Fødesldato" to "Personalia",
        "Ønsker dagpenger fra dato" to "Personalia",
        "Dato for bortfall på grunn av alder" to "Personalia"
    )

    internal fun håndter(seksjon: Seksjon) {
        val behov = seksjon.onEach{
            if (! it.erBesvart())
                it.navn
        }
        rapidsConnection.publish("""{}""")
    }

    private fun mapBehov(faktumNavn: List<String>): List<String> {
        var behovIdentificators = mutableListOf<String>()
        faktumNavn.forEach {

        }
        return behovIdentificators
    }
}
