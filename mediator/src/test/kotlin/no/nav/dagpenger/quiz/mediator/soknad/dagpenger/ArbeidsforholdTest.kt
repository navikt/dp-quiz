package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Gjenopptak.`dagpenger søknadsdato`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Gjenopptak.`type arbeidstid`
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ArbeidsforholdTest {
    private val fakta = Arbeidsforhold.fakta() + Gjenopptak.fakta()
    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *fakta)
    private lateinit var søknadprosess: Søknadprosess

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(Arbeidsforhold.regeltre(søknad)) {
            Arbeidsforhold.seksjon(søknad)
        }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Arbeidsforhold.verifiserFeltsammensetting(53, 425579)
    }

    @Test
    fun `Sjekk at alle fakta er definert i en seksjon`() {
        val faktaISeksjoner = søknadprosess.flatten().map { it.id.toInt() }
        val alleFakta = Arbeidsforhold.databaseIder().toList()
        assertTrue(
            faktaISeksjoner.containsAll(
                alleFakta
            ),
            "Ikke alle faktum er ikke definert i seksjon.\nMangler seksjon for faktum id: ${
            alleFakta.toSet().minus(faktaISeksjoner.toSet())
            }"
        )
    }

    @Test
    @Disabled("MÅ FLYTTES TIL GJENOPPTAK")
    fun `Trenger ikke fylle inn arbeidsforhold når type arbeidstid er besvart med Ingen alternativer passer og det ikke er gjenopptak`() {
        /*søknadprosess.envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        søknadprosess.dato(Arbeidsforhold.`dagpenger søknadsdato`).besvar(1.januar)

        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.varierende"))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.kombinasjon"))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`)
            .besvar(Envalg("faktum.type-arbeidstid.svar.ingen-passer"))
        assertEquals(true, søknadprosess.resultat())*/
    }

    @Test
    @Disabled("MÅ FLYTTES TIL GJENOPPTAK")
    fun `arbeidsforhold ved gjenopptak av dagpenger og dens avhengigheter`() {
        /*`besvar innledende spørsmål om arbeidsforhold for gjenopptak`()
        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak endringer i arbeidsforhold siden sist`).besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak endringer i arbeidsforhold siden sist`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        `besvar spørsmål for et arbeidsforhold`()

        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak ønsker ny beregning av dagpenger`).besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak ønsker ny beregning av dagpenger`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`).besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei")))
        assertEquals(null, søknadprosess.resultat())

                assertErUbesvarte(
                    søknadprosess.boolsk(Arbeidsforhold.`gjenopptak jobbet siden sist du fikk dagpenger`),
                    søknadprosess.tekst(Arbeidsforhold.`gjenopptak årsak til stans av dagpenger`),
                    søknadprosess.dato(Arbeidsforhold.`gjenopptak søknadsdato`),
                    søknadprosess.boolsk(Arbeidsforhold.`gjenopptak endringer i arbeidsforhold siden sist`),
                    søknadprosess.boolsk(Arbeidsforhold.`gjenopptak ønsker ny beregning av dagpenger`),
                    søknadprosess.boolsk(Arbeidsforhold.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`),
                )*/
    }

    @Test
    fun `regeltre for ikke endret arbeidsforhold og dens avhengigheter`() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret"))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold kjent antall timer jobbet`}.1").besvar(true)
        søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer jobbet`}.1").besvar(40.5)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold tilleggsopplysninger`}.1")
            .besvar(Tekst(""))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.avskjediget"))
        assertEquals(null, søknadprosess.resultat())

        assertErUbesvarte(
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold kjent antall timer jobbet`}.1"),
            søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer jobbet`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold har tilleggsopplysninger`}.1"),
            søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold tilleggsopplysninger`}.1")
        )

        assertEquals(null, søknadprosess.resultat())
    }

    @Test
    fun `regeltre for avskjedighet og dens avhengigheter`() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.avskjediget"))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer før mistet jobb`}.1").besvar(true)
        søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold hva er årsak til avskjediget`}.1").besvar(Tekst("Årsak"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        assertErUbesvarte(
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer før mistet jobb`}.1"),
            søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold hva er årsak til avskjediget`}.1")
        )

        assertEquals(null, søknadprosess.resultat())
    }

    @Test
    fun `regeltre for sagt opp av arbeidsgiver og dens avhengigheter`() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer før mistet jobb`}.1").besvar(true)
        søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold vet du årsak til sagt opp av arbeidsgiver`}.1")
            .besvar(Tekst("Årsak"))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
            .besvar(true)

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.arbeidsgiver-konkurs"))

        assertErUbesvarte(
            søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer før mistet jobb`}.1"),
            søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold vet du årsak til sagt opp av arbeidsgiver`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
        )

        assertEquals(null, søknadprosess.resultat())
    }

    @Test
    fun `regeltre for arbeidsgiver er konkurs og dens avhengigheter`() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.arbeidsgiver-konkurs"))

        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold midlertidig arbeidsforhold med sluttdato`}.1")
            .besvar(true)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer før konkurs`}.1").besvar(true)
        søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)

        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1").besvar(false)
        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1").besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`}.1")
            .besvar(true)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`}.1")
            .besvar(true)
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold har søkt om lønnsgarantimidler`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.har-sokt-om-lonnsgarantimidler.svar.ja"))
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`}.1")
            .besvar(
                Envalg("faktum.arbeidsforhold.dekker-lonnsgarantiordningen-lonnskravet-ditt.svar.ja")
            )
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold utbetalt lønn etter konkurs`}.1").besvar(true)
        søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold siste dag utbetalt for konkurs`}.1").besvar(1.januar)

        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.kontrakt-utgaatt"))

        assertErUbesvarte(
            søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold midlertidig arbeidsforhold med sluttdato`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer før konkurs`}.1"),
            søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`}.1"),
            søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold har søkt om lønnsgarantimidler`}.1"),
            søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold utbetalt lønn etter konkurs`}.1"),
            søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold siste dag utbetalt for konkurs`}.1")
        )

        assertEquals(null, søknadprosess.resultat())
    }

    @Test
    fun `regeltre for utgått kontrakt og dens avhengigheter`() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.kontrakt-utgaatt"))

        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer før kontrakt utgikk`}.1")
            .besvar(false)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1")
            .besvar(false)
        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1")
            .besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ja"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ikke-svart"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.nei"))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold årsak til ikke akseptert tilbud`}.1")
            .besvar(Tekst("Årsak"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-selv"))

        assertErUbesvarte(
            søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1"),
            søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold svar på forlengelse eller annen stilling`}.1"),
            søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold årsak til ikke akseptert tilbud`}.1")
        )

        assertEquals(null, søknadprosess.resultat())
    }

    @Test
    fun `regeltre for sagt opp selv og dens avhengigheter`() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-selv"))
        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer før du sa opp`}.1").besvar(false)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold årsak til du sa opp`}.1").besvar(Tekst("Årsak"))

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))

        assertErUbesvarte(
            søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer før du sa opp`}.1"),
            søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold årsak til du sa opp`}.1")
        )

        assertEquals(null, søknadprosess.resultat())
    }

    @Test
    fun `regeltre for redusert arbeidstid og dens avhengigheter`() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold startdato arbeidsforhold`}.1").besvar(1.januar)
        søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold arbeidstid redusert fra dato`}.1").besvar(1.februar)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer før redusert arbeidstid`}.1")
            .besvar(false)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold vet du årsak til redusert arbeidstid`}.1")
            .besvar(Tekst("Årsak"))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
            .besvar(true)

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))

        assertErUbesvarte(
            søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold startdato arbeidsforhold`}.1"),
            søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold arbeidstid redusert fra dato`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer før redusert arbeidstid`}.1"),
            søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold vet du årsak til redusert arbeidstid`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
        )

        assertEquals(null, søknadprosess.resultat())
    }

    @Test
    fun `regeltre for permittert og dens avhengigheter`() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold midlertidig med kontraktfestet sluttdato`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.midlertidig-med-kontraktfestet-sluttdato.svar.ja"))
        søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold kontraktfestet sluttdato`}.1").besvar(1.februar)
        søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold midlertidig arbeidsforhold oppstartsdato`}.1")
            .besvar(1.januar)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold permittert fra fiskeri næring`}.1").besvar(true)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer før permittert`}.1").besvar(false)
        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold permittert periode`}.1").besvar(Periode(5.januar))
        søknadprosess.heltall("${Arbeidsforhold.`arbeidsforhold permittert prosent`}.1").besvar(40)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du lønnsplikt periode`}.1").besvar(true)
        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold når var lønnsplikt periode`}.1")
            .besvar(Periode(6.januar, 20.januar))

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))

        assertErUbesvarte(
            søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold midlertidig med kontraktfestet sluttdato`}.1"),
            søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold kontraktfestet sluttdato`}.1"),
            søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold midlertidig arbeidsforhold oppstartsdato`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold permittert fra fiskeri næring`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer før permittert`}.1"),
            søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold permittert periode`}.1"),
            søknadprosess.heltall("${Arbeidsforhold.`arbeidsforhold permittert prosent`}.1"),
            søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du lønnsplikt periode`}.1"),
            søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold når var lønnsplikt periode`}.1")
        )

        assertEquals(null, søknadprosess.resultat())
    }

    // DAG-341. Feil i arbeidsforhold-generator
    @Disabled
    @Test
    fun `Bug - rekkefølgen på spørsmålene blir feil`() {
        søknadprosess.generator(Arbeidsforhold.arbeidsforhold).besvar(1)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        søknadprosess.land("${Arbeidsforhold.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.arbeidsgiver-konkurs"))
        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold midlertidig arbeidsforhold med sluttdato`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.midlertidig-arbeidsforhold-med-sluttdato.svar.ja"))
        val nesteSeksjonAsJsonNode = jacksonObjectMapper().readTree(søknadprosess.nesteSeksjoner()[0].somSpørsmål())
        val arbeidsforholdGeneratorSvar = nesteSeksjonAsJsonNode["seksjoner"][0]["fakta"][2]["svar"][0]
        assertNull(arbeidsforholdGeneratorSvar.last()["svar"], "siste spørsmål i listen skal være ubesvart")
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val minimaltRegeltreForArbeidsforhold: Subsumsjon = with(søknad) {
            Gjenopptak.regeltre(this).hvisOppfylt {
                Arbeidsforhold.regeltre(this)
            }
        }
        val søknadprosessForArbeidsforhold = søknad.testSøknadprosess(minimaltRegeltreForArbeidsforhold) {
            Gjenopptak.seksjon(søknad) + Arbeidsforhold.seksjon(søknad)
        }
        val faktaForGjennopptak =
            søknadprosessForArbeidsforhold.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        // TODO: MÅ FLYTTES TIL GJENOPPTAK
        assertEquals("10001,8050,8051,8049,8052,8053,8054,8001,8002", faktaForGjennopptak)

        søknadprosessForArbeidsforhold.envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        val faktaForArbeidsforhold =
            søknadprosessForArbeidsforhold.nesteSeksjoner().first().joinToString(separator = ",\n") { it.id }
        assertEquals(forventetSpørsmålsrekkefølgeForSøker, faktaForArbeidsforhold)
    }

    private fun `besvar innledende spørsmål om arbeidsforhold for gjenopptak`() {
        /*søknadprosess.envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja")))
        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak jobbet siden sist du fikk dagpenger`).besvar(true)
        søknadprosess.tekst(Arbeidsforhold.`gjenopptak årsak til stans av dagpenger`).besvar(Tekst("Årsak"))
        søknadprosess.dato(Arbeidsforhold.`gjenopptak søknadsdato`).besvar(1.januar)*/
    }

    private fun `besvar spørsmål for et arbeidsforhold`() {
        søknadprosess.generator(Arbeidsforhold.arbeidsforhold).besvar(1)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        søknadprosess.land("${Arbeidsforhold.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret"))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold kjent antall timer jobbet`}.1").besvar(false)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(false)
    }

    private fun `besvar innledende spørsmål om arbeidsforhold`() {
        søknadprosess.envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        søknadprosess.dato(`dagpenger søknadsdato`).besvar(1.januar)
        søknadprosess.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
        søknadprosess.generator(Arbeidsforhold.arbeidsforhold).besvar(1)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        søknadprosess.land("${Arbeidsforhold.`arbeidsforhold land`}.1").besvar(Land("NOR"))
    }

    private fun `besvar spørsmål om skift, turnus og rotasjon`() {
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold skift eller turnus`}.1").besvar(true)

        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold rotasjon`}.1").besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold rotasjon`}.1").besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.heltall("${Arbeidsforhold.`arbeidsforhold arbeidsdager siste rotasjon`}.1").besvar(20)
        søknadprosess.heltall("${Arbeidsforhold.`arbeidsforhold fridager siste rotasjon`}.1").besvar(2)
    }

    private fun assertErUbesvarte(vararg fakta: Faktum<*>) =
        fakta.forEach { faktum ->
            assertFalse(faktum.erBesvart())
        }

    private val forventetSpørsmålsrekkefølgeForSøker = """
        10001,
        8050,
        8051,
        8049,
        8052,
        8053,
        8054,
        8001,
        8002
    """.trimIndent()
}
