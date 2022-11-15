package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InnsendingMetadataStrategiTest {
    private lateinit var søknadprosess: Søknadprosess

    init {
        Innsending.registrer { prototypeSøknad ->
            søknadprosess = Versjon.id(Innsending.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }
    }

    @Test
    fun `Får skjemakode Ettersending til søknad `() {
        søknadprosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`).besvar(Envalg("faktum.generell-innsending.hvorfor.svar.endring"))
        søknadprosess.tekst(GenerellInnsending.`skriv kort hvorfor du sender inn dokumentasjon`).besvar(Tekst("En vakker historie om hva jeg vil"))
        with(InnsendingMetadataStrategi().metadata(søknadprosess)) {
            assertEquals("GENERELL_INNSENDING", this.skjemakode)
            assertEquals("Generell innsending", this.tittel)
        }
    }
}
