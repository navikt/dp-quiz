package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import org.junit.jupiter.api.Test

internal class InnsendingSkjemastrategiTest {

    private lateinit var søknadprosess: Søknadprosess

    init {
        Innsending.registrer { prototypeSøknad ->
            søknadprosess = Versjon.id(Innsending.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }
    }

    @Test
    fun `Får skjemakode Annet - N6 `() {
        søknadprosess.envalg(Hvorfor.`hvorfor vil du sende oss ting`).besvar(Envalg("faktum.hvorfor.svar.endring"))
        søknadprosess.tekst(Hvorfor.`hva sender du oss`).besvar(Tekst("En vakker historie om hva jeg vil"))
        with(InnsendingSkjemastrategi().skjemakode(søknadprosess)) {
            kotlin.test.assertEquals("N6", this.skjemakode)
            kotlin.test.assertEquals("Annet", this.tittel)
        }
    }
}
