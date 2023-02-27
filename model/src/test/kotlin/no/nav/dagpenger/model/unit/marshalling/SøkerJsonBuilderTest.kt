package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.MedSøknad
import no.nav.dagpenger.model.helpers.TestProsesser
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder
import no.nav.dagpenger.model.regel.dokumenteresAv
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.under
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Prosessversjon
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.sannsynliggjøresAv
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class SøkerJsonBuilderTest {
    private lateinit var prototypeFakta: Fakta
    private lateinit var prosess: Prosess

    @BeforeEach
    fun setup() {
        prototypeFakta = Fakta(
            testversjon,
            boolsk faktum "f1" id 1,
            boolsk faktum "f2" id 2 avhengerAv 1,
            boolsk faktum "f3" id 3,
            boolsk faktum "f4" id 4 avhengerAv 3,
            boolsk faktum "f5" id 5,
            heltall faktum "f6" id 6,
            tekst faktum "f7" id 7,
            heltall faktum "f67" id 67 navngittAv 7 genererer 6 og 7 og 22,
            dato faktum "f8" id 8,
            dato faktum "f9" id 9,
            maks dato "f10" av 8 og 9 id 10,
            boolsk faktum "f11" id 11 avhengerAv 10,
            boolsk faktum "f12" id 12 avhengerAv 67,
            heltall faktum "f1314" id 1314 genererer 13 og 14,
            boolsk faktum "f13" id 13,
            boolsk faktum "f14" id 14,
            dokument faktum "f15" id 15 avhengerAv 5 og 8 og 67,
            boolsk faktum "f16" id 16 avhengerAv 15,
            envalg faktum "f17" id 17 med "envalg1" med "envalg2",
            flervalg faktum "f18" id 18 med "flervalg1" med "flervalg2",
            heltall faktum "f1718" id 1718 genererer 17 og 18,
            land faktum "f19" gruppe "eøs" med listOf(Land("SWE")) gruppe "norge-jan-mayen" med listOf(
                Land("NOR"),
                Land("SJM"),
            ) id 19,
            dato faktum "f20" id 20,
            heltall faktum "f21" genererer 20 id 21,
            dokument faktum "f22" id 22 avhengerAv 6,
            boolsk faktum "f23" id 23 avhengerAv 22,
            boolsk faktum "f24" id 24,
            dokument faktum "f25" id 25 avhengerAv 24,
            boolsk faktum "f26" id 26 avhengerAv 25,
        )
        prosess = søknadprosess(søkerSubsumsjon())
    }

    @Test
    fun `SøkerJsonBuilder returnerer besvarte fakta og neste ubesvarte faktum`() {
        SøkerJsonBuilder(prosess).resultat().also {
            assertMetadata(it)
        }

        MedSøknad(prosess) {
            harAntallSeksjoner(1)
            seksjon("seksjon1") {
                fakta {
                    boolsk("f1") {
                        harRoller("søker")
                        erIkkeBesvart()
                    }
                }
            }
        }

        prosess.boolsk(1).besvar(true)
        MedSøknad(prosess) {
            harAntallSeksjoner(1)
            seksjon("seksjon1") {
                fakta {
                    harAntallFakta(2)
                    alle { harRoller("søker") }
                    boolsk("f1") { erBesvartMed(true) }
                    boolsk("f3") { erIkkeBesvart() }
                }
            }
        }

        prosess.boolsk(3).besvar(true)
        MedSøknad(prosess) {
            harAntallSeksjoner(1)
            seksjon("seksjon1") {
                fakta {
                    alle { harRoller("søker") }
                    boolsk("f1") { erBesvartMed(true) }
                    boolsk("f3") { erBesvartMed(true) }
                    boolsk("f5") { erIkkeBesvart() }
                }
            }
        }

        prosess.boolsk(5).besvar(true)
        MedSøknad(prosess) {
            harAntallSeksjoner(2)
            seksjon("seksjon1") {
                erFerdig()
                fakta {
                    boolsk("f1") { erBesvartMed(true) }
                    boolsk("f3") { erBesvartMed(true) }
                    boolsk("f5") { erBesvartMed(true) }
                }
            }
            seksjon("seksjon2") {
                fakta {
                    generator("f67") {
                        erIkkeBesvart()
                    }
                }
            }
        }

        prosess.generator(67).besvar(2)
        prosess.heltall("6.1").besvar(11)
        prosess.tekst("7.1").besvar(Tekst("Hei"))
        MedSøknad(prosess) {
            harAntallSeksjoner(2)
            seksjon("seksjon2") {
                fakta {
                    alle { harRoller("søker") }
                    generator("f67") {
                        erBesvartMed(2)
                        svar(1) {
                            harAntallFakta(2)
                            harAntallBesvarte(2)
                        }
                        svar(2) {
                            // Bare 6.2 skal bli med fordi regeltreet sier at 6 er neste faktum. Ikke 7.2
                            harAntallFakta(1)
                            harAntallBesvarte(0)
                        }
                    }
                }
            }
        }

        prosess.heltall("6.2").besvar(19)
        MedSøknad(prosess) {
            harAntallSeksjoner(2)
            seksjon("seksjon2") {
                fakta {
                    harAntallBesvarte(1)
                    alle { harRoller("søker") }
                    generator("f67") {
                        erBesvartMed(2)
                        svar(1) { harAntallBesvarte(2) }
                        svar(2) { harAntallBesvarte(1) }
                    }
                }
            }
        }

        prosess.tekst("7.2").besvar(Tekst("Hadet"))
        MedSøknad(prosess) {
            harAntallSeksjoner(2)
            seksjon("seksjon2") { erFerdig() }
            seksjon("seksjon2") {
                fakta {
                    harAntallFakta(1)
                    generator("f67") {
                        erBesvartMed(2)
                        svar(1) { harAntallBesvarte(2) }
                        svar(2) { harAntallBesvarte(2) }
                    }
                }
            }
        }

        prosess.dato("8").besvar(LocalDate.now())
        prosess.dato("9").besvar(LocalDate.now())

        MedSøknad(prosess) {
            seksjon("dokumentasjon") {
                fakta {
                    harAntallFakta(4)
                    harAntallReadOnly(3)
                    boolsk("f5") {
                        erReadOnly()
                        erBesvartMed(true)
                    }
                    dato("f8") {
                        erReadOnly()
                        erBesvart()
                    }
                    generator("f67") {
                        erReadOnly()
                        alle { erReadOnly() }
                        erBesvartMed(2)
                        svar(1) {
                            heltall("f6") {
                                erBesvartMed(11)
                                sannsynliggjøresAv {
                                    dokument("f22") {
                                        erIkkeBesvart()
                                    }
                                }
                            }
                            tekst("f7") { erBesvartMed("Hei") }
                        }
                        svar(2) {
                            heltall("f6") { erBesvartMed(19) }
                            tekst("f7") { erBesvartMed("Hadet") }
                        }
                    }
                    dokument("f15") {
                        erReadOnly(false)
                    }
                }
            }
        }

        prosess.dokument(15).besvar(Dokument(LocalDate.now(), "urn:nav:1234"))
        prosess.generator(1718).besvar(1)

        MedSøknad(prosess) {
            harAntallSeksjoner(4)
            seksjon("seksjon3") {
                fakta {
                    harAntallFakta(1)
                    harAntallBesvarte(1)
                    generator("f1718") {
                        erBesvartMed(1)
                        harRoller("søker")

                        svar(1) {
                            envalg("f17") {
                                erIkkeBesvart()
                                harGyldigeValg("f17.envalg1", "f17.envalg2")
                            }
                        }

                        templates {
                            alle { harRoller("søker") }
                            envalg("f17") {
                                harGyldigeValg("f17.envalg1", "f17.envalg2")
                            }
                            flervalg("f18") {
                                harGyldigeValg("f18.flervalg1", "f18.flervalg2")
                            }
                        }
                    }
                }
            }
        }

        prosess.envalg("17.1").besvar(Envalg("f17.envalg1"))
        prosess.flervalg("18.1").besvar(Flervalg("f18.flervalg2"))

        MedSøknad(prosess) {
            harAntallSeksjoner(5)
            seksjon("Gyldige land") {
                fakta {
                    land("f19") {
                        harLand("NOR", "SWE")
                        grupper(sjekkAlle = true) {
                            gruppe("f19.gruppe.eøs") {
                                harLand("SWE")
                            }
                            gruppe("f19.gruppe.norge-jan-mayen") {
                                harLand("NOR", "SJM")
                            }
                        }
                    }
                }
            }
        }

        prosess.land(19).besvar(Land("NOR"))

        prosess.generator(21).besvar(1)
        prosess.dato("20.1").besvar(LocalDate.now())
        prosess.boolsk(24).besvar(true)

        MedSøknad(prosess) {
            seksjon("grunnleggende med dokumentasjon") {
                fakta {
                    boolsk("f24") {
                        erBesvart()
                        sannsynliggjøresAv {
                            dokument("f25") {
                                erIkkeBesvart()
                            }
                        }
                    }
                }
            }
        }

        prosess.dokument(24).besvar(Dokument(LocalDateTime.now(), "urn:nav:12345"))

        MedSøknad(prosess) { erFerdig() }
    }

    @Test
    fun `Boolske fakta skal ha en beskrivendeId for hvert av de to gyldige valgene`() {
        val regel = søkerSubsumsjon()
        val søknadprosess = søknadprosess(regel)
        // Sjekk uten svar
        MedSøknad(søknadprosess) {
            seksjon("seksjon1") {
                fakta { boolsk("f1") { harGyldigeValg("f1.svar.ja", "f1.svar.nei") } }
            }
        }
        // Sjekk med svar
        søknadprosess.boolsk(1).besvar(true)
        MedSøknad(søknadprosess) {
            seksjon("seksjon1") {
                fakta(sjekkAlle = false, sjekkRekkefølge = false) {
                    boolsk("f1") {
                        harGyldigeValg(
                            "f1.svar.ja",
                            "f1.svar.nei",
                        )
                    }
                }
            }
        }
    }

    private fun søkerSubsumsjon(): Subsumsjon {
        val alleBarnMåværeUnder18år =
            (prototypeFakta.heltall(6) under 18).sannsynliggjøresAv(prototypeFakta.dokument(22))
        val deltre = "§ 1.2 har kun ikke myndige barn".deltre {
            alleBarnMåværeUnder18år.hvisIkkeOppfylt {
                prototypeFakta.boolsk(7).utfylt()
            }
        }
        val generatorSubsumsjon67 = (prototypeFakta.generator(67) med deltre).godkjentAv(prototypeFakta.boolsk(23))
        val generatorSubsumsjon1718 = prototypeFakta.generator(1718) med "Besvarte valg".deltre {
            "alle må være besvarte".alle(
                prototypeFakta.envalg(17).utfylt(),
                prototypeFakta.envalg(18).utfylt(),
            )
        }
        val regeltre = "regel" deltre {
            "alle i søknaden skal være besvart".alle(
                "alle i seksjon 1".alle(
                    (prototypeFakta.boolsk(1) er true).hvisIkkeOppfylt {
                        prototypeFakta.boolsk(2).utfylt()
                    },
                    (prototypeFakta.boolsk(3) er true).hvisIkkeOppfylt {
                        prototypeFakta.boolsk(4).utfylt()
                    },
                    prototypeFakta.boolsk(5).utfylt(),
                ),
                "alle i seksjon 2".alle(
                    generatorSubsumsjon67,
                ),
                "NAV-systemer vil svare automatisk på følgende fakta".alle(
                    prototypeFakta.dato(8).utfylt(),
                    prototypeFakta.dato(9).utfylt(),
                ),
                "dokumentasjon".alle(
                    prototypeFakta.boolsk(5) er true hvisOppfylt {
                        prototypeFakta.boolsk(16) dokumenteresAv prototypeFakta.dokument(15)
                    },
                ),
                "Generator med valg".alle(
                    generatorSubsumsjon1718,
                ),
                "Land".alle(
                    prototypeFakta.land(19).utfylt(),
                ),
                prototypeFakta.generator(21) har
                    "deltre".deltre {
                        prototypeFakta.dato(20).utfylt()
                    },
                "Grunnleggende med dokumentasjon".minstEnAv(
                    (prototypeFakta.boolsk(24) er true).sannsynliggjøresAv(prototypeFakta.dokument(25))
                        .godkjentAv(prototypeFakta.boolsk(26)),
                    prototypeFakta.boolsk(24) er false,
                ),
            )
        }
        return regeltre
    }

    private fun søknadprosess(prototypeSubsumsjon: Subsumsjon): Prosess {
        val seksjoner = listOf(
            Seksjon(
                "seksjon1",
                Rolle.søker,
                prototypeFakta.boolsk(1),
                prototypeFakta.boolsk(2),
                prototypeFakta.boolsk(3),
                prototypeFakta.boolsk(4),
                prototypeFakta.boolsk(5),
            ),
            Seksjon(
                "seksjon2",
                Rolle.søker,
                prototypeFakta.heltall(6),
                prototypeFakta.boolsk(7),
                prototypeFakta.heltall(67),
            ),
            Seksjon(
                "navseksjon",
                Rolle.nav,
                prototypeFakta.dato(8),
                prototypeFakta.dato(9),
            ),
            Seksjon(
                "dokumentasjon",
                Rolle.søker,
                prototypeFakta.dokument(15),
            ),
            Seksjon(
                "seksjon3",
                Rolle.søker,
                prototypeFakta.generator(1718),
                prototypeFakta.envalg(17),
                prototypeFakta.flervalg(18),
            ),
            Seksjon(
                "saksbehandler godkjenning",
                Rolle.saksbehandler,
                prototypeFakta.boolsk(16),
            ),
            Seksjon(
                "Gyldige land",
                Rolle.søker,
                prototypeFakta.land(19),
            ),
            Seksjon(
                "nav generator fakta",
                Rolle.nav,
                prototypeFakta.generator(21),
                prototypeFakta.dato(20),
            ),
            Seksjon(
                "grunnleggende med dokumentasjon",
                Rolle.søker,
                prototypeFakta.boolsk(24),
                prototypeFakta.dokument(25),
            ),
            Seksjon(
                "godkjenner seksjon",
                Rolle.saksbehandler,
                prototypeFakta.boolsk(23),
                prototypeFakta.boolsk(26),
                prototypeFakta.dokument(22),
            ),
        )
        val prototypeProsess = Prosess(
            TestProsesser.Test,
            prototypeFakta,
            seksjoner = seksjoner.toTypedArray(),
            rootSubsumsjon = prototypeSubsumsjon,
        )

        return Prosessversjon.Bygger(
            prototypeFakta,
            prototypeSubsumsjon,
            prototypeProsess,
        ).utredningsprosess(testPerson)
    }

    private fun assertMetadata(søkerJson: ObjectNode) {
        assertEquals("søker_oppgave", søkerJson["@event_name"].asText())
        Assertions.assertDoesNotThrow { søkerJson["@id"].asText().also { UUID.fromString(it) } }
        Assertions.assertDoesNotThrow { søkerJson["@opprettet"].asText().also { LocalDateTime.parse(it) } }
        Assertions.assertDoesNotThrow { søkerJson["søknad_uuid"].asText().also { UUID.fromString(it) } }
        assertEquals("12020052345", søkerJson["fødselsnummer"].asText())
    }
}
