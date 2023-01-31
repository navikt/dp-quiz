package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Faktagrupper
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
    private val søknad = Fakta(HenvendelsesType(Prosess.Dagpenger, -1), *fakta)
    private lateinit var faktagrupper: Faktagrupper

    @BeforeEach
    fun setup() {
        faktagrupper = søknad.testSøknadprosess(DinSituasjon.regeltre(søknad)) {
            DinSituasjon.seksjon(this)
        }
    }

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        DinSituasjon.verifiserFeltsammensetting(64, 8480)
    }

    @Test
    fun `Sjekk at alle fakta er definert i en seksjon`() {
        val faktaISeksjoner = faktagrupper.flatten().map { it.id.toInt() }
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
        faktagrupper.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        faktagrupper.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`).besvar(Tekst("Årsak"))
        faktagrupper.dato(DinSituasjon.`gjenopptak søknadsdato`).besvar(1.januar)
        faktagrupper.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger eller hatt endringer i arbeidsforhold`)
            .besvar(false)
        assertEquals(true, faktagrupper.resultat())

        // Avhengigheter
        faktagrupper.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        assertEquals(null, faktagrupper.resultat())

        assertErUbesvarte(
            faktagrupper.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`),
            faktagrupper.dato(DinSituasjon.`gjenopptak søknadsdato`),
            faktagrupper.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger eller hatt endringer i arbeidsforhold`)
        )
    }

    @Test
    fun `Gjenopptak søknad - har jobbet siden sist eller hatt endring i arbeidsforhold`() {
        faktagrupper.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        faktagrupper.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`).besvar(Tekst("Årsak"))
        faktagrupper.dato(DinSituasjon.`gjenopptak søknadsdato`).besvar(1.januar)
        faktagrupper.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger eller hatt endringer i arbeidsforhold`)
            .besvar(true)

        faktagrupper.boolsk(DinSituasjon.`gjenopptak ønsker ny beregning av dagpenger`).besvar(false)
        `besvar spørsmål for et arbeidsforhold`()
        assertEquals(true, faktagrupper.resultat())

        faktagrupper.boolsk(DinSituasjon.`gjenopptak ønsker ny beregning av dagpenger`).besvar(true)
        assertEquals(null, faktagrupper.resultat())

        faktagrupper.boolsk(DinSituasjon.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`).besvar(false)
        assertEquals(true, faktagrupper.resultat())

        faktagrupper.boolsk(DinSituasjon.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`).besvar(true)
        assertEquals(null, faktagrupper.resultat())

        faktagrupper.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
        assertEquals(true, faktagrupper.resultat())

        // Avhengigheter
        faktagrupper.boolsk(DinSituasjon.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`).besvar(false)
        assertErUbesvarte(faktagrupper.envalg(DinSituasjon.`type arbeidstid`))

        faktagrupper.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.vet-ikke"))
        assertEquals(null, faktagrupper.resultat())

        assertErUbesvarte(
            faktagrupper.tekst(DinSituasjon.`gjenopptak årsak til stans av dagpenger`),
            faktagrupper.dato(DinSituasjon.`gjenopptak søknadsdato`),
            faktagrupper.boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger eller hatt endringer i arbeidsforhold`),
            faktagrupper.boolsk(DinSituasjon.`gjenopptak ønsker ny beregning av dagpenger`),
            faktagrupper.boolsk(DinSituasjon.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`)
        )
    }

    @Test
    fun `Ny søknad - type arbeidstid er besvart med Ingen alternativer passer`() {
        faktagrupper.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        faktagrupper.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)
        faktagrupper.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.ingen-passer"))
        assertEquals(true, faktagrupper.resultat())

        // Avhengigheter
        faktagrupper.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        assertEquals(null, faktagrupper.resultat())

        assertErUbesvarte(
            faktagrupper.dato(DinSituasjon.`dagpenger søknadsdato`),
            faktagrupper.envalg(DinSituasjon.`type arbeidstid`)
        )
    }

    @Test
    fun `Arbeidsforhold - ikke endret`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret"))
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold kjent antall timer jobbet`}.1").besvar(true)
        faktagrupper.desimaltall("${DinSituasjon.`arbeidsforhold antall timer jobbet`}.1").besvar(40.5)
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(false)
        assertEquals(true, faktagrupper.resultat())

        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(true)
        assertEquals(null, faktagrupper.resultat())

        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold tilleggsopplysninger`}.1")
            .besvar(Tekst("Tilleggsopplysninger"))
        assertEquals(true, faktagrupper.resultat())

        // Avhengigheter
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.avskjediget"))
        assertEquals(null, faktagrupper.resultat())

        assertErUbesvarte(
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold kjent antall timer jobbet`}.1"),
            faktagrupper.desimaltall("${DinSituasjon.`arbeidsforhold antall timer jobbet`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1"),
            faktagrupper.tekst("${DinSituasjon.`arbeidsforhold tilleggsopplysninger`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - avskjediget`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.avskjediget"))
        faktagrupper.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1").besvar(true)
        faktagrupper.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til avskjediget`}.1").besvar(Tekst("Årsak"))
        assertEquals(true, faktagrupper.resultat())

        // Avhengigheter
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))
        assertEquals(null, faktagrupper.resultat())

        assertErUbesvarte(
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1"),
            faktagrupper.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            faktagrupper.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til avskjediget`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - sagt opp av arbeidsgiver`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        faktagrupper.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1").besvar(true)
        faktagrupper.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til sagt opp av arbeidsgiver`}.1")
            .besvar(Tekst("Årsak"))
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
            .besvar(true)

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, faktagrupper.resultat())

        // Avhengigheter
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.arbeidsgiver-konkurs"))
        assertEquals(null, faktagrupper.resultat())

        assertErUbesvarte(
            faktagrupper.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før mistet jobb`}.1"),
            faktagrupper.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            faktagrupper.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til sagt opp av arbeidsgiver`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - arbeidsgiver er konkurs`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.arbeidsgiver-konkurs"))

        faktagrupper.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold med sluttdato`}.1")
            .besvar(true)
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før konkurs`}.1").besvar(true)
        faktagrupper.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)

        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1").besvar(false)
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold godta trekk direkte fra konkursboet`}.1").besvar(true)
        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, faktagrupper.resultat())

        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1").besvar(true)
        assertEquals(null, faktagrupper.resultat())

        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`}.1")
            .besvar(true)
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold godta trekk direkte fra konkursboet`}.1").besvar(true)
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`}.1")
            .besvar(true)
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold har søkt om lønnsgarantimidler`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.har-sokt-om-lonnsgarantimidler.svar.ja"))
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`}.1")
            .besvar(
                Envalg("faktum.arbeidsforhold.dekker-lonnsgarantiordningen-lonnskravet-ditt.svar.ja")
            )
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold utbetalt lønn etter konkurs`}.1").besvar(true)
        faktagrupper.dato("${DinSituasjon.`arbeidsforhold siste dag utbetalt for konkurs`}.1").besvar(1.januar)

        assertEquals(true, faktagrupper.resultat())

        // Avhengigheter
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.kontrakt-utgaatt"))
        assertEquals(null, faktagrupper.resultat())

        assertErUbesvarte(
            faktagrupper.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold med sluttdato`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før konkurs`}.1"),
            faktagrupper.desimaltall("${DinSituasjon.`arbeidsforhold antall timer dette arbeidsforhold`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold godta trekk direkte fra konkursboet`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`}.1"),
            faktagrupper.envalg("${DinSituasjon.`arbeidsforhold har søkt om lønnsgarantimidler`}.1"),
            faktagrupper.envalg("${DinSituasjon.`arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold utbetalt lønn etter konkurs`}.1"),
            faktagrupper.dato("${DinSituasjon.`arbeidsforhold siste dag utbetalt for konkurs`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - utgått kontrakt`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.kontrakt-utgaatt"))

        faktagrupper.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før kontrakt utgikk`}.1")
            .besvar(false)
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1")
            .besvar(false)
        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, faktagrupper.resultat())

        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1")
            .besvar(true)
        assertEquals(null, faktagrupper.resultat())

        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ja"))
        assertEquals(true, faktagrupper.resultat())

        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ikke-svart"))
        assertEquals(true, faktagrupper.resultat())

        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.nei"))
        assertEquals(null, faktagrupper.resultat())

        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold årsak til ikke akseptert tilbud`}.1")
            .besvar(Tekst("Årsak"))
        assertEquals(true, faktagrupper.resultat())

        // Avhengigheter
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-selv"))
        assertEquals(null, faktagrupper.resultat())

        assertErUbesvarte(
            faktagrupper.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1"),
            faktagrupper.envalg("${DinSituasjon.`arbeidsforhold svar på forlengelse eller annen stilling`}.1"),
            faktagrupper.tekst("${DinSituasjon.`arbeidsforhold årsak til ikke akseptert tilbud`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - sagt opp selv`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-selv"))
        faktagrupper.periode("${DinSituasjon.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før du sa opp`}.1").besvar(false)
        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold årsak til du sa opp`}.1").besvar(Tekst("Årsak"))

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, faktagrupper.resultat())

        // Avhengigheter
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        assertEquals(null, faktagrupper.resultat())

        assertErUbesvarte(
            faktagrupper.periode("${DinSituasjon.`arbeidsforhold varighet`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før du sa opp`}.1"),
            faktagrupper.tekst("${DinSituasjon.`arbeidsforhold årsak til du sa opp`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - redusert arbeidstid`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        faktagrupper.dato("${DinSituasjon.`arbeidsforhold startdato arbeidsforhold`}.1").besvar(1.januar)
        faktagrupper.dato("${DinSituasjon.`arbeidsforhold arbeidstid redusert fra dato`}.1").besvar(1.februar)
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før redusert arbeidstid`}.1")
            .besvar(false)
        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til redusert arbeidstid`}.1")
            .besvar(Tekst("Årsak"))
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
            .besvar(true)

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, faktagrupper.resultat())

        // Avhengigheter
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))
        assertEquals(null, faktagrupper.resultat())

        assertErUbesvarte(
            faktagrupper.dato("${DinSituasjon.`arbeidsforhold startdato arbeidsforhold`}.1"),
            faktagrupper.dato("${DinSituasjon.`arbeidsforhold arbeidstid redusert fra dato`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før redusert arbeidstid`}.1"),
            faktagrupper.tekst("${DinSituasjon.`arbeidsforhold hva er årsak til redusert arbeidstid`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
        )
    }

    @Test
    fun `Arbeidsforhold - permittert`() {
        `besvar innledende spørsmål om situasjon og arbeidsforhold`()

        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold midlertidig med kontraktfestet sluttdato`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.midlertidig-med-kontraktfestet-sluttdato.svar.ja"))
        faktagrupper.dato("${DinSituasjon.`arbeidsforhold kontraktfestet sluttdato`}.1").besvar(1.februar)
        faktagrupper.dato("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold oppstartsdato`}.1")
            .besvar(1.januar)
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold permittert fra fiskeri næring`}.1").besvar(true)
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før permittert`}.1").besvar(false)
        faktagrupper.periode("${DinSituasjon.`arbeidsforhold permittert periode`}.1").besvar(Periode(5.januar))
        faktagrupper.heltall("${DinSituasjon.`arbeidsforhold permittert prosent`}.1").besvar(40)
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du lønnsplikt periode`}.1").besvar(true)
        faktagrupper.periode("${DinSituasjon.`arbeidsforhold når var lønnsplikt periode`}.1")
            .besvar(Periode(6.januar, 20.januar))

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, faktagrupper.resultat())

        // Avhengigheter
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        assertEquals(null, faktagrupper.resultat())

        assertErUbesvarte(
            faktagrupper.envalg("${DinSituasjon.`arbeidsforhold midlertidig med kontraktfestet sluttdato`}.1"),
            faktagrupper.dato("${DinSituasjon.`arbeidsforhold kontraktfestet sluttdato`}.1"),
            faktagrupper.dato("${DinSituasjon.`arbeidsforhold midlertidig arbeidsforhold oppstartsdato`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold permittert fra fiskeri næring`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du antall timer før permittert`}.1"),
            faktagrupper.periode("${DinSituasjon.`arbeidsforhold permittert periode`}.1"),
            faktagrupper.heltall("${DinSituasjon.`arbeidsforhold permittert prosent`}.1"),
            faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold vet du lønnsplikt periode`}.1"),
            faktagrupper.periode("${DinSituasjon.`arbeidsforhold når var lønnsplikt periode`}.1")
        )
    }

    private fun `besvar spørsmål for et arbeidsforhold`() {
        faktagrupper.generator(DinSituasjon.arbeidsforhold).besvar(1)
        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        faktagrupper.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret"))
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold kjent antall timer jobbet`}.1").besvar(false)
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold har tilleggsopplysninger`}.1").besvar(false)
    }

    private fun `besvar innledende spørsmål om situasjon og arbeidsforhold`() {
        faktagrupper.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        faktagrupper.dato(DinSituasjon.`dagpenger søknadsdato`).besvar(1.januar)
        faktagrupper.envalg(DinSituasjon.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        faktagrupper.generator(DinSituasjon.arbeidsforhold).besvar(1)
        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("Ullfabrikken"))
        faktagrupper.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
    }

    private fun `besvar spørsmål om skift, turnus og rotasjon`() {
        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold skift eller turnus`}.1").besvar(true)

        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold rotasjon`}.1").besvar(false)
        assertEquals(true, faktagrupper.resultat())

        faktagrupper.boolsk("${DinSituasjon.`arbeidsforhold rotasjon`}.1").besvar(true)
        assertEquals(null, faktagrupper.resultat())

        faktagrupper.heltall("${DinSituasjon.`arbeidsforhold arbeidsdager siste rotasjon`}.1").besvar(20)
        faktagrupper.heltall("${DinSituasjon.`arbeidsforhold fridager siste rotasjon`}.1").besvar(2)
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val fakta = Fakta(HenvendelsesType(Prosess.Dagpenger, -1), *DinSituasjon.fakta())
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
