package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.MedSøknad
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Henvendelser
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.helpers.testPerson
import no.nav.dagpenger.quiz.mediator.soknad.Prosesser
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`egne barn`
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertTrue

class DagpengerflytTest {
    private var prosess: Prosess

    init {
        Dagpenger.registrer()
        prosess = Henvendelser.prosess(
            testPerson,
            Prosesser.Søknad,
        )
    }

    @Test
    fun `dagpenger flyt - letteste vei til ferdig`() {
        prosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))

        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        prosess.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)
        prosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.ingen-passer"))

        prosess.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        prosess.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(false)

        prosess.boolsk(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd`).besvar(true)

        vernepliktMåDokumenteres(prosess)

        prosess.boolsk(AndreYtelser.`andre ytelser mottatt eller søkt`).besvar(false)
        prosess.boolsk(AndreYtelser.`utbetaling eller økonomisk gode tidligere arbeidsgiver`).besvar(false)

        prosess.boolsk(Utdanning.`tar du utdanning`).besvar(false)
        prosess.boolsk(Utdanning.`avsluttet utdanning siste 6 mnd`).besvar(false)
        prosess.boolsk(Utdanning.`planlegger utdanning med dagpenger`).besvar(false)

        prosess.generator(Barnetillegg.`barn liste register`).besvar(1)
        prosess.tekst("${Barnetillegg.`barn fornavn mellomnavn register`}.1").besvar(Tekst("test testen"))
        prosess.tekst("${Barnetillegg.`barn etternavn register`}.1").besvar(Tekst("TTTT"))
        prosess.dato("${Barnetillegg.`barn fødselsdato register`}.1").besvar(LocalDate.now().minusYears(10))
        prosess.land("${Barnetillegg.`barn statsborgerskap register`}.1").besvar(Land("NOR"))
        // Besvares av bruker
        prosess.boolsk("${Barnetillegg.`forsørger du barnet register`}.1").besvar(false)
        // Egne barn
        prosess.boolsk(`egne barn`).besvar(true)
        prosess.generator(Barnetillegg.`barn liste`).besvar(1)
        prosess.tekst("${Barnetillegg.`barn fornavn mellomnavn`}.1").besvar(Tekst("test testen"))
        prosess.tekst("${Barnetillegg.`barn etternavn`}.1").besvar(Tekst("TTTT"))
        prosess.dato("${Barnetillegg.`barn fødselsdato`}.1").besvar(LocalDate.now().minusYears(10))
        prosess.land("${Barnetillegg.`barn statsborgerskap`}.1").besvar(Land("NOR"))
        prosess.boolsk("${Barnetillegg.`forsørger du barnet`}.1").besvar(true)

        egneBarnMåDokumenteres(prosess)

        prosess.boolsk(ReellArbeidssoker.`kan jobbe heltid`).besvar(true)
        prosess.boolsk(ReellArbeidssoker.`kan du jobbe i hele Norge`).besvar(true)
        prosess.boolsk(ReellArbeidssoker.`kan ta alle typer arbeid`).besvar(true)
        prosess.boolsk(ReellArbeidssoker.`kan bytte yrke eller gå ned i lønn`).besvar(true)

        prosess.boolsk(Tilleggsopplysninger.`har tilleggsopplysninger`).besvar(false)

        assertTrue(
            prosess.erFerdigFor(Rolle.nav, Rolle.søker),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
                prosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }",
        )
        assertTrue(
            prosess.erFerdig(),
            "Forventet at Dagpenger søknadsprosessen ikke var ferdig. Mangler svar på ${
                prosess.nesteSeksjoner().flatten().joinToString { "\n$it" }
            }",
        )
    }

    private fun vernepliktMåDokumenteres(prosess: Prosess) {
        MedSøknad(prosess) {
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

    private fun egneBarnMåDokumenteres(prosess: Prosess) {
        MedSøknad(prosess) {
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
