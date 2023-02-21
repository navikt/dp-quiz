package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.soknad.Prosesser
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DinSituasjon.`dagpenger søknadsdato`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DinSituasjon.`type arbeidstid`
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DagpengerMetadataStrategiTest {
    private lateinit var prosess: Prosess

    init {
        Dagpenger.registrer { prototypeSøknad ->
            prosess = Versjon.id(Prosesser.Søknad)
                .utredningsprosess(prototypeSøknad)
        }
    }

    @Test
    fun `skjemakode permittering når et arbeidsforhold er permittert`() {
        prosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        prosess.generator(Barnetillegg.`barn liste register`).besvar(0)
        prosess.boolsk(Barnetillegg.`egne barn`).besvar(false)

        prosess.dato(`dagpenger søknadsdato`).besvar(1.januar)
        prosess.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        prosess.generator(DinSituasjon.arbeidsforhold).besvar(2)
        prosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        prosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        prosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.2").besvar(Tekst("navn"))
        prosess.land("${DinSituasjon.`arbeidsforhold land`}.2").besvar(Land("NOR"))
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.2")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))

        with(DagpengerMetadataStrategi().metadata(prosess)) {
            assertEquals("04-01.04", this.skjemakode)
        }
    }

    @Test
    fun `skjemakode ordinær når et arbeidsforhold er ordinær`() {
        prosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        prosess.generator(Barnetillegg.`barn liste register`).besvar(0)
        prosess.boolsk(Barnetillegg.`egne barn`).besvar(false)

        prosess.dato(`dagpenger søknadsdato`).besvar(1.januar)
        prosess.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        prosess.generator(DinSituasjon.arbeidsforhold).besvar(1)
        prosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        prosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        with(DagpengerMetadataStrategi().metadata(prosess)) {
            assertEquals("04-01.03", this.skjemakode)
        }
    }

    @Test
    fun `skjemakode gjenopptak permittering når gjenopptak og permittert`() {
        prosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        prosess.generator(Barnetillegg.`barn liste register`).besvar(0)
        prosess.boolsk(Barnetillegg.`egne barn`).besvar(false)

        prosess.dato(`dagpenger søknadsdato`).besvar(1.januar)
        prosess.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        prosess.generator(DinSituasjon.arbeidsforhold).besvar(2)
        prosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        prosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        prosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.2").besvar(Tekst("navn"))
        prosess.land("${DinSituasjon.`arbeidsforhold land`}.2").besvar(Land("NOR"))
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.2")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))

        with(DagpengerMetadataStrategi().metadata(prosess)) {
            assertEquals("04-16.04", this.skjemakode)
        }
    }

    @Test
    fun `skjemakode gjenopptak når gjenopptak`() {
        prosess.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        prosess.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        prosess.generator(Barnetillegg.`barn liste register`).besvar(0)
        prosess.boolsk(Barnetillegg.`egne barn`).besvar(false)

        prosess.dato(`dagpenger søknadsdato`).besvar(1.januar)
        prosess.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        prosess.generator(DinSituasjon.arbeidsforhold).besvar(1)
        prosess.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        prosess.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        prosess.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        with(DagpengerMetadataStrategi().metadata(prosess)) {
            assertEquals("04-16.03", this.skjemakode)
        }
    }
}
