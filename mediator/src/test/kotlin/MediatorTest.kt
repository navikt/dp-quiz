import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.helse.rapids_rivers.RapidsConnection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MediatorTest {
    private val ønsketDato = FaktumNavn(2, "Ønsker dagpenger fra dato").faktum(LocalDate::class.java)
    private val fødselsdato = FaktumNavn(1, "Fødselsdato").faktum(LocalDate::class.java)
    private val dimisjonsdato = FaktumNavn(10, "Dimisjonsdato").faktum(LocalDate::class.java)
    private val seksjonNav = Seksjon("seksjon1", Rolle.nav, ønsketDato, fødselsdato)
    private val seksjonsSøker = Seksjon("seksjon2", Rolle.søker, dimisjonsdato)

    private val subsumsjoner = "".alle(ønsketDato før fødselsdato, dimisjonsdato før fødselsdato)
    val søknad = Søknad(
        seksjonNav,
        seksjonsSøker
    )

    @Test
    fun `skal produsere behov`() {
        val messages = mutableListOf<Pair<String?, String>>()
        val mediator = Mediator(
            connection = object : RapidsConnection() {
                override fun publish(message: String) {
                }

                override fun publish(key: String, message: String) {
                    messages.add(key to message)
                }

                override fun start() {
                    TODO("Not yet implemented")
                }

                override fun stop() {
                    TODO("Not yet implemented")
                }
            }
        )

        mediator.håndter(søknad, subsumsjoner)
        assertEquals(1, messages.size)
        assertEquals(søknad.toString(), messages[0].first)
        ønsketDato.besvar(LocalDate.now(), rolle = Rolle.nav)
        fødselsdato.besvar(LocalDate.now(), rolle = Rolle.nav)
        mediator.håndter(søknad, subsumsjoner)
        assertEquals(2, messages.size)
    }
}
