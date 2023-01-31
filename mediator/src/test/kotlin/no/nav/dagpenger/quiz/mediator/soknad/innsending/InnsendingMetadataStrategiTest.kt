package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Versjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InnsendingMetadataStrategiTest {
    private lateinit var faktagrupper: Faktagrupper

    init {
        Innsending.registrer { prototypeSøknad ->
            faktagrupper = Versjon.id(Innsending.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }
    }

    @Test
    fun `Får skjemakode Ettersending til søknad `() {
        faktagrupper.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`).besvar(Envalg("faktum.generell-innsending.hvorfor.svar.endring"))
        faktagrupper.tekst(GenerellInnsending.`skriv kort hvorfor du sender inn dokumentasjon`).besvar(Tekst("En vakker historie om hva jeg vil"))
        with(InnsendingMetadataStrategi().metadata(faktagrupper)) {
            assertEquals("GENERELL_INNSENDING", this.skjemakode)
            assertEquals("Generell innsending", this.tittel)
        }
    }
}
