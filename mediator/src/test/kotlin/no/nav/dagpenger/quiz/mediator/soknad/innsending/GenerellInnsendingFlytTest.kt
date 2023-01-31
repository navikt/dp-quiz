package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Versjon
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GenerellInnsendingFlytTest {
    private lateinit var faktagrupper: Faktagrupper

    init {
        Innsending.registrer { prototypeSøknad ->
            faktagrupper = Versjon.id(Innsending.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }
    }

    @Test
    fun `Innsending flyt - letteste vei til ferdig`() {
        faktagrupper.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.endring"))
        faktagrupper.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))

        faktagrupper.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            faktagrupper.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            faktagrupper.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertTrue(
            faktagrupper.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            faktagrupper.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
        assertTrue(
            faktagrupper.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            faktagrupper.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
    }

    @Test
    fun `Innsending flyt - uten dokumentasjon`() {
        faktagrupper.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.endring"))
        faktagrupper.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))

        faktagrupper.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            faktagrupper.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            faktagrupper.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertTrue(
            faktagrupper.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            faktagrupper.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
        assertTrue(
            faktagrupper.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            faktagrupper.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
    }

    @Test
    fun `Innsending flyt - svar vet-ikke`() {
        faktagrupper.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.annet"))
        faktagrupper.tekst(GenerellInnsending.`skriv kort hvorfor du sender inn dokumentasjon`).besvar(Tekst("Derfor"))
        faktagrupper.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))

        faktagrupper.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        assertTrue(
            faktagrupper.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig for søker. Mangler svar på ${
            faktagrupper.nesteSeksjoner().flatten().filterNot { it.erBesvart() }.joinToString { "\n$it" }
            }"
        )

        assertTrue(
            faktagrupper.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            faktagrupper.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
        assertTrue(
            faktagrupper.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
            faktagrupper.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }"
        )
    }

    @Test
    fun `Envalg nuller ut påfølgingsspørsmålene`() {
        // Begynn uten svar
        faktagrupper.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.annet"))
        assertFalse(faktagrupper.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        // Er besvart etter svar
        faktagrupper.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))
        assertTrue(faktagrupper.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        // Nulles ut av endret envalg
        faktagrupper.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.ettersending"))
        assertFalse(faktagrupper.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        // Nulles ut igjen
        faktagrupper.tekst(GenerellInnsending.`tittel på dokument`).besvar(Tekst("En vakker historie om hva jeg vil"))
        assertTrue(faktagrupper.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
        faktagrupper.envalg(GenerellInnsending.`hvorfor sender du inn dokumentasjon`)
            .besvar(Envalg("faktum.generell-innsending.hvorfor.svar.annet"))
        assertFalse(faktagrupper.tekst(GenerellInnsending.`tittel på dokument`).erBesvart())
    }
}
