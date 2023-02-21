package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Versjon
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GenerellInnsendingFlytTest {
    private lateinit var prosess: Prosess

    init {
        Innsending.registrer { prototypeSøknad ->
            prosess = Versjon.id(Innsending.VERSJON_ID)
                .utredningsprosess(prototypeSøknad)
        }
    }

    @Test
    fun `Innsending flyt - letteste vei til ferdig`() {
        prosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.endring"))
        prosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))

        prosess.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            prosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            prosess.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertTrue(
            prosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            prosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
        assertTrue(
            prosess.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            prosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
    }

    @Test
    fun `Innsending flyt - uten dokumentasjon`() {
        prosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.endring"))
        prosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))

        prosess.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            prosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            prosess.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertTrue(
            prosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            prosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
        assertTrue(
            prosess.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            prosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
    }

    @Test
    fun `Innsending flyt - svar vet-ikke`() {
        prosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.annet"))
        prosess.tekst(GenerellInnsending.`skriv kort hvorfor du sender inn dokumentasjon`).besvar(Tekst("Derfor"))
        prosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))

        prosess.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            prosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            prosess.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertTrue(
            prosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            prosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
        assertTrue(
            prosess.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            prosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
    }

    @Test
    fun `Envalg nuller ut påfølgingsspørsmålene`() {
        // Begynn uten svar
        prosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.annet"))
        assertFalse(prosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        // Er besvart etter svar
        prosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))
        assertTrue(prosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        // Nulles ut av endret envalg
        prosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.ettersending"))
        assertFalse(prosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        // Nulles ut igjen
        prosess.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))
        assertTrue(prosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        prosess.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.annet"))
        assertFalse(prosess.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
    }
}
