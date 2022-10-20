package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DagpengerMetadataStrategiTest {

    private lateinit var søknadprosess: Søknadprosess

    init {
        Dagpenger.registrer { prototypeSøknad ->
            søknadprosess = Versjon.id(Dagpenger.VERSJON_ID)
                .søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
        }
    }

    @Test
    fun `skjemakode permittering når et arbeidsforhold er permittert`() {
        søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        søknadprosess.envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        søknadprosess.generator(Barnetillegg.`barn liste register`).besvar(0)
        søknadprosess.boolsk(Barnetillegg.`egne barn`).besvar(false)

        søknadprosess.dato(Arbeidsforhold.`dagpenger søknadsdato`).besvar(1.januar)
        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        søknadprosess.generator(Arbeidsforhold.arbeidsforhold).besvar(2)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        søknadprosess.land("${Arbeidsforhold.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold navn bedrift`}.2").besvar(Tekst("navn"))
        søknadprosess.land("${Arbeidsforhold.`arbeidsforhold land`}.2").besvar(Land("NOR"))
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.2")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))

        with(DagpengerMetadataStrategi().metadata(søknadprosess)) {
            assertEquals("04-01.04", this.skjemakode)
        }
    }

    @Test
    fun `skjemakode ordinær når et arbeidsforhold er ordinær`() {
        søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        søknadprosess.envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        søknadprosess.generator(Barnetillegg.`barn liste register`).besvar(0)
        søknadprosess.boolsk(Barnetillegg.`egne barn`).besvar(false)

        søknadprosess.dato(Arbeidsforhold.`dagpenger søknadsdato`).besvar(1.januar)
        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        søknadprosess.generator(Arbeidsforhold.arbeidsforhold).besvar(1)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        søknadprosess.land("${Arbeidsforhold.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        with(DagpengerMetadataStrategi().metadata(søknadprosess)) {
            assertEquals("04-01.03", this.skjemakode)
        }
    }

    @Test
    fun `skjemakode gjenopptak permittering når gjenopptak og permittert`() {
        søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        søknadprosess.envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        søknadprosess.generator(Barnetillegg.`barn liste register`).besvar(0)
        søknadprosess.boolsk(Barnetillegg.`egne barn`).besvar(false)

        søknadprosess.dato(Arbeidsforhold.`dagpenger søknadsdato`).besvar(1.januar)
        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        søknadprosess.generator(Arbeidsforhold.arbeidsforhold).besvar(2)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        søknadprosess.land("${Arbeidsforhold.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold navn bedrift`}.2").besvar(Tekst("navn"))
        søknadprosess.land("${Arbeidsforhold.`arbeidsforhold land`}.2").besvar(Land("NOR"))
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.2")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))

        with(DagpengerMetadataStrategi().metadata(søknadprosess)) {
            assertEquals("04-16.04", this.skjemakode)
        }
    }

    @Test
    fun `skjemakode gjenopptak når gjenopptak`() {
        søknadprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        søknadprosess.envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        søknadprosess.generator(Barnetillegg.`barn liste register`).besvar(0)
        søknadprosess.boolsk(Barnetillegg.`egne barn`).besvar(false)

        søknadprosess.dato(Arbeidsforhold.`dagpenger søknadsdato`).besvar(1.januar)
        søknadprosess.envalg(Arbeidsforhold.`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        søknadprosess.generator(Arbeidsforhold.arbeidsforhold).besvar(1)
        søknadprosess.tekst("${Arbeidsforhold.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        søknadprosess.land("${Arbeidsforhold.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        søknadprosess.envalg("${Arbeidsforhold.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        with(DagpengerMetadataStrategi().metadata(søknadprosess)) {
            assertEquals("04-16.03", this.skjemakode)
        }
    }
}
