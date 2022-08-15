package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.MedSøknad
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder
import no.nav.dagpenger.model.regel.dokumenteresAv
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.godkjentAv
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.under
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class SøkerJsonBuilderTest {
    private lateinit var prototypeSøknad: Søknad
    private lateinit var søknadprosess: Søknadprosess

    @BeforeEach
    fun setup() {
        prototypeSøknad = Søknad(
            testversjon,
            boolsk faktum "f1" id 1,
            boolsk faktum "f2" id 2 avhengerAv 1,
            boolsk faktum "f3" id 3,
            boolsk faktum "f4" id 4 avhengerAv 3,
            boolsk faktum "f5" id 5,
            heltall faktum "f6" id 6,
            boolsk faktum "f7" id 7,
            heltall faktum "f67" id 67 genererer 6 og 7 og 22,
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
                Land("SJM")
            ) id 19,
            dato faktum "f20" id 20,
            heltall faktum "f21" genererer 20 id 21,
            dokument faktum "f22" id 22 sannsynliggjør 6,
            boolsk faktum "f23" id 23 avhengerAv 22,
        )
        søknadprosess = søknadprosess(søkerSubsumsjon())
    }

    @Test
    fun `SøkerJsonBuilder returnerer besvarte fakta og neste ubesvarte faktum`() {
        SøkerJsonBuilder(søknadprosess).resultat().also {
            assertMetadata(it)
        }

        MedSøknad(søknadprosess) {
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

        søknadprosess.boolsk(1).besvar(true)
        MedSøknad(søknadprosess) {
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

        søknadprosess.boolsk(3).besvar(true)
        MedSøknad(søknadprosess) {
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

        søknadprosess.boolsk(5).besvar(true)
        MedSøknad(søknadprosess) {
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

        søknadprosess.generator(67).besvar(2)
        søknadprosess.heltall("6.1").besvar(21)
        søknadprosess.boolsk("7.1").besvar(true)
        MedSøknad(søknadprosess) {
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

        søknadprosess.heltall("6.2").besvar(19)
        MedSøknad(søknadprosess) {
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

        søknadprosess.boolsk("7.2").besvar(true)
        MedSøknad(søknadprosess) {
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

        søknadprosess.dato("8").besvar(LocalDate.now())
        søknadprosess.dato("9").besvar(LocalDate.now())

        MedSøknad(søknadprosess) {
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
                                erBesvartMed(21)
                                sannsynliggjøresAv {
                                    dokument("f22") {
                                        erIkkeBesvart()
                                    }
                                }
                            }
                            boolsk("f7") { erBesvartMed(true) }
                        }
                        svar(2) {
                            heltall("f6") { erBesvartMed(19) }
                            boolsk("f7") { erBesvartMed(true) }
                        }
                    }
                    dokument("f15") {
                        erReadOnly(false)
                    }
                }
            }
        }

        søknadprosess.dokument(15).besvar(Dokument(LocalDate.now(), "urn:nav:1234"))
        søknadprosess.generator(1718).besvar(1)

        MedSøknad(søknadprosess) {
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

        søknadprosess.envalg("17.1").besvar(Envalg("f17.envalg1"))
        søknadprosess.flervalg("18.1").besvar(Flervalg("f18.flervalg2"))

        MedSøknad(søknadprosess) {
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

        søknadprosess.land(19).besvar(Land("NOR"))

        søknadprosess.generator(21).besvar(1)
        søknadprosess.dato("20.1").besvar(LocalDate.now())

        MedSøknad(søknadprosess) { erFerdig() }
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
                fakta(sjekkAlle = false, sjekkRekkefølge = false) { boolsk("f1") { harGyldigeValg("f1.svar.ja", "f1.svar.nei") } }
            }
        }
    }

    private fun søkerSubsumsjon(): Subsumsjon {
        val alleBarnMåværeUnder18år = prototypeSøknad.heltall(6) under 18
        val deltre = "§ 1.2 har kun ikke myndige barn".deltre {
            alleBarnMåværeUnder18år.hvisIkkeOppfylt {
                prototypeSøknad.boolsk(7).utfylt()
            }
        }

        val generatorSubsumsjon67 = (prototypeSøknad.generator(67) med deltre).godkjentAv(prototypeSøknad.boolsk(23))
        val generatorSubsumsjon1718 = prototypeSøknad.generator(1718) med "Besvarte valg".deltre {
            "alle må være besvarte".alle(
                prototypeSøknad.envalg(17).utfylt(),
                prototypeSøknad.envalg(18).utfylt()
            )
        }
        val regeltre = "regel" deltre {
            "alle i søknaden skal være besvart".alle(
                "alle i seksjon 1".alle(
                    (prototypeSøknad.boolsk(1) er true).hvisIkkeOppfylt {
                        prototypeSøknad.boolsk(2).utfylt()
                    },
                    (prototypeSøknad.boolsk(3) er true).hvisIkkeOppfylt {
                        prototypeSøknad.boolsk(4).utfylt()
                    },
                    prototypeSøknad.boolsk(5).utfylt()
                ),
                "alle i seksjon 2".alle(
                    generatorSubsumsjon67
                ),
                "NAV-systemer vil svare automatisk på følgende fakta".alle(
                    prototypeSøknad.dato(8).utfylt(),
                    prototypeSøknad.dato(9).utfylt()
                ),
                "dokumentasjon".alle(
                    prototypeSøknad.boolsk(5) er true hvisOppfylt {
                        prototypeSøknad.boolsk(16) dokumenteresAv prototypeSøknad.dokument(15)
                    }
                ),
                "Generator med valg".alle(
                    generatorSubsumsjon1718
                ),
                "Land".alle(
                    prototypeSøknad.land(19).utfylt()
                ),
                prototypeSøknad.generator(21) har
                    "deltre".deltre {
                        prototypeSøknad.dato(20).utfylt()
                    }

            )
        }
        return regeltre
    }

    private fun søknadprosess(prototypeSubsumsjon: Subsumsjon): Søknadprosess {
        val seksjoner = listOf(
            Seksjon(
                "seksjon1",
                Rolle.søker,
                prototypeSøknad.boolsk(1),
                prototypeSøknad.boolsk(2),
                prototypeSøknad.boolsk(3),
                prototypeSøknad.boolsk(4),
                prototypeSøknad.boolsk(5)
            ),
            Seksjon(
                "seksjon2",
                Rolle.søker,
                prototypeSøknad.heltall(6),
                prototypeSøknad.boolsk(7),
                prototypeSøknad.heltall(67)
            ),
            Seksjon(
                "navseksjon",
                Rolle.nav,
                prototypeSøknad.dato(8),
                prototypeSøknad.dato(9)
            ),
            Seksjon(
                "dokumentasjon",
                Rolle.søker,
                prototypeSøknad.dokument(15)
            ),
            Seksjon(
                "seksjon3",
                Rolle.søker,
                prototypeSøknad.generator(1718),
                prototypeSøknad.envalg(17),
                prototypeSøknad.flervalg(18),
            ),
            Seksjon(
                "saksbehandler godkjenning",
                Rolle.saksbehandler,
                prototypeSøknad.boolsk(16)
            ),
            Seksjon(
                "Gyldige land",
                Rolle.søker,
                prototypeSøknad.land(19)
            ),
            Seksjon(
                "nav generator fakta",
                Rolle.nav,
                prototypeSøknad.generator(21),
                prototypeSøknad.dato(20)
            ),
            Seksjon(
                "godkjenner seksjon",
                Rolle.saksbehandler,
                prototypeSøknad.boolsk(23),
                prototypeSøknad.dokument(22)
            )
        )
        val prototypeFaktagrupper = Søknadprosess(
            prototypeSøknad,
            seksjoner = seksjoner.toTypedArray(),
            rootSubsumsjon = prototypeSubsumsjon
        )

        return Versjon.Bygger(
            prototypeSøknad,
            prototypeSubsumsjon,
            mapOf(Versjon.UserInterfaceType.Web to prototypeFaktagrupper)
        ).søknadprosess(testPerson, Versjon.UserInterfaceType.Web)
    }

    private fun assertMetadata(søkerJson: ObjectNode) {
        assertEquals("søker_oppgave", søkerJson["@event_name"].asText())
        Assertions.assertDoesNotThrow { søkerJson["@id"].asText().also { UUID.fromString(it) } }
        Assertions.assertDoesNotThrow { søkerJson["@opprettet"].asText().also { LocalDateTime.parse(it) } }
        Assertions.assertDoesNotThrow { søkerJson["søknad_uuid"].asText().also { UUID.fromString(it) } }
        assertEquals("12020052345", søkerJson["fødselsnummer"].asText())
    }
}
