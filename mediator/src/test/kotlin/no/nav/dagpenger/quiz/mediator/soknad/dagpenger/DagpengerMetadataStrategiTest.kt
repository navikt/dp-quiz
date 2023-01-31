package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DinSituasjon.`dagpenger søknadsdato`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DinSituasjon.`type arbeidstid`
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DagpengerMetadataStrategiTest {

    private lateinit var utredningsprosess: Utredningsprosess

    init {
        Dagpenger.registrer { prototypeSøknad ->
            utredningsprosess = Versjon.id(Dagpenger.VERSJON_ID)
                .søknadprosess(prototypeSøknad)
        }
    }

    @Test
    fun `skjemakode permittering når et arbeidsforhold er permittert`() {
        utredningsprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        utredningsprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        utredningsprosess.generator(Barnetillegg.`barn liste register`).besvar(0)
        utredningsprosess.boolsk(Barnetillegg.`egne barn`).besvar(false)

        utredningsprosess.dato(`dagpenger søknadsdato`).besvar(1.januar)
        utredningsprosess.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        utredningsprosess.generator(DinSituasjon.arbeidsforhold).besvar(2)
        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        utredningsprosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.2").besvar(Tekst("navn"))
        utredningsprosess.land("${DinSituasjon.`arbeidsforhold land`}.2").besvar(Land("NOR"))
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.2")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))

        with(DagpengerMetadataStrategi().metadata(utredningsprosess)) {
            assertEquals("04-01.04", this.skjemakode)
        }
    }

    @Test
    fun `skjemakode ordinær når et arbeidsforhold er ordinær`() {
        utredningsprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        utredningsprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        utredningsprosess.generator(Barnetillegg.`barn liste register`).besvar(0)
        utredningsprosess.boolsk(Barnetillegg.`egne barn`).besvar(false)

        utredningsprosess.dato(`dagpenger søknadsdato`).besvar(1.januar)
        utredningsprosess.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        utredningsprosess.generator(DinSituasjon.arbeidsforhold).besvar(1)
        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        utredningsprosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        with(DagpengerMetadataStrategi().metadata(utredningsprosess)) {
            assertEquals("04-01.03", this.skjemakode)
        }
    }

    @Test
    fun `skjemakode gjenopptak permittering når gjenopptak og permittert`() {
        utredningsprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        utredningsprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        utredningsprosess.generator(Barnetillegg.`barn liste register`).besvar(0)
        utredningsprosess.boolsk(Barnetillegg.`egne barn`).besvar(false)

        utredningsprosess.dato(`dagpenger søknadsdato`).besvar(1.januar)
        utredningsprosess.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        utredningsprosess.generator(DinSituasjon.arbeidsforhold).besvar(2)
        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        utredningsprosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.2").besvar(Tekst("navn"))
        utredningsprosess.land("${DinSituasjon.`arbeidsforhold land`}.2").besvar(Land("NOR"))
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.2")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))

        with(DagpengerMetadataStrategi().metadata(utredningsprosess)) {
            assertEquals("04-16.04", this.skjemakode)
        }
    }

    @Test
    fun `skjemakode gjenopptak når gjenopptak`() {
        utredningsprosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        utredningsprosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        utredningsprosess.generator(Barnetillegg.`barn liste register`).besvar(0)
        utredningsprosess.boolsk(Barnetillegg.`egne barn`).besvar(false)

        utredningsprosess.dato(`dagpenger søknadsdato`).besvar(1.januar)
        utredningsprosess.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        utredningsprosess.generator(DinSituasjon.arbeidsforhold).besvar(1)
        utredningsprosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        utredningsprosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        utredningsprosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        with(DagpengerMetadataStrategi().metadata(utredningsprosess)) {
            assertEquals("04-16.03", this.skjemakode)
        }
    }
}
