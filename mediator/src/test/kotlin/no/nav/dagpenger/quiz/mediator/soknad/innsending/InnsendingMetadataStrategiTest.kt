package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Henvendelser
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.helpers.testPerson
import no.nav.dagpenger.quiz.mediator.soknad.Prosesser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InnsendingMetadataStrategiTest {
    private lateinit var prosess: Prosess

    init {
        Innsending.registrer()
        prosess = Henvendelser.prosess(testPerson, Prosesser.Innsending)
    }

    @Test
    fun `Får skjemakode Ettersending til søknad `() {
        prosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`).besvar(Envalg("faktum.generell-innsending.hvorfor.svar.endring"))
        prosess.tekst(GenerellInnsending.`skriv kort hvorfor du sender inn dokumentasjon`).besvar(Tekst("En vakker historie om hva jeg vil"))
        with(InnsendingMetadataStrategi().metadata(prosess)) {
            assertEquals("GENERELL_INNSENDING", this.skjemakode)
            assertEquals("Generell innsending", this.tittel)
        }
    }
}
