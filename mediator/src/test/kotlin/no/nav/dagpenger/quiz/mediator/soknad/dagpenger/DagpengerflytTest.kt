package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.MedSøknad
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Barnetillegg.`egne barn`
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertTrue

class DagpengerflytTest {
    private lateinit var faktagrupper: Faktagrupper

    init {
        Dagpenger.registrer { prototypeSøknad ->
            faktagrupper = Versjon.id(Dagpenger.VERSJON_ID)
                .søknadprosess(prototypeSøknad)
        }
    }

    @Test
    fun `dagpenger flyt - letteste vei til ferdig`() {
        faktagrupper.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))

        faktagrupper.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        faktagrupper.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)
        faktagrupper.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.ingen-passer"))

        faktagrupper.boolsk(EgenNæring.`driver du egen næring`).besvar(false)
        faktagrupper.boolsk(EgenNæring.`driver du eget gårdsbruk`).besvar(false)

        faktagrupper.boolsk(Verneplikt.`avtjent militær sivilforsvar tjeneste siste 12 mnd`).besvar(true)

        vernepliktMåDokumenteres(faktagrupper)

        faktagrupper.boolsk(AndreYtelser.`andre ytelser mottatt eller søkt`).besvar(false)
        faktagrupper.boolsk(AndreYtelser.`utbetaling eller økonomisk gode tidligere arbeidsgiver`).besvar(false)

        faktagrupper.boolsk(Utdanning.`tar du utdanning`).besvar(false)
        faktagrupper.boolsk(Utdanning.`avsluttet utdanning siste 6 mnd`).besvar(false)
        faktagrupper.boolsk(Utdanning.`planlegger utdanning med dagpenger`).besvar(false)

        faktagrupper.generator(Barnetillegg.`barn liste register`).besvar(1)
        faktagrupper.tekst("${Barnetillegg.`barn fornavn mellomnavn register`}.1").besvar(Tekst("test testen"))
        faktagrupper.tekst("${Barnetillegg.`barn etternavn register`}.1").besvar(Tekst("TTTT"))
        faktagrupper.dato("${Barnetillegg.`barn fødselsdato register`}.1").besvar(LocalDate.now().minusYears(10))
        faktagrupper.land("${Barnetillegg.`barn statsborgerskap register`}.1").besvar(Land("NOR"))
        // Besvares av bruker
        faktagrupper.boolsk("${Barnetillegg.`forsørger du barnet register`}.1").besvar(false)
        // Egne barn
        faktagrupper.boolsk(`egne barn`).besvar(true)
        faktagrupper.generator(Barnetillegg.`barn liste`).besvar(1)
        faktagrupper.tekst("${Barnetillegg.`barn fornavn mellomnavn`}.1").besvar(Tekst("test testen"))
        faktagrupper.tekst("${Barnetillegg.`barn etternavn`}.1").besvar(Tekst("TTTT"))
        faktagrupper.dato("${Barnetillegg.`barn fødselsdato`}.1").besvar(LocalDate.now().minusYears(10))
        faktagrupper.land("${Barnetillegg.`barn statsborgerskap`}.1").besvar(Land("NOR"))
        faktagrupper.boolsk("${Barnetillegg.`forsørger du barnet`}.1").besvar(true)

        egneBarnMåDokumenteres(faktagrupper)

        faktagrupper.boolsk(ReellArbeidssoker.`kan jobbe heltid`).besvar(true)
        faktagrupper.boolsk(ReellArbeidssoker.`kan du jobbe i hele Norge`).besvar(true)
        faktagrupper.boolsk(ReellArbeidssoker.`kan ta alle typer arbeid`).besvar(true)
        faktagrupper.boolsk(ReellArbeidssoker.`kan bytte yrke eller gå ned i lønn`).besvar(true)

        faktagrupper.boolsk(Tilleggsopplysninger.`har tilleggsopplysninger`).besvar(false)

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

    private fun vernepliktMåDokumenteres(faktagrupper: Faktagrupper) {
        MedSøknad(faktagrupper) {
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

    private fun egneBarnMåDokumenteres(faktagrupper: Faktagrupper) {
        MedSøknad(faktagrupper) {
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
