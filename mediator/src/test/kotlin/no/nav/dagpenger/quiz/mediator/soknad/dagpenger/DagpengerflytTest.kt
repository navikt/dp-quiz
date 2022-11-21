package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.MedSøknad
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
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

        søknadprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        søknadprosess.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)
        søknadprosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.ingen-passer"))

        søknadprosess.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        søknadprosess.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(false)

        søknadprosess.boolsk(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd`).besvar(true)

        vernepliktMåDokumenteres(søknadprosess)

        søknadprosess.boolsk(AndreYtelser.`andre ytelser mottatt eller søkt`).besvar(false)
        søknadprosess.boolsk(AndreYtelser.`utbetaling eller økonomisk gode tidligere arbeidsgiver`).besvar(false)

        søknadprosess.boolsk(Utdanning.`tar du utdanning`).besvar(false)
        søknadprosess.boolsk(Utdanning.`avsluttet utdanning siste 6 mnd`).besvar(false)
        søknadprosess.boolsk(Utdanning.`planlegger utdanning med dagpenger`).besvar(false)

        søknadprosess.generator(Barnetillegg.`barn liste register`).besvar(1)
        søknadprosess.tekst("${Barnetillegg.`barn fornavn mellomnavn register`}.1").besvar(Tekst("test testen"))
        søknadprosess.tekst("${Barnetillegg.`barn etternavn register`}.1").besvar(Tekst("TTTT"))
        søknadprosess.dato("${Barnetillegg.`barn fødselsdato register`}.1").besvar(LocalDate.now().minusYears(10))
        søknadprosess.land("${Barnetillegg.`barn statsborgerskap register`}.1").besvar(Land("NOR"))
        // Besvares av bruker
        søknadprosess.boolsk("${Barnetillegg.`forsørger du barnet register`}.1").besvar(false)
        // Egne barn
        søknadprosess.boolsk(`egne barn`).besvar(true)
        søknadprosess.generator(Barnetillegg.`barn liste`).besvar(1)
        søknadprosess.tekst("${Barnetillegg.`barn fornavn mellomnavn`}.1").besvar(Tekst("test testen"))
        søknadprosess.tekst("${Barnetillegg.`barn etternavn`}.1").besvar(Tekst("TTTT"))
        søknadprosess.dato("${Barnetillegg.`barn fødselsdato`}.1").besvar(LocalDate.now().minusYears(10))
        søknadprosess.land("${Barnetillegg.`barn statsborgerskap`}.1").besvar(Land("NOR"))
        søknadprosess.boolsk("${Barnetillegg.`forsørger du barnet`}.1").besvar(true)

        egneBarnMåDokumenteres(søknadprosess)

        søknadprosess.boolsk(ReellArbeidssoker.`kan jobbe heltid`).besvar(true)
        søknadprosess.boolsk(ReellArbeidssoker.`kan du jobbe i hele Norge`).besvar(true)
        søknadprosess.boolsk(ReellArbeidssoker.`kan ta alle typer arbeid`).besvar(true)
        søknadprosess.boolsk(ReellArbeidssoker.`kan bytte yrke eller gå ned i lønn`).besvar(true)

        søknadprosess.boolsk(Tilleggsopplysninger.`har tilleggsopplysninger`).besvar(false)

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

    private fun vernepliktMåDokumenteres(søknadprosess: Søknadprosess) {
        MedSøknad(søknadprosess) {
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

    private fun egneBarnMåDokumenteres(søknadprosess: Søknadprosess) {
        MedSøknad(søknadprosess) {
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
