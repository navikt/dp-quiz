package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GenerellInnsendingFlytTest {
    private lateinit var søknadprosess: Søknadprosess

    init {
        Innsending.registrer { prototypeSøknad ->
            søknadprosess = Versjon.id(Innsending.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }
    }

    @Test
    fun `Innsending flyt - letteste vei til ferdig`() {
        søknadprosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.endring"))
        søknadprosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))

        søknadprosess.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            søknadprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            søknadprosess.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertTrue(
            søknadprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            søknadprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
        assertTrue(
            søknadprosess.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            søknadprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
    }

    @Test
    fun `Innsending flyt - uten dokumentasjon`() {
        søknadprosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.endring"))
        søknadprosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))

        søknadprosess.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            søknadprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            søknadprosess.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertTrue(
            søknadprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            søknadprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
        assertTrue(
            søknadprosess.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            søknadprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
    }

    @Test
    fun `Innsending flyt - svar vet-ikke`() {
        søknadprosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.annet"))
        søknadprosess.tekst(GenerellInnsending.`skriv kort hvorfor du sender inn dokumentasjon`).besvar(Tekst("Derfor"))
        søknadprosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))

        søknadprosess.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            søknadprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            søknadprosess.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertTrue(
            søknadprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            søknadprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
        assertTrue(
            søknadprosess.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            søknadprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
    }

    @Test
    fun `Envalg nuller ut påfølgingsspørsmålene`() {
        // Begynn uten svar
        søknadprosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.annet"))
        assertFalse(søknadprosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        // Er besvart etter svar
        søknadprosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))
        assertTrue(søknadprosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        // Nulles ut av endret envalg
        søknadprosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.ettersending"))
        assertFalse(søknadprosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        // Nulles ut igjen
        søknadprosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))
        assertTrue(søknadprosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        søknadprosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.annet"))
        assertFalse(søknadprosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
    }
}
