package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GenerellInnsendingFlytTest {
    private lateinit var utredningsprosess: Utredningsprosess

    init {
        Innsending.registrer { prototypeSøknad ->
            utredningsprosess = Versjon.id(Innsending.VERSJON_ID)
                .utredningsprosess(prototypeSøknad)
        }
    }

    @Test
    fun `Innsending flyt - letteste vei til ferdig`() {
        utredningsprosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.endring"))
        utredningsprosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))

        utredningsprosess.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            utredningsprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            utredningsprosess.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertTrue(
            utredningsprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            utredningsprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
        assertTrue(
            utredningsprosess.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            utredningsprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
    }

    @Test
    fun `Innsending flyt - uten dokumentasjon`() {
        utredningsprosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.endring"))
        utredningsprosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))

        utredningsprosess.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            utredningsprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            utredningsprosess.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertTrue(
            utredningsprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            utredningsprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
        assertTrue(
            utredningsprosess.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            utredningsprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
    }

    @Test
    fun `Innsending flyt - svar vet-ikke`() {
        utredningsprosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.annet"))
        utredningsprosess.tekst(GenerellInnsending.`skriv kort hvorfor du sender inn dokumentasjon`).besvar(Tekst("Derfor"))
        utredningsprosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))

        utredningsprosess.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            utredningsprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            utredningsprosess.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertTrue(
            utredningsprosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            utredningsprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
        assertTrue(
            utredningsprosess.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            utredningsprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
    }

    @Test
    fun `Envalg nuller ut påfølgingsspørsmålene`() {
        // Begynn uten svar
        utredningsprosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.annet"))
        assertFalse(utredningsprosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        // Er besvart etter svar
        utredningsprosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))
        assertTrue(utredningsprosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        // Nulles ut av endret envalg
        utredningsprosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.ettersending"))
        assertFalse(utredningsprosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        // Nulles ut igjen
        utredningsprosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))
        assertTrue(utredningsprosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        utredningsprosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.annet"))
        assertFalse(utredningsprosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
    }
}
