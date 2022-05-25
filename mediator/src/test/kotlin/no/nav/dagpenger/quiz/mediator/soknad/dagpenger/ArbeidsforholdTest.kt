package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.februar
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ArbeidsforholdTest {
    private val fakta = Arbeidsforhold.fakta() + Gjenopptak.fakta()
    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *fakta)
    private lateinit var søknadprosess: Søknadprosess

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        Arbeidsforhold.verifiserFeltsammensetting(54, 433485)
    }

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(Arbeidsforhold.regeltre(søknad))
    }

    @Test
    fun `Trenger ikke fylle inn arbeidsforhold når type arbeidstid er besvart med Ingen alternativer passer og det ikke er gjenopptak`() {
        søknadprosess.boolsk(Gjenopptak.`mottatt dagpenger siste 12 mnd`).besvar(false)
        søknadprosess.dato(Arbeidsforhold.`dagpenger soknadsdato`).besvar(1.januar)

        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.varierende"))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.kombinasjon"))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.ingen-passer"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `arbeidsforhold ved gjenopptak av dagpenger`() {
        `besvar innledende spørsmål om arbeidsforhold for gjenopptak`()
        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak endringer i arbeidsforhold siden sist`).besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak endringer i arbeidsforhold siden sist`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        `besvar spørsmål for et arbeidsforhold`()

        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak onsker ny beregning av dagpenger`).besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak onsker ny beregning av dagpenger`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak onsker aa faa fastsatt ny vanlig arbeidstid`).besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak onsker aa faa fastsatt ny vanlig arbeidstid`).besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `ikke endret arbeidsforhold`() {
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
    }

    @Test
    fun avskjediget() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.avskjediget"))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer foer mistet jobb`}.1").besvar(true)
        søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold hva er aarsak til avskjediget`}.1").besvar(Tekst("Årsak"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `sagt opp av arbeidsgiver`() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer foer mistet jobb`}.1").besvar(true)
        søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold vet du aarsak til sagt opp av arbeidsgiver`}.1")
            .besvar(Tekst("Årsak"))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1")
            .besvar(true)

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Arbeidsgiver er konkurs`() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.arbeidsgiver-konkurs"))

        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold midlertidig arbeidsforhold med sluttdato`}.1").besvar(true)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer foer konkurs`}.1").besvar(true)
        søknadprosess.desimaltall("${Arbeidsforhold.`arbeidsforhold antall timer dette arbeidsforhold`}.1").besvar(40.5)

        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold soke forskudd lonnsgarantimidler`}.1").besvar(false)
        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold soke forskudd lonnsgarantimidler`}.1").besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold soke forskudd lonnsgarantimidler i tillegg til dagpenger`}.1").besvar(true)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold godta trekk fra nav av forskudd fra lonnsgarantimidler`}.1").besvar(true)
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold har sokt om lonnsgarantimidler`}.1").besvar(Envalg("faktum.arbeidsforhold.har-sokt-om-lonnsgarantimidler.svar.ja"))
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold dekker lonnsgarantiordningen lonnskravet ditt`}.1").besvar(
            Envalg("faktum.arbeidsforhold.dekker-lonnsgarantiordningen-lonnskravet-ditt.svar.ja")
        )
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold utbetalt lonn etter konkurs`}.1").besvar(true)
        søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold siste dag utbetalt for konkurs`}.1").besvar(1.januar)

        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Kontrakten er utgått`() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.kontrakt-utgaatt"))

        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer foer kontrakt utgikk`}.1").besvar(false)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1").besvar(false)
        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold tilbud om forlengelse eller annen stilling`}.1").besvar(true)
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold svar paa forlengelse eller annen stilling`}.1").besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ja"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold svar paa forlengelse eller annen stilling`}.1").besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ikke-svart"))
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold svar paa forlengelse eller annen stilling`}.1").besvar(Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.nei"))
        assertEquals(null, søknadprosess.resultat())

        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold aarsak til ikke akseptert tilbud`}.1").besvar(Tekst("Årsak"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Sagt opp selv`() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-selv"))
        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold varighet`}.1").besvar(Periode(1.januar, 1.februar))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer foer du sa opp`}.1").besvar(false)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold aarsak til du sa opp`}.1").besvar(Tekst("Årsak"))

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Redusert arbeidstid`() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
        søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold startdato arbeidsforhold`}.1").besvar(1.januar)
        søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold arbeidstid redusert fra dato`}.1").besvar(1.februar)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer foer redusert arbeidstid`}.1").besvar(false)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold vet du aarsak til redusert arbeidstid`}.1").besvar(Tekst("Årsak"))
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold tilbud om annen stilling eller annet sted i norge`}.1").besvar(true)

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun permittert() {
        `besvar innledende spørsmål om arbeidsforhold`()

        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold midlertidig med kontraktfestet sluttdato`}.1").besvar(Envalg("faktum.arbeidsforhold.midlertidig-med-kontraktfestet-sluttdato.svar.ja"))
        søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold kontraktfestet sluttdato`}.1").besvar(1.februar)
        søknadprosess.dato("${Arbeidsforhold.`arbeidsforhold midlertidig arbeidsforhold oppstartsdato`}.1").besvar(1.januar)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold permittertert fra fiskeri naering`}.1").besvar(true)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du antall timer foer permittert`}.1").besvar(false)
        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold permittert periode`}.1").besvar(Periode(5.januar))
        søknadprosess.heltall("${Arbeidsforhold.`arbeidsforhold permittert prosent`}.1").besvar(40)
        søknadprosess.boolsk("${Arbeidsforhold.`arbeidsforhold vet du lonnsplikt periode`}.1").besvar(true)
        søknadprosess.periode("${Arbeidsforhold.`arbeidsforhold naar var lonnsplikt periode`}.1").besvar(Periode(6.januar, 20.januar))

        `besvar spørsmål om skift, turnus og rotasjon`()
        assertEquals(true, søknadprosess.resultat())
    }

    private fun `besvar innledende spørsmål om arbeidsforhold for gjenopptak`() {
        søknadprosess.boolsk(Gjenopptak.`mottatt dagpenger siste 12 mnd`).besvar(true)
        søknadprosess.boolsk(Arbeidsforhold.`gjenopptak jobbet siden sist du fikk dagpenger`).besvar(true)
        søknadprosess.tekst(Arbeidsforhold.`gjenopptak aarsak til stans av dagpenger`).besvar(Tekst("Årsak"))
        søknadprosess.dato(Arbeidsforhold.`gjenopptak soknadsdato`).besvar(1.januar)
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
        søknadprosess.boolsk(Gjenopptak.`mottatt dagpenger siste 12 mnd`).besvar(false)
        søknadprosess.dato(Arbeidsforhold.`dagpenger soknadsdato`).besvar(1.januar)
        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
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
}
