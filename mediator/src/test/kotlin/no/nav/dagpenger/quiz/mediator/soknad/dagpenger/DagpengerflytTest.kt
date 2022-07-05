package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`egne barn`
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertTrue

class DagpengerflytTest {

    private lateinit var søknadprosess: Søknadprosess

    init {
        Dagpenger.registrer { prototypeSøknad ->
            søknadprosess = Versjon.id(Dagpenger.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }
    }

    @Test
    fun `dagpenger flyt - letteste vei til ferdig`() {
        søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))

        søknadprosess.envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`).besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))

        søknadprosess.generator(Barnetillegg.`barn liste register`).besvar(1)
        søknadprosess.tekst("${Barnetillegg.`barn fornavn mellomnavn register`}.1").besvar(Tekst("test testen"))
        søknadprosess.tekst("${Barnetillegg.`barn etternavn register`}.1").besvar(Tekst("TTTT"))
        søknadprosess.dato("${Barnetillegg.`barn foedselsdato register`}.1").besvar(LocalDate.now().minusYears(10))
        søknadprosess.land("${Barnetillegg.`barn statsborgerskap register`}.1").besvar(Land("NOR"))

        // Besvares av bruker
        søknadprosess.boolsk("${Barnetillegg.`forsoerger du barnet register`}.1").besvar(false)
        søknadprosess.boolsk(`egne barn`).besvar(false)

        søknadprosess.nesteSeksjoner().onEach {
            it.somSpørsmål()
        }

        søknadprosess.dato(Arbeidsforhold.`dagpenger soknadsdato`).besvar(1.januar)
        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.ingen-passer"))

        søknadprosess.boolsk(EøsArbeidsforhold.`eos arbeid siste 36 mnd`).besvar(false)

        søknadprosess.boolsk(EgenNæring.`driver du egen naering`).besvar(false)
        søknadprosess.boolsk(EgenNæring.`driver du eget gaardsbruk`).besvar(false)

        søknadprosess.boolsk(Verneplikt.`avtjent militaer sivilforsvar tjeneste siste 12 mnd`).besvar(false)

        søknadprosess.boolsk(AndreYtelser.`andre ytelser mottatt eller sokt`).besvar(false)
        søknadprosess.boolsk(AndreYtelser.`utbetaling eller okonomisk gode tidligere arbeidsgiver`).besvar(false)

        søknadprosess.boolsk(Utdanning.`tar du utdanning`).besvar(false)
        søknadprosess.boolsk(Utdanning.`avsluttet utdanning siste 6 mnd`).besvar(false)
        søknadprosess.boolsk(Utdanning.`planlegger utdanning med dagpenger`).besvar(false)

        søknadprosess.boolsk(ReellArbeidssoker.`Kan jobbe heltid`).besvar(true)
        søknadprosess.boolsk(ReellArbeidssoker.`Kan du jobbe i hele Norge`).besvar(true)
        søknadprosess.boolsk(ReellArbeidssoker.`Kan ta alle typer arbeid`).besvar(true)
        søknadprosess.boolsk(ReellArbeidssoker.`Kan bytte yrke og eller gå ned i lønn`).besvar(true)

        søknadprosess.boolsk(Tilleggsopplysninger.`har tilleggsopplysninger`).besvar(false)
        assertTrue(søknadprosess.erFerdigFor(Rolle.nav, Rolle.søker), "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${søknadprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }}")
        assertTrue(søknadprosess.erFerdig(), "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${søknadprosess.nesteSeksjoner().flatten().joinToString { "\n$it" }}")
    }
}
