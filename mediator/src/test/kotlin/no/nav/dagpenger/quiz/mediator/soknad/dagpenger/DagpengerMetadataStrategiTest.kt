package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DinSituasjon.`dagpenger søknadsdato`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DinSituasjon.`type arbeidstid`
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DagpengerMetadataStrategiTest {

    private lateinit var faktagrupper: Faktagrupper

    init {
        Dagpenger.registrer { prototypeSøknad ->
            faktagrupper = Versjon.id(Dagpenger.VERSJON_ID)
                .søknadprosess(prototypeSøknad)
        }
    }

    @Test
    fun `skjemakode permittering når et arbeidsforhold er permittert`() {
        faktagrupper.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        faktagrupper.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        faktagrupper.generator(Barnetillegg.`barn liste register`).besvar(0)
        faktagrupper.boolsk(Barnetillegg.`egne barn`).besvar(false)

        faktagrupper.dato(`dagpenger søknadsdato`).besvar(1.januar)
        faktagrupper.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        faktagrupper.generator(DinSituasjon.arbeidsforhold).besvar(2)
        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        faktagrupper.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.2").besvar(Tekst("navn"))
        faktagrupper.land("${DinSituasjon.`arbeidsforhold land`}.2").besvar(Land("NOR"))
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.2")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))

        with(DagpengerMetadataStrategi().metadata(faktagrupper)) {
            assertEquals("04-01.04", this.skjemakode)
        }
    }

    @Test
    fun `skjemakode ordinær når et arbeidsforhold er ordinær`() {
        faktagrupper.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        faktagrupper.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
        faktagrupper.generator(Barnetillegg.`barn liste register`).besvar(0)
        faktagrupper.boolsk(Barnetillegg.`egne barn`).besvar(false)

        faktagrupper.dato(`dagpenger søknadsdato`).besvar(1.januar)
        faktagrupper.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        faktagrupper.generator(DinSituasjon.arbeidsforhold).besvar(1)
        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        faktagrupper.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        with(DagpengerMetadataStrategi().metadata(faktagrupper)) {
            assertEquals("04-01.03", this.skjemakode)
        }
    }

    @Test
    fun `skjemakode gjenopptak permittering når gjenopptak og permittert`() {
        faktagrupper.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        faktagrupper.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        faktagrupper.generator(Barnetillegg.`barn liste register`).besvar(0)
        faktagrupper.boolsk(Barnetillegg.`egne barn`).besvar(false)

        faktagrupper.dato(`dagpenger søknadsdato`).besvar(1.januar)
        faktagrupper.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        faktagrupper.generator(DinSituasjon.arbeidsforhold).besvar(2)
        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        faktagrupper.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.2").besvar(Tekst("navn"))
        faktagrupper.land("${DinSituasjon.`arbeidsforhold land`}.2").besvar(Land("NOR"))
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.2")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.permittert"))

        with(DagpengerMetadataStrategi().metadata(faktagrupper)) {
            assertEquals("04-16.04", this.skjemakode)
        }
    }

    @Test
    fun `skjemakode gjenopptak når gjenopptak`() {
        faktagrupper.land(Bosted.`hvilket land bor du i`).besvar(Land("NOR"))
        faktagrupper.envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`)
            .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
        faktagrupper.generator(Barnetillegg.`barn liste register`).besvar(0)
        faktagrupper.boolsk(Barnetillegg.`egne barn`).besvar(false)

        faktagrupper.dato(`dagpenger søknadsdato`).besvar(1.januar)
        faktagrupper.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))

        faktagrupper.generator(DinSituasjon.arbeidsforhold).besvar(1)
        faktagrupper.tekst("${DinSituasjon.`arbeidsforhold navn bedrift`}.1").besvar(Tekst("navn"))
        faktagrupper.land("${DinSituasjon.`arbeidsforhold land`}.1").besvar(Land("NOR"))
        faktagrupper.envalg("${DinSituasjon.`arbeidsforhold endret`}.1")
            .besvar(Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))

        with(DagpengerMetadataStrategi().metadata(faktagrupper)) {
            assertEquals("04-16.03", this.skjemakode)
        }
    }
}
