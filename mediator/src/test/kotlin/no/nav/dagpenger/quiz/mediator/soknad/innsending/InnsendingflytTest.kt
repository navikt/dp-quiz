package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class InnsendingflytTest {
    private lateinit var søknadprosess: Søknadprosess

    init {
        Innsending.registrer { prototypeSøknad ->
            søknadprosess = Versjon.id(Innsending.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }
    }

    @Test
    fun `Innsending flyt - letteste vei til ferdig`() {
        søknadprosess.envalg(Hvorfor.`hvorfor vil du sende oss ting`).besvar(Envalg("faktum.hvorfor.svar.endring"))
        søknadprosess.tekst(Hvorfor.`hva sender du oss`).besvar(Tekst("En vakker historie om hva jeg vil"))

        søknadprosess.boolsk(Hvorfor.`dokumentasjon tilgjengelig`).besvar(true)
        søknadprosess.dokument(Hvorfor.`dokumentasjon`).besvar(Dokument(LocalDate.now(), "urn:foo:bar"))
        søknadprosess.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            søknadprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            søknadprosess.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertFalse(søknadprosess.erFerdig())
        søknadprosess.boolsk(Hvorfor.`godkjenning av dokumentasjon`).besvar(true)

        assertTrue(
            søknadprosess.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            søknadprosess.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )
    }

    @Test
    fun `Innsending flyt - uten dokumentasjon`() {
        søknadprosess.envalg(Hvorfor.`hvorfor vil du sende oss ting`).besvar(Envalg("faktum.hvorfor.svar.endring"))
        søknadprosess.tekst(Hvorfor.`hva sender du oss`).besvar(Tekst("En vakker historie om hva jeg vil"))

        søknadprosess.boolsk(Hvorfor.`dokumentasjon tilgjengelig`).besvar(false)
        søknadprosess.tekst(Hvorfor.`dokumentasjon årsak`).besvar(Tekst("Jeg har bare så lyst å fortelle dere noe!"))
        søknadprosess.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            søknadprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            søknadprosess.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertFalse(søknadprosess.erFerdig())
        søknadprosess.boolsk(Hvorfor.`godkjenning av dokumentasjon`).besvar(true)

        assertTrue(
            søknadprosess.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            søknadprosess.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )
    }
}
