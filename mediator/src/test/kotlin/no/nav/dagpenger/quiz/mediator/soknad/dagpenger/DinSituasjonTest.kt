package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

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
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DinSituasjonTest {
    private val fakta = DinSituasjon.fakta()
    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *fakta)
    private lateinit var søknadprosess: Søknadprosess

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(DinSituasjon.regeltre(søknad)) {
            DinSituasjon.seksjon(this)
        }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        DinSituasjon.verifiserFeltsammensetting(62, 8153)
    }

    @Test
    fun `Sjekk at alle fakta er definert i en seksjon`() {
        val faktaISeksjoner = søknadprosess.flatten().map { it.id.toInt() }
        val alleFakta = DinSituasjon.databaseIder().toList()
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
    fun `Gjenopptak søknad - har ikke jobbet siden sist`() {
        søknadprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        søknadprosess.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`).besvar(Tekst("Årsak"))
        søknadprosess.dato(DinSituasjon.`gjenopptak søknadsdato`).besvar(1.januar)
        søknadprosess.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger`).besvar(false)
        assertEquals(true, søknadprosess.resultat())

        // Avhengigheter
        søknadprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        assertEquals(null, søknadprosess.resultat())

        assertErUbesvarte(
            søknadprosess.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`),
            søknadprosess.dato(DinSituasjon.`gjenopptak søknadsdato`),
            søknadprosess.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger`)
        )
    }

    @Test
    fun `Gjenopptak søknad - har jobbet siden sist`() {
        søknadprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        søknadprosess.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`).besvar(Tekst("Årsak"))
        søknadprosess.dato(DinSituasjon.`gjenopptak søknadsdato`).besvar(1.januar)
        søknadprosess.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger`).besvar(true)

        søknadprosess.boolsk(DinSituasjon.`gjenopptak endringer i arbeidsforhold siden sist`).besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk(DinSituasjon.`gjenopptak endringer i arbeidsforhold siden sist`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(DinSituasjon.`gjenopptak ønsker ny beregning av dagpenger`).besvar(true)
        søknadprosess.boolsk(DinSituasjon.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`).besvar(true)
        søknadprosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.ingen-passer"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
        assertEquals(null, søknadprosess.resultat())

        `besvar spørsmål for et arbeidsforhold`()
        assertEquals(true, søknadprosess.resultat())

        // Avhengigheter
        søknadprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.vet-ikke"))
        assertEquals(null, søknadprosess.resultat())

        assertErUbesvarte(
            søknadprosess.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`),
            søknadprosess.dato(DinSituasjon.`gjenopptak søknadsdato`),
            søknadprosess.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger`),
            søknadprosess.boolsk(DinSituasjon.`gjenopptak endringer i arbeidsforhold siden sist`),
            søknadprosess.boolsk(DinSituasjon.`gjenopptak ønsker ny beregning av dagpenger`),
            søknadprosess.boolsk(DinSituasjon.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`),
            søknadprosess.envalg(DinSituasjon.`type arbeidstid`)
        )
    }

    @Test
    fun `Ny søknad - type arbeidstid er besvart med Ingen alternativer passer`() {
        søknadprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        søknadprosess.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)
        søknadprosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.ingen-passer"))
        assertEquals(true, søknadprosess.resultat())

        // Avhengigheter
        søknadprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        assertEquals(null, søknadprosess.resultat())

        assertErUbesvarte(
            søknadprosess.dato(DinSituasjon.`dagpenger søknadsdato`),
            søknadprosess.envalg(DinSituasjon.`type arbeidstid`)
        )
    }

    @Test
    fun `Arbeidsforhold - ikke endret`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret"))
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold kjent antall timer jobbet`}.1").besvar(true)
        søknadprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer jobbet`}.1").besvar(40.5)
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst("${DinSituasjon.`arbeidsforhold tilleggsopplysninger`}.1")
            .besvar(Tekst("Tilleggsopplysninger"))
        assertEquals(true, søknadprosess.resultat())

        // Avhengigheter
        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.avskjediget"))
        assertEquals(null, søknadprosess.resultat())

        assertErUbesvarte(
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold kjent antall timer jobbet`}.1"),
            søknadprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer jobbet`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1"),
            søknadprosess.tekst("${DinSituasjon.`arbeidsforhold tilleggsopplysninger`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - avskjediget`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.avskjediget"))
        søknadprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1").besvar(true)
        søknadprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        søknadprosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til avskjediget`}.1").besvar(Tekst("Årsak"))
        assertEquals(true, søknadprosess.resultat())

        // Avhengigheter
        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))
        assertEquals(null, søknadprosess.resultat())

        assertErUbesvarte(
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1"),
            søknadprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            søknadprosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til avskjediget`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - sagt opp av arbeidsgiver`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        søknadprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1").besvar(true)
        søknadprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        søknadprosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til sagt opp av arbeidsgiver`}.1")
            .besvar(Tekst("Årsak"))
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
            .besvar(true)

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        // Avhengigheter
        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.arbeidsgiver-konkurs"))
        assertEquals(null, søknadprosess.resultat())

        assertErUbesvarte(
            søknadprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1"),
            søknadprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            søknadprosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til sagt opp av arbeidsgiver`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - arbeidsgiver er konkurs`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.arbeidsgiver-konkurs"))

        søknadprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold med sluttdato`}.1")
            .besvar(true)
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før konkurs`}.1").besvar(true)
        søknadprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)

        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1").besvar(false)
        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1").besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`}.1")
            .besvar(true)
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`}.1")
            .besvar(true)
        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold har søkt om lønnsgarantimidler`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.har-sokt-om-lonnsgarantimidler.svar.ja"))
        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`}.1")
            .besvar(
                Envalg("faktum.arbeidsforhold.dekker-lonnsgarantiordningen-lonnskravet-ditt.svar.ja")
            )
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold utbetalt lønn etter konkurs`}.1").besvar(true)
        søknadprosess.dato("${DinSituasjon.`arbeidsforhold siste dag utbetalt for konkurs`}.1").besvar(1.januar)

        assertEquals(true, søknadprosess.resultat())

        // Avhengigheter
        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.kontrakt-utgaatt"))
        assertEquals(null, søknadprosess.resultat())

        assertErUbesvarte(
            søknadprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold med sluttdato`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før konkurs`}.1"),
            søknadprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`}.1"),
            søknadprosess.envalg("${DinSituasjon.`arbeidsforhold har søkt om lønnsgarantimidler`}.1"),
            søknadprosess.envalg("${DinSituasjon.`arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold utbetalt lønn etter konkurs`}.1"),
            søknadprosess.dato("${DinSituasjon.`arbeidsforhold siste dag utbetalt for konkurs`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - utgått kontrakt`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.kontrakt-utgaatt"))

        søknadprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før kontrakt utgikk`}.1")
            .besvar(false)
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1")
            .besvar(false)
        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1")
            .besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ja"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ikke-svart"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.nei"))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst("${DinSituasjon.`arbeidsforhold årsak til ikke akseptert tilbud`}.1")
            .besvar(Tekst("Årsak"))
        assertEquals(true, søknadprosess.resultat())

        // Avhengigheter
        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-selv"))
        assertEquals(null, søknadprosess.resultat())

        assertErUbesvarte(
            søknadprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1"),
            søknadprosess.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1"),
            søknadprosess.tekst("${DinSituasjon.`arbeidsforhold årsak til ikke akseptert tilbud`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - sagt opp selv`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-selv"))
        søknadprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før du sa opp`}.1").besvar(false)
        søknadprosess.tekst("${DinSituasjon.`arbeidsforhold årsak til du sa opp`}.1").besvar(Tekst("Årsak"))

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        // Avhengigheter
        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        assertEquals(null, søknadprosess.resultat())

        assertErUbesvarte(
            søknadprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før du sa opp`}.1"),
            søknadprosess.tekst("${DinSituasjon.`arbeidsforhold årsak til du sa opp`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - redusert arbeidstid`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        søknadprosess.dato("${DinSituasjon.`arbeidsforhold startdato arbeidsforhold`}.1").besvar(1.januar)
        søknadprosess.dato("${DinSituasjon.`arbeidsforhold arbeidstid redusert fra dato`}.1").besvar(1.februar)
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før redusert arbeidstid`}.1")
            .besvar(false)
        søknadprosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til redusert arbeidstid`}.1")
            .besvar(Tekst("Årsak"))
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
            .besvar(true)

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        // Avhengigheter
        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))
        assertEquals(null, søknadprosess.resultat())

        assertErUbesvarte(
            søknadprosess.dato("${DinSituasjon.`arbeidsforhold startdato arbeidsforhold`}.1"),
            søknadprosess.dato("${DinSituasjon.`arbeidsforhold arbeidstid redusert fra dato`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før redusert arbeidstid`}.1"),
            søknadprosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til redusert arbeidstid`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - permittert`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))
        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold midlertidig med kontraktfestet sluttdato`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.midlertidig-med-kontraktfestet-sluttdato.svar.ja"))
        søknadprosess.dato("${DinSituasjon.`arbeidsforhold kontraktfestet sluttdato`}.1").besvar(1.februar)
        søknadprosess.dato("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold oppstartsdato`}.1")
            .besvar(1.januar)
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold permittert fra fiskeri næring`}.1").besvar(true)
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før permittert`}.1").besvar(false)
        søknadprosess.periode("${DinSituasjon.`arbeidsforhold permittert periode`}.1").besvar(Periode(5.januar))
        søknadprosess.heltall("${DinSituasjon.`arbeidsforhold permittert prosent`}.1").besvar(40)
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du lønnsplikt periode`}.1").besvar(true)
        søknadprosess.periode("${DinSituasjon.`arbeidsforhold når var lønnsplikt periode`}.1")
            .besvar(Periode(6.januar, 20.januar))

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        // Avhengigheter
        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        assertEquals(null, søknadprosess.resultat())

        assertErUbesvarte(
            søknadprosess.envalg("${DinSituasjon.`arbeidsforhold midlertidig med kontraktfestet sluttdato`}.1"),
            søknadprosess.dato("${DinSituasjon.`arbeidsforhold kontraktfestet sluttdato`}.1"),
            søknadprosess.dato("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold oppstartsdato`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold permittert fra fiskeri næring`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før permittert`}.1"),
            søknadprosess.periode("${DinSituasjon.`arbeidsforhold permittert periode`}.1"),
            søknadprosess.heltall("${DinSituasjon.`arbeidsforhold permittert prosent`}.1"),
            søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du lønnsplikt periode`}.1"),
            søknadprosess.periode("${DinSituasjon.`arbeidsforhold når var lønnsplikt periode`}.1")
        )
    }

    private fun `besvar spørsmål for et arbeidsforhold`() {
        søknadprosess.generator(DinSituasjon.arbeidsforhold).besvar(1)
        søknadprosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        søknadprosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        søknadprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret"))
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold kjent antall timer jobbet`}.1").besvar(false)
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(false)
    }

    private fun `besvar innledende spørsmål om situasjon og arbeidsforhold`() {
        søknadprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        søknadprosess.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)
        søknadprosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        søknadprosess.generator(DinSituasjon.arbeidsforhold).besvar(1)
        søknadprosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        søknadprosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
    }

    private fun `besvar spørsmål om skift, turnus og rotasjon`() {
        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold skift eller turnus`}.1").besvar(true)

        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold rotasjon`}.1").besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${DinSituasjon.`arbeidsforhold rotasjon`}.1").besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.heltall("${DinSituasjon.`arbeidsforhold arbeidsdager siste rotasjon`}.1").besvar(20)
        søknadprosess.heltall("${DinSituasjon.`arbeidsforhold fridager siste rotasjon`}.1").besvar(2)
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *DinSituasjon.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            DinSituasjon.regeltre(søknad)
        ) {
            DinSituasjon.seksjon(this)
        }
        val faktaFraGjenopptak = søknadprosess.nesteSeksjoner().first().joinToString(separator = ",\n") { it.id }
        assertEquals(forventetSpørsmålsrekkefølgeForSøker, faktaFraGjenopptak)
    }

    private fun assertErUbesvarte(vararg fakta: Faktum<*>) =
        fakta.forEach { faktum ->
            assertFalse(faktum.erBesvart())
        }

    private val forventetSpørsmålsrekkefølgeForSøker = """
    101,
    103,
    104,
    102,
    105,
    106,
    107,
    109,
    108,
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
    162
    """.trimIndent()
}
