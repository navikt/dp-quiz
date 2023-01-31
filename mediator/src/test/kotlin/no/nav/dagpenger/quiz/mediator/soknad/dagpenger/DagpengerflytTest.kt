package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.MedSøknad
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`egne barn`
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertTrue

class DagpengerflytTest {
    private lateinit var utredningsprosess: Utredningsprosess

    init {
        Dagpenger.registrer { prototypeSøknad ->
            utredningsprosess = Versjon.id(Dagpenger.VERSJON_ID)
                .søknadprosess(prototypeSøknad)
        }
    }

    @Test
    fun `dagpenger flyt - letteste vei til ferdig`() {
        utredningsprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))

        utredningsprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        utredningsprosess.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)
        utredningsprosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.ingen-passer"))

        utredningsprosess.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        utredningsprosess.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(false)

        utredningsprosess.boolsk(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd`).besvar(true)

        vernepliktMåDokumenteres(utredningsprosess)

        utredningsprosess.boolsk(AndreYtelser.`andre ytelser mottatt eller søkt`).besvar(false)
        utredningsprosess.boolsk(AndreYtelser.`utbetaling eller økonomisk gode tidligere arbeidsgiver`).besvar(false)

        utredningsprosess.boolsk(Utdanning.`tar du utdanning`).besvar(false)
        utredningsprosess.boolsk(Utdanning.`avsluttet utdanning siste 6 mnd`).besvar(false)
        utredningsprosess.boolsk(Utdanning.`planlegger utdanning med dagpenger`).besvar(false)

        utredningsprosess.generator(Barnetillegg.`barn liste register`).besvar(1)
        utredningsprosess.tekst("${Barnetillegg.`barn fornavn mellomnavn register`}.1").besvar(Tekst("test testen"))
        utredningsprosess.tekst("${Barnetillegg.`barn etternavn register`}.1").besvar(Tekst("TTTT"))
        utredningsprosess.dato("${Barnetillegg.`barn fødselsdato register`}.1").besvar(LocalDate.now().minusYears(10))
        utredningsprosess.land("${Barnetillegg.`barn statsborgerskap register`}.1").besvar(Land("NOR"))
        // Besvares av bruker
        utredningsprosess.boolsk("${Barnetillegg.`forsørger du barnet register`}.1").besvar(false)
        // Egne barn
        utredningsprosess.boolsk(`egne barn`).besvar(true)
        utredningsprosess.generator(Barnetillegg.`barn liste`).besvar(1)
        utredningsprosess.tekst("${Barnetillegg.`barn fornavn mellomnavn`}.1").besvar(Tekst("test testen"))
        utredningsprosess.tekst("${Barnetillegg.`barn etternavn`}.1").besvar(Tekst("TTTT"))
        utredningsprosess.dato("${Barnetillegg.`barn fødselsdato`}.1").besvar(LocalDate.now().minusYears(10))
        utredningsprosess.land("${Barnetillegg.`barn statsborgerskap`}.1").besvar(Land("NOR"))
        utredningsprosess.boolsk("${Barnetillegg.`forsørger du barnet`}.1").besvar(true)

        egneBarnMåDokumenteres(utredningsprosess)

        utredningsprosess.boolsk(ReellArbeidssoker.`kan jobbe heltid`).besvar(true)
        utredningsprosess.boolsk(ReellArbeidssoker.`kan du jobbe i hele Norge`).besvar(true)
        utredningsprosess.boolsk(ReellArbeidssoker.`kan ta alle typer arbeid`).besvar(true)
        utredningsprosess.boolsk(ReellArbeidssoker.`kan bytte yrke eller gå ned i lønn`).besvar(true)

        utredningsprosess.boolsk(Tilleggsopplysninger.`har tilleggsopplysninger`).besvar(false)

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

    private fun vernepliktMåDokumenteres(utredningsprosess: Utredningsprosess) {
        MedSøknad(utredningsprosess) {
            seksjon("verneplikt") {
                fakta {
                    boolsk("faktum.avtjent-militaer-sivilforsvar-tjeneste-siste-12-mnd") {
                        erBesvart()
                        sannsynliggjøresAv {
                            dokument("faktum.dokument-avtjent-militaer-sivilforsvar-tjeneste-siste-12-mnd-dokumentasjon") {
                                erIkkeBesvart()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun egneBarnMåDokumenteres(utredningsprosess: Utredningsprosess) {
        MedSøknad(utredningsprosess) {
            seksjon("barnetillegg") {
                fakta(false, false) {
                    generator("faktum.barn-liste") {
                        svar(1) {
                            boolsk("faktum.forsoerger-du-barnet") {
                                erBesvartMed(true)
                                sannsynliggjøresAv {
                                    dokument("faktum.dokument-foedselsattest-bostedsbevis-for-barn-under-18aar") {
                                        erIkkeBesvart()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
