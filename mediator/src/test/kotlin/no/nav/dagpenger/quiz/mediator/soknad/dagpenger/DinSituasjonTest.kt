package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.helpers.testFaktaversjon
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DinSituasjonTest {
    private val fakta = DinSituasjon.fakta()
    private val søknad = Fakta(testFaktaversjon(), *fakta)
    private lateinit var prosess: Prosess

    @BeforeEach
    fun setup() {
        prosess =
            søknad.testSøknadprosess(subsumsjon = DinSituasjon.regeltre(søknad)) {
                DinSituasjon.seksjon(this)
            }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        DinSituasjon.verifiserFeltsammensetting(62, 8269)
    }

    @Test
    fun `Sjekk at alle fakta er definert i en seksjon`() {
        val faktaISeksjoner = prosess.flatten().map { it.id.toInt() }
        val alleFakta = DinSituasjon.databaseIder().toList()
        assertTrue(
            faktaISeksjoner.containsAll(
                alleFakta,
            ),
            "Ikke alle faktum er ikke definert i seksjon.\nMangler seksjon for faktum id: ${
            alleFakta.toSet().minus(faktaISeksjoner.toSet())
            }",
        )
    }

    @Test
    fun `Gjenopptak søknad - har ikke jobbet siden sist eller hatt noen endring i arbeidsforhold`() {
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        prosess.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`).besvar(Tekst("Årsak"))
        prosess.dato(DinSituasjon.`gjenopptak søknadsdato`).besvar(1.januar)
        prosess.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger eller hatt endringer i arbeidsforhold`)
            .besvar(false)
        assertEquals(true, prosess.resultat())

        // Avhengigheter
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        assertEquals(null, prosess.resultat())

        assertErUbesvarte(
            prosess.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`),
            prosess.dato(DinSituasjon.`gjenopptak søknadsdato`),
            prosess.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger eller hatt endringer i arbeidsforhold`),
        )
    }

    @Test
    fun `Gjenopptak søknad - har jobbet siden sist eller hatt endring i arbeidsforhold`() {
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        prosess.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`).besvar(Tekst("Årsak"))
        prosess.dato(DinSituasjon.`gjenopptak søknadsdato`).besvar(1.januar)
        prosess.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger eller hatt endringer i arbeidsforhold`)
            .besvar(true)

        `besvar spørsmål for et arbeidsforhold`()
        assertEquals(true, prosess.resultat())

        prosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
        assertEquals(true, prosess.resultat())

        // Avhengigheter
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.vet-ikke"))
        assertErUbesvarte(prosess.envalg(DinSituasjon.`type arbeidstid`))

        assertEquals(null, prosess.resultat())

        assertErUbesvarte(
            prosess.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`),
            prosess.dato(DinSituasjon.`gjenopptak søknadsdato`),
            prosess.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger eller hatt endringer i arbeidsforhold`),
        )
    }

    @Test
    fun `Ny søknad - type arbeidstid er besvart med Ingen alternativer passer`() {
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        prosess.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)
        prosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.ingen-passer"))
        assertEquals(true, prosess.resultat())

        // Avhengigheter
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        assertEquals(null, prosess.resultat())

        assertErUbesvarte(
            prosess.dato(DinSituasjon.`dagpenger søknadsdato`),
            prosess.envalg(DinSituasjon.`type arbeidstid`),
        )
    }

    @Test
    fun `Arbeidsforhold - ikke endret`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret"))
        prosess.boolsk("${DinSituasjon.`arbeidsforhold kjent antall timer jobbet`}.1").besvar(true)
        prosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer jobbet`}.1").besvar(40.5)
        prosess.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(false)
        assertEquals(true, prosess.resultat())

        prosess.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(true)
        assertEquals(null, prosess.resultat())

        prosess.tekst("${DinSituasjon.`arbeidsforhold tilleggsopplysninger`}.1")
            .besvar(Tekst("Tilleggsopplysninger"))
        assertEquals(true, prosess.resultat())

        // Avhengigheter
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.avskjediget"))
        assertEquals(null, prosess.resultat())

        assertErUbesvarte(
            prosess.boolsk("${DinSituasjon.`arbeidsforhold kjent antall timer jobbet`}.1"),
            prosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer jobbet`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1"),
            prosess.tekst("${DinSituasjon.`arbeidsforhold tilleggsopplysninger`}.1"),
        )
    }

    @Test
    fun `Arbeidsforhold - avskjediget`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.avskjediget"))
        prosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1").besvar(true)
        prosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        prosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til avskjediget`}.1").besvar(Tekst("Årsak"))
        assertEquals(true, prosess.resultat())

        // Avhengigheter
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))
        assertEquals(null, prosess.resultat())

        assertErUbesvarte(
            prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1"),
            prosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            prosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til avskjediget`}.1"),
        )
    }

    @Test
    fun `Arbeidsforhold - sagt opp av arbeidsgiver`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        prosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1").besvar(true)
        prosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        prosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til sagt opp av arbeidsgiver`}.1")
            .besvar(Tekst("Årsak"))
        prosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
            .besvar(true)

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, prosess.resultat())

        // Avhengigheter
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.arbeidsgiver-konkurs"))
        assertEquals(null, prosess.resultat())

        assertErUbesvarte(
            prosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1"),
            prosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            prosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til sagt opp av arbeidsgiver`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1"),
        )
    }

    @Test
    fun `Arbeidsforhold - arbeidsgiver er konkurs`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.arbeidsgiver-konkurs"))

        prosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        prosess.boolsk("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold med sluttdato`}.1")
            .besvar(true)
        prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før konkurs`}.1").besvar(true)
        prosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)

        prosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1").besvar(false)
        prosess.boolsk("${DinSituasjon.`arbeidsforhold godta trekk direkte fra konkursboet`}.1").besvar(true)
        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, prosess.resultat())

        prosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1").besvar(true)
        assertEquals(null, prosess.resultat())

        prosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`}.1")
            .besvar(true)
        prosess.boolsk("${DinSituasjon.`arbeidsforhold godta trekk direkte fra konkursboet`}.1").besvar(true)
        prosess.boolsk("${DinSituasjon.`arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`}.1")
            .besvar(true)
        prosess.envalg("${DinSituasjon.`arbeidsforhold har søkt om lønnsgarantimidler`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.har-sokt-om-lonnsgarantimidler.svar.ja"))
        prosess.envalg("${DinSituasjon.`arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`}.1")
            .besvar(
                Envalg("faktum.arbeidsforhold.dekker-lonnsgarantiordningen-lonnskravet-ditt.svar.ja"),
            )
        prosess.boolsk("${DinSituasjon.`arbeidsforhold utbetalt lønn etter konkurs`}.1").besvar(true)
        prosess.dato("${DinSituasjon.`arbeidsforhold siste dag utbetalt for konkurs`}.1").besvar(1.januar)

        assertEquals(true, prosess.resultat())

        // Avhengigheter
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.kontrakt-utgaatt"))
        assertEquals(null, prosess.resultat())

        assertErUbesvarte(
            prosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold med sluttdato`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før konkurs`}.1"),
            prosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold godta trekk direkte fra konkursboet`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`}.1"),
            prosess.envalg("${DinSituasjon.`arbeidsforhold har søkt om lønnsgarantimidler`}.1"),
            prosess.envalg("${DinSituasjon.`arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold utbetalt lønn etter konkurs`}.1"),
            prosess.dato("${DinSituasjon.`arbeidsforhold siste dag utbetalt for konkurs`}.1"),
        )
    }

    @Test
    fun `Arbeidsforhold - utgått kontrakt`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.kontrakt-utgaatt"))

        prosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før kontrakt utgikk`}.1")
            .besvar(false)
        prosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1")
            .besvar(false)
        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, prosess.resultat())

        prosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1")
            .besvar(true)
        assertEquals(null, prosess.resultat())

        prosess.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ja"))
        assertEquals(true, prosess.resultat())

        prosess.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ikke-svart"))
        assertEquals(true, prosess.resultat())

        prosess.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.nei"))
        assertEquals(null, prosess.resultat())

        prosess.tekst("${DinSituasjon.`arbeidsforhold årsak til ikke akseptert tilbud`}.1")
            .besvar(Tekst("Årsak"))
        assertEquals(true, prosess.resultat())

        // Avhengigheter
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-selv"))
        assertEquals(null, prosess.resultat())

        assertErUbesvarte(
            prosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1"),
            prosess.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1"),
            prosess.tekst("${DinSituasjon.`arbeidsforhold årsak til ikke akseptert tilbud`}.1"),
        )
    }

    @Test
    fun `Arbeidsforhold - sagt opp selv`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-selv"))
        prosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før du sa opp`}.1").besvar(false)
        prosess.tekst("${DinSituasjon.`arbeidsforhold årsak til du sa opp`}.1").besvar(Tekst("Årsak"))

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, prosess.resultat())

        // Avhengigheter
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        assertEquals(null, prosess.resultat())

        assertErUbesvarte(
            prosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før du sa opp`}.1"),
            prosess.tekst("${DinSituasjon.`arbeidsforhold årsak til du sa opp`}.1"),
        )
    }

    @Test
    fun `Arbeidsforhold - redusert arbeidstid`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        prosess.dato("${DinSituasjon.`arbeidsforhold startdato arbeidsforhold`}.1").besvar(1.januar)
        prosess.dato("${DinSituasjon.`arbeidsforhold arbeidstid redusert fra dato`}.1").besvar(1.februar)
        prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før redusert arbeidstid`}.1")
            .besvar(false)
        prosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til redusert arbeidstid`}.1")
            .besvar(Tekst("Årsak"))
        prosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
            .besvar(true)

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, prosess.resultat())

        // Avhengigheter
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))
        assertEquals(null, prosess.resultat())

        assertErUbesvarte(
            prosess.dato("${DinSituasjon.`arbeidsforhold startdato arbeidsforhold`}.1"),
            prosess.dato("${DinSituasjon.`arbeidsforhold arbeidstid redusert fra dato`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før redusert arbeidstid`}.1"),
            prosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til redusert arbeidstid`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1"),
        )
    }

    @Test
    fun `Arbeidsforhold - permittert`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))
        prosess.envalg("${DinSituasjon.`arbeidsforhold midlertidig med kontraktfestet sluttdato`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.midlertidig-med-kontraktfestet-sluttdato.svar.ja"))
        prosess.dato("${DinSituasjon.`arbeidsforhold kontraktfestet sluttdato`}.1").besvar(1.februar)
        prosess.dato("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold oppstartsdato`}.1")
            .besvar(1.januar)
        prosess.boolsk("${DinSituasjon.`arbeidsforhold permittert fra fiskeri næring`}.1").besvar(false)
        prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før permittert`}.1").besvar(false)
        prosess.periode("${DinSituasjon.`arbeidsforhold permittert periode`}.1").besvar(Periode(5.januar))
        prosess.heltall("${DinSituasjon.`arbeidsforhold permittert prosent`}.1").besvar(40)

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, prosess.resultat())

        prosess.boolsk("${DinSituasjon.`arbeidsforhold permittert fra fiskeri næring`}.1").besvar(true)
        assertEquals(null, prosess.resultat())

        prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du lønnsplikt periode`}.1").besvar(true)
        prosess.periode("${DinSituasjon.`arbeidsforhold når var lønnsplikt periode`}.1")
            .besvar(Periode(6.januar, 20.januar))
        assertEquals(true, prosess.resultat())

        // Avhengigheter
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        assertEquals(null, prosess.resultat())

        assertErUbesvarte(
            prosess.envalg("${DinSituasjon.`arbeidsforhold midlertidig med kontraktfestet sluttdato`}.1"),
            prosess.dato("${DinSituasjon.`arbeidsforhold kontraktfestet sluttdato`}.1"),
            prosess.dato("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold oppstartsdato`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold permittert fra fiskeri næring`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før permittert`}.1"),
            prosess.periode("${DinSituasjon.`arbeidsforhold permittert periode`}.1"),
            prosess.heltall("${DinSituasjon.`arbeidsforhold permittert prosent`}.1"),
            prosess.boolsk("${DinSituasjon.`arbeidsforhold vet du lønnsplikt periode`}.1"),
            prosess.periode("${DinSituasjon.`arbeidsforhold når var lønnsplikt periode`}.1"),
        )
    }

    private fun `besvar spørsmål for et arbeidsforhold`() {
        prosess.generator(DinSituasjon.arbeidsforhold).besvar(1)
        prosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        prosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret"))
        prosess.boolsk("${DinSituasjon.`arbeidsforhold kjent antall timer jobbet`}.1").besvar(false)
        prosess.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(false)
    }

    private fun `besvar innledende spørsmål om situasjon og arbeidsforhold`() {
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        prosess.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)
        prosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        prosess.generator(DinSituasjon.arbeidsforhold).besvar(1)
        prosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        prosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
    }

    private fun `besvar spørsmål om skift, turnus og rotasjon`() {
        prosess.boolsk("${DinSituasjon.`arbeidsforhold skift eller turnus`}.1").besvar(true)
        prosess.boolsk("${DinSituasjon.`arbeidsforhold rotasjon`}.1").besvar(true)
        prosess.heltall("${DinSituasjon.`arbeidsforhold arbeidsdager siste rotasjon`}.1").besvar(20)
        prosess.heltall("${DinSituasjon.`arbeidsforhold fridager siste rotasjon`}.1").besvar(2)
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val fakta = Fakta(testFaktaversjon(), *DinSituasjon.fakta())
        val søknadprosess =
            fakta.testSøknadprosess(
                subsumsjon = DinSituasjon.regeltre(fakta),
            ) {
                DinSituasjon.seksjon(this)
            }
        val faktaFraDinSituasjon = søknadprosess.nesteSeksjoner().first().joinToString(separator = ",\n") { it.id }
        assertEquals(forventetSpørsmålsrekkefølgeForSøker, faktaFraDinSituasjon)
    }

    private fun assertErUbesvarte(vararg fakta: Faktum<*>) =
        fakta.forEach { faktum ->
            assertFalse(faktum.erBesvart())
        }

    private val forventetSpørsmålsrekkefølgeForSøker =
        """
101,
103,
104,
102,
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
