package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.MedSøknad
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.EøsArbeidsforhold.`eøs arbeidsforhold arbeidsgivernavn`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.EøsArbeidsforhold.`eøs arbeidsforhold land`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.EøsArbeidsforhold.`eøs arbeidsforhold personnummer`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.EøsArbeidsforhold.`eøs arbeidsforhold varighet`
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class EøsArbeidsforholdTest {
    private val fakta = EøsArbeidsforhold.fakta() + DinSituasjon.fakta()

    private val søknad = Fakta(Faktaversjon(Prosessfakta.Dagpenger, versjon = -1), *fakta)
    private lateinit var prosess: Prosess

    @BeforeEach
    fun setup() {
        prosess = søknad.testSøknadprosess(
            EøsArbeidsforhold.regeltre(søknad),
        ) {
            EøsArbeidsforhold.seksjon(søknad)
        }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        EøsArbeidsforhold.verifiserFeltsammensetting(6, 54021)
    }

    @Test
    fun `Trenger ikke svare på spørsmål om EØS dersom man ikke har vært i jobb eller ikke har hatt endringer i arbeidsforhold`() {
        `ny søknad men har ikke vært i jobb`()
        assertEquals(true, prosess.erFerdig())

        `gjenopptak men har ikke jobbet siden sist eller hatt endringer i arbeidsforhold`()
        assertEquals(true, prosess.erFerdig())
    }

    @Test
    fun `Har arbeidet innenfor EØS de siste 36 mnd`() {
        `har vært i jobb`()
        assertEquals(null, prosess.resultat())

        prosess.boolsk(EøsArbeidsforhold.`eøs arbeid siste 36 mnd`).besvar(false)
        assertEquals(true, prosess.resultat())
        prosess.boolsk(EøsArbeidsforhold.`eøs arbeid siste 36 mnd`).besvar(true)
        assertEquals(null, prosess.resultat())

        prosess.generator(EøsArbeidsforhold.`eøs arbeidsforhold`).besvar(2)
        prosess.tekst("$`eøs arbeidsforhold arbeidsgivernavn`.1").besvar(Tekst("CERN"))
        prosess.land("$`eøs arbeidsforhold land`.1").besvar(Land("CHE"))
        prosess.tekst("$`eøs arbeidsforhold personnummer`.1").besvar(Tekst("12345678901"))
        prosess.periode("$`eøs arbeidsforhold varighet`.1").besvar(
            Periode(
                fom = LocalDate.now().minusDays(50),
                tom = LocalDate.now(),
            ),
        )

        assertEquals(null, prosess.resultat())

        prosess.tekst("$`eøs arbeidsforhold arbeidsgivernavn`.2").besvar(Tekst("CERN"))
        prosess.land("$`eøs arbeidsforhold land`.2").besvar(Land("CHE"))
        prosess.tekst("$`eøs arbeidsforhold personnummer`.2").besvar(Tekst("12345678901"))
        prosess.periode("$`eøs arbeidsforhold varighet`.2").besvar(
            Periode(
                fom = LocalDate.now().minusDays(50),
                tom = LocalDate.now(),
            ),
        )
        assertEquals(true, prosess.resultat())
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val minimaltRegeltreForEøsArbeidsforhold: Subsumsjon = with(søknad) {
            DinSituasjon.regeltre(this).hvisOppfylt {
                EøsArbeidsforhold.regeltre(this)
            }
        }

        val søknadprosessForEøsArbeidsforhold = søknad.testSøknadprosess(minimaltRegeltreForEøsArbeidsforhold) {
            EøsArbeidsforhold.seksjon(søknad) + DinSituasjon.seksjon(søknad)
        }

        val faktaForDinSituasjon = søknadprosessForEøsArbeidsforhold.nesteSeksjoner().first().joinToString(separator = ",\n") { it.id }
        assertEquals(forventetSpørsmålsrekkefølgeForDinSituasjon, faktaForDinSituasjon)

        `besvar DinSituasjon`(søknadprosessForEøsArbeidsforhold)

        val faktaForEøsArbeidsforhold = søknadprosessForEøsArbeidsforhold.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals("9001,9002,9003,9004,9005,9006", faktaForEøsArbeidsforhold)
    }

    @Test
    fun `For et EØS-land skal det være en egen gruppe for kun EØS-land`() {
        val minimaltRegeltreForEøsArbeidsforhold: Subsumsjon = with(søknad) {
            DinSituasjon.regeltre(this).hvisOppfylt {
                EøsArbeidsforhold.regeltre(this)
            }
        }

        val søknadprosessForEøsArbeidsforhold = søknad.testSøknadprosess(minimaltRegeltreForEøsArbeidsforhold) {
            EøsArbeidsforhold.seksjon(søknad) + DinSituasjon.seksjon(søknad)
        }

        `besvar DinSituasjon`(søknadprosessForEøsArbeidsforhold)
        søknadprosessForEøsArbeidsforhold.boolsk(EøsArbeidsforhold.`eøs arbeid siste 36 mnd`).besvar(true)
        søknadprosessForEøsArbeidsforhold.generator(EøsArbeidsforhold.`eøs arbeidsforhold`).besvar(1)
        søknadprosessForEøsArbeidsforhold.land("$`eøs arbeidsforhold land`.1").besvar(Land("CHE"))

        MedSøknad(søknadprosessForEøsArbeidsforhold) {
            harAntallSeksjoner(2)
            seksjon("eos-arbeidsforhold") {
                fakta(sjekkAlle = false, sjekkRekkefølge = false) {
                    generator("faktum.eos-arbeidsforhold") {
                        svar(1) {
                            land("faktum.eos-arbeidsforhold.land") {
                                grupper(sjekkAlle = false) {
                                    gruppe("faktum.eos-arbeidsforhold.land.gruppe.eøs") {
                                        eøsEllerSveits().forEach { eøsLand ->
                                            harLand(eøsLand.alpha3Code)
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

    private fun `gjenopptak men har ikke jobbet siden sist eller hatt endringer i arbeidsforhold`() {
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        prosess.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger eller hatt endringer i arbeidsforhold`).besvar(false)
    }

    private fun `ny søknad men har ikke vært i jobb`() {
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        prosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.ingen-passer"))
    }

    private fun `har vært i jobb`() {
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        prosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
    }

    private fun `besvar DinSituasjon`(prosess: Prosess) {
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        prosess.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)

        prosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
        prosess.generator(DinSituasjon.arbeidsforhold).besvar(1)
        prosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        prosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret"))
        prosess.boolsk("${DinSituasjon.`arbeidsforhold kjent antall timer jobbet`}.1").besvar(false)
        prosess.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(false)
    }

    private val forventetSpørsmålsrekkefølgeForDinSituasjon = """
101,
103,
104,
102,
105,
106,
108,
107,
109,
110,
111,
112,
113,
114,
115,
116,
117,
118,
119,
120,
121,
122,
123,
124,
125,
126,
127,
128,
129,
130,
131,
132,
133,
134,
135,
136,
137,
138,
139,
140,
164,
141,
142,
143,
144,
145,
146,
147,
148,
149,
150,
151,
152,
153,
154,
155,
156,
157,
158,
159,
160,
161,
162,
163
    """.trimIndent()
}
