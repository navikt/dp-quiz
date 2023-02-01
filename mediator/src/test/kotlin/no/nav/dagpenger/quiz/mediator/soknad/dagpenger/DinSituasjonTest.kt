package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktaVersjon
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Utredningsprosess
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
    private val søknad = Fakta(FaktaVersjon(Prosess.Dagpenger, -1), *fakta)
    private lateinit var utredningsprosess: Utredningsprosess

    @BeforeEach
    fun setup() {
        utredningsprosess = søknad.testSøknadprosess(DinSituasjon.regeltre(søknad)) {
            DinSituasjon.seksjon(this)
        }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        DinSituasjon.verifiserFeltsammensetting(64, 8480)
    }

    @Test
    fun `Sjekk at alle fakta er definert i en seksjon`() {
        val faktaISeksjoner = utredningsprosess.flatten().map { it.id.toInt() }
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
    fun `Gjenopptak søknad - har ikke jobbet siden sist eller hatt noen endring i arbeidsforhold`() {
        utredningsprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        utredningsprosess.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`).besvar(Tekst("Årsak"))
        utredningsprosess.dato(DinSituasjon.`gjenopptak søknadsdato`).besvar(1.januar)
        utredningsprosess.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger eller hatt endringer i arbeidsforhold`)
            .besvar(false)
        assertEquals(true, utredningsprosess.resultat())

        // Avhengigheter
        utredningsprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        assertEquals(null, utredningsprosess.resultat())

        assertErUbesvarte(
            utredningsprosess.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`),
            utredningsprosess.dato(DinSituasjon.`gjenopptak søknadsdato`),
            utredningsprosess.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger eller hatt endringer i arbeidsforhold`)
        )
    }

    @Test
    fun `Gjenopptak søknad - har jobbet siden sist eller hatt endring i arbeidsforhold`() {
        utredningsprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        utredningsprosess.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`).besvar(Tekst("Årsak"))
        utredningsprosess.dato(DinSituasjon.`gjenopptak søknadsdato`).besvar(1.januar)
        utredningsprosess.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger eller hatt endringer i arbeidsforhold`)
            .besvar(true)

        utredningsprosess.boolsk(DinSituasjon.`gjenopptak ønsker ny beregning av dagpenger`).besvar(false)
        `besvar spørsmål for et arbeidsforhold`()
        assertEquals(true, utredningsprosess.resultat())

        utredningsprosess.boolsk(DinSituasjon.`gjenopptak ønsker ny beregning av dagpenger`).besvar(true)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.boolsk(DinSituasjon.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`).besvar(false)
        assertEquals(true, utredningsprosess.resultat())

        utredningsprosess.boolsk(DinSituasjon.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`).besvar(true)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
        assertEquals(true, utredningsprosess.resultat())

        // Avhengigheter
        utredningsprosess.boolsk(DinSituasjon.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`).besvar(false)
        assertErUbesvarte(utredningsprosess.envalg(DinSituasjon.`type arbeidstid`))

        utredningsprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.vet-ikke"))
        assertEquals(null, utredningsprosess.resultat())

        assertErUbesvarte(
            utredningsprosess.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`),
            utredningsprosess.dato(DinSituasjon.`gjenopptak søknadsdato`),
            utredningsprosess.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger eller hatt endringer i arbeidsforhold`),
            utredningsprosess.boolsk(DinSituasjon.`gjenopptak ønsker ny beregning av dagpenger`),
            utredningsprosess.boolsk(DinSituasjon.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`)
        )
    }

    @Test
    fun `Ny søknad - type arbeidstid er besvart med Ingen alternativer passer`() {
        utredningsprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        utredningsprosess.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)
        utredningsprosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.ingen-passer"))
        assertEquals(true, utredningsprosess.resultat())

        // Avhengigheter
        utredningsprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        assertEquals(null, utredningsprosess.resultat())

        assertErUbesvarte(
            utredningsprosess.dato(DinSituasjon.`dagpenger søknadsdato`),
            utredningsprosess.envalg(DinSituasjon.`type arbeidstid`)
        )
    }

    @Test
    fun `Arbeidsforhold - ikke endret`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret"))
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold kjent antall timer jobbet`}.1").besvar(true)
        utredningsprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer jobbet`}.1").besvar(40.5)
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(false)
        assertEquals(true, utredningsprosess.resultat())

        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(true)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold tilleggsopplysninger`}.1")
            .besvar(Tekst("Tilleggsopplysninger"))
        assertEquals(true, utredningsprosess.resultat())

        // Avhengigheter
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.avskjediget"))
        assertEquals(null, utredningsprosess.resultat())

        assertErUbesvarte(
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold kjent antall timer jobbet`}.1"),
            utredningsprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer jobbet`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1"),
            utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold tilleggsopplysninger`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - avskjediget`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.avskjediget"))
        utredningsprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1").besvar(true)
        utredningsprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til avskjediget`}.1").besvar(Tekst("Årsak"))
        assertEquals(true, utredningsprosess.resultat())

        // Avhengigheter
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))
        assertEquals(null, utredningsprosess.resultat())

        assertErUbesvarte(
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1"),
            utredningsprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til avskjediget`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - sagt opp av arbeidsgiver`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        utredningsprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1").besvar(true)
        utredningsprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til sagt opp av arbeidsgiver`}.1")
            .besvar(Tekst("Årsak"))
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
            .besvar(true)

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, utredningsprosess.resultat())

        // Avhengigheter
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.arbeidsgiver-konkurs"))
        assertEquals(null, utredningsprosess.resultat())

        assertErUbesvarte(
            utredningsprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1"),
            utredningsprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til sagt opp av arbeidsgiver`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - arbeidsgiver er konkurs`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.arbeidsgiver-konkurs"))

        utredningsprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold med sluttdato`}.1")
            .besvar(true)
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før konkurs`}.1").besvar(true)
        utredningsprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)

        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1").besvar(false)
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold godta trekk direkte fra konkursboet`}.1").besvar(true)
        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, utredningsprosess.resultat())

        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1").besvar(true)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`}.1")
            .besvar(true)
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold godta trekk direkte fra konkursboet`}.1").besvar(true)
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`}.1")
            .besvar(true)
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold har søkt om lønnsgarantimidler`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.har-sokt-om-lonnsgarantimidler.svar.ja"))
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`}.1")
            .besvar(
                Envalg("faktum.arbeidsforhold.dekker-lonnsgarantiordningen-lonnskravet-ditt.svar.ja")
            )
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold utbetalt lønn etter konkurs`}.1").besvar(true)
        utredningsprosess.dato("${DinSituasjon.`arbeidsforhold siste dag utbetalt for konkurs`}.1").besvar(1.januar)

        assertEquals(true, utredningsprosess.resultat())

        // Avhengigheter
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.kontrakt-utgaatt"))
        assertEquals(null, utredningsprosess.resultat())

        assertErUbesvarte(
            utredningsprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold med sluttdato`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før konkurs`}.1"),
            utredningsprosess.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold godta trekk direkte fra konkursboet`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`}.1"),
            utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold har søkt om lønnsgarantimidler`}.1"),
            utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold utbetalt lønn etter konkurs`}.1"),
            utredningsprosess.dato("${DinSituasjon.`arbeidsforhold siste dag utbetalt for konkurs`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - utgått kontrakt`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.kontrakt-utgaatt"))

        utredningsprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før kontrakt utgikk`}.1")
            .besvar(false)
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1")
            .besvar(false)
        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, utredningsprosess.resultat())

        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1")
            .besvar(true)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ja"))
        assertEquals(true, utredningsprosess.resultat())

        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ikke-svart"))
        assertEquals(true, utredningsprosess.resultat())

        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.nei"))
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold årsak til ikke akseptert tilbud`}.1")
            .besvar(Tekst("Årsak"))
        assertEquals(true, utredningsprosess.resultat())

        // Avhengigheter
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-selv"))
        assertEquals(null, utredningsprosess.resultat())

        assertErUbesvarte(
            utredningsprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1"),
            utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1"),
            utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold årsak til ikke akseptert tilbud`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - sagt opp selv`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-selv"))
        utredningsprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før du sa opp`}.1").besvar(false)
        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold årsak til du sa opp`}.1").besvar(Tekst("Årsak"))

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, utredningsprosess.resultat())

        // Avhengigheter
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        assertEquals(null, utredningsprosess.resultat())

        assertErUbesvarte(
            utredningsprosess.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før du sa opp`}.1"),
            utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold årsak til du sa opp`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - redusert arbeidstid`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        utredningsprosess.dato("${DinSituasjon.`arbeidsforhold startdato arbeidsforhold`}.1").besvar(1.januar)
        utredningsprosess.dato("${DinSituasjon.`arbeidsforhold arbeidstid redusert fra dato`}.1").besvar(1.februar)
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før redusert arbeidstid`}.1")
            .besvar(false)
        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til redusert arbeidstid`}.1")
            .besvar(Tekst("Årsak"))
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
            .besvar(true)

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, utredningsprosess.resultat())

        // Avhengigheter
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))
        assertEquals(null, utredningsprosess.resultat())

        assertErUbesvarte(
            utredningsprosess.dato("${DinSituasjon.`arbeidsforhold startdato arbeidsforhold`}.1"),
            utredningsprosess.dato("${DinSituasjon.`arbeidsforhold arbeidstid redusert fra dato`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før redusert arbeidstid`}.1"),
            utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til redusert arbeidstid`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - permittert`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold midlertidig med kontraktfestet sluttdato`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.midlertidig-med-kontraktfestet-sluttdato.svar.ja"))
        utredningsprosess.dato("${DinSituasjon.`arbeidsforhold kontraktfestet sluttdato`}.1").besvar(1.februar)
        utredningsprosess.dato("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold oppstartsdato`}.1")
            .besvar(1.januar)
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold permittert fra fiskeri næring`}.1").besvar(true)
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før permittert`}.1").besvar(false)
        utredningsprosess.periode("${DinSituasjon.`arbeidsforhold permittert periode`}.1").besvar(Periode(5.januar))
        utredningsprosess.heltall("${DinSituasjon.`arbeidsforhold permittert prosent`}.1").besvar(40)
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du lønnsplikt periode`}.1").besvar(true)
        utredningsprosess.periode("${DinSituasjon.`arbeidsforhold når var lønnsplikt periode`}.1")
            .besvar(Periode(6.januar, 20.januar))

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, utredningsprosess.resultat())

        // Avhengigheter
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        assertEquals(null, utredningsprosess.resultat())

        assertErUbesvarte(
            utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold midlertidig med kontraktfestet sluttdato`}.1"),
            utredningsprosess.dato("${DinSituasjon.`arbeidsforhold kontraktfestet sluttdato`}.1"),
            utredningsprosess.dato("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold oppstartsdato`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold permittert fra fiskeri næring`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før permittert`}.1"),
            utredningsprosess.periode("${DinSituasjon.`arbeidsforhold permittert periode`}.1"),
            utredningsprosess.heltall("${DinSituasjon.`arbeidsforhold permittert prosent`}.1"),
            utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold vet du lønnsplikt periode`}.1"),
            utredningsprosess.periode("${DinSituasjon.`arbeidsforhold når var lønnsplikt periode`}.1")
        )
    }

    private fun `besvar spørsmål for et arbeidsforhold`() {
        utredningsprosess.generator(DinSituasjon.arbeidsforhold).besvar(1)
        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        utredningsprosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret"))
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold kjent antall timer jobbet`}.1").besvar(false)
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(false)
    }

    private fun `besvar innledende spørsmål om situasjon og arbeidsforhold`() {
        utredningsprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        utredningsprosess.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)
        utredningsprosess.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        utredningsprosess.generator(DinSituasjon.arbeidsforhold).besvar(1)
        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        utredningsprosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
    }

    private fun `besvar spørsmål om skift, turnus og rotasjon`() {
        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold skift eller turnus`}.1").besvar(true)

        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold rotasjon`}.1").besvar(false)
        assertEquals(true, utredningsprosess.resultat())

        utredningsprosess.boolsk("${DinSituasjon.`arbeidsforhold rotasjon`}.1").besvar(true)
        assertEquals(null, utredningsprosess.resultat())

        utredningsprosess.heltall("${DinSituasjon.`arbeidsforhold arbeidsdager siste rotasjon`}.1").besvar(20)
        utredningsprosess.heltall("${DinSituasjon.`arbeidsforhold fridager siste rotasjon`}.1").besvar(2)
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val fakta = Fakta(FaktaVersjon(Prosess.Dagpenger, -1), *DinSituasjon.fakta())
        val søknadprosess = fakta.testSøknadprosess(
            DinSituasjon.regeltre(fakta)
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

    private val forventetSpørsmålsrekkefølgeForSøker = """
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
