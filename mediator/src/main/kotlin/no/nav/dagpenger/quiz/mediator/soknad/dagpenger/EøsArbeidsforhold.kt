package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.inneholder
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object EøsArbeidsforhold : DslFaktaseksjon {
    const val `eøs arbeid siste 36 mnd` = 9001
    const val `eøs arbeidsforhold` = 9002
    const val `eøs arbeidsforhold arbeidsgivernavn` = 9003
    const val `eøs arbeidsforhold land` = 9004
    const val `eøs arbeidsforhold personnummer` = 9005
    const val `eøs arbeidsforhold varighet` = 9006

    override val fakta = listOf(
        boolsk faktum "faktum.eos-arbeid-siste-36-mnd" id `eøs arbeid siste 36 mnd`,
        heltall faktum "faktum.eos-arbeidsforhold" id `eøs arbeidsforhold` avhengerAv `eøs arbeid siste 36 mnd`
            genererer `eøs arbeidsforhold arbeidsgivernavn`
            og `eøs arbeidsforhold land`
            og `eøs arbeidsforhold personnummer`
            og `eøs arbeidsforhold varighet`,
        tekst faktum "faktum.eos-arbeidsforhold.arbeidsgivernavn" id `eøs arbeidsforhold arbeidsgivernavn`,
        land faktum "faktum.eos-arbeidsforhold.land" gruppe "eøs" med eøsEllerSveits() id `eøs arbeidsforhold land`,
        tekst faktum "faktum.eos-arbeidsforhold.personnummer" id `eøs arbeidsforhold personnummer`,
        periode faktum "faktum.eos-arbeidsforhold.varighet" id `eøs arbeidsforhold varighet`
    )

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "arbeidsforhold eøs".deltre {
            `er gjenopptak`() hvisOppfylt {
                `har jobbet siden sist`() hvisOppfylt { `eøs arbeidsforhold`() }
            } hvisIkkeOppfylt {
                `hatt fast, varierende eller kombinert arbeidstid`() hvisOppfylt { `eøs arbeidsforhold`() }
            }
        }
    }

    private fun Søknad.`eøs arbeidsforhold`() = "Arbeidsforhold i EØS området".minstEnAv(
        boolsk(`eøs arbeid siste 36 mnd`) er false,
        boolsk(`eøs arbeid siste 36 mnd`) er true hvisOppfylt {
            "Hvis ja, må det oppgi arbeidsforhold".alle(
                generator(`eøs arbeidsforhold`) minst 1,
                generator(`eøs arbeidsforhold`) har "En til flere EØS arbeidsforhold".deltre {
                    "alt må være utfylt".alle(
                        tekst(`eøs arbeidsforhold arbeidsgivernavn`).utfylt(),
                        land(`eøs arbeidsforhold land`).utfylt(),
                        tekst(`eøs arbeidsforhold personnummer`).utfylt(),
                        periode(`eøs arbeidsforhold varighet`).utfylt()
                    )
                }
            )
        }
    )

    private fun Søknad.`er gjenopptak`() =
        envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`) inneholder Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja")

    private fun Søknad.`har jobbet siden sist`() =
        boolsk(DinSituasjon.`gjenopptak jobbet siden sist du fikk dagpenger`) er true

    private fun Søknad.`hatt fast, varierende eller kombinert arbeidstid`() =
        "har hatt fast, varierende eller kombinasjon arbeidstid".minstEnAv(
            envalg(DinSituasjon.`type arbeidstid`) inneholder Envalg("faktum.type-arbeidstid.svar.fast"),
            envalg(DinSituasjon.`type arbeidstid`) inneholder Envalg("faktum.type-arbeidstid.svar.varierende"),
            envalg(DinSituasjon.`type arbeidstid`) inneholder Envalg("faktum.type-arbeidstid.svar.kombinasjon")
        )

    override fun seksjon(søknad: Søknad) =
        listOf(søknad.seksjon("eos-arbeidsforhold", Rolle.søker, *spørsmålsrekkefølgeForSøker()))

    override val spørsmålsrekkefølgeForSøker = listOf(
        `eøs arbeid siste 36 mnd`,
        `eøs arbeidsforhold`,
        `eøs arbeidsforhold arbeidsgivernavn`,
        `eøs arbeidsforhold land`,
        `eøs arbeidsforhold personnummer`,
        `eøs arbeidsforhold varighet`
    )
}
