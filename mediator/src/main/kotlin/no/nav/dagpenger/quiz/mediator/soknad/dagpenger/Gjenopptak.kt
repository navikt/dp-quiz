package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.inneholder
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Arbeidsforhold.søknadsdato
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Arbeidsforhold.`type arbeidstid`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Gjenopptak.`mottatt dagpenger siste 12 mnd`

object Gjenopptak : DslFaktaseksjon {
    const val `mottatt dagpenger siste 12 mnd` = 10001
    override val fakta = listOf(
        envalg faktum "faktum.mottatt-dagpenger-siste-12-mnd" med "svar.ja" med "svar.nei" med "svar.vet-ikke" id `mottatt dagpenger siste 12 mnd`
    )

    override fun seksjon(søknad: Søknad) =
        listOf(søknad.seksjon("gjenopptak", Rolle.søker, *spørsmålsrekkefølgeForSøker()))

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "gjenopptak".deltre {
            (envalg(`mottatt dagpenger siste 12 mnd`) inneholder Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
                .hvisOppfylt { `arbeidsforhold gjenopptak`() }
                .hvisIkkeOppfylt {
                    "søknadsdato, type arbeidstid og alle arbeidsforhold".alle(
                        søknadsdato(),
                        `type arbeidstid`()
                    )
                }
        }
    }

    private fun Søknad.`arbeidsforhold gjenopptak`() =
        "spørsmål om gjenopptaket".alle(
            boolsk(Arbeidsforhold.`gjenopptak jobbet siden sist du fikk dagpenger`).utfylt(),
            tekst(Arbeidsforhold.`gjenopptak årsak til stans av dagpenger`).utfylt(),
            dato(Arbeidsforhold.`gjenopptak søknadsdato`).utfylt(),
            "hatt endringer i arbeidsforhold siden sist eller ikke".minstEnAv(
                boolsk(Arbeidsforhold.`gjenopptak endringer i arbeidsforhold siden sist`) er false,
                boolsk(Arbeidsforhold.`gjenopptak endringer i arbeidsforhold siden sist`) er true hvisOppfylt {
                    "arbeidsforhold og spørsmål om beregning og fastsatt ny arbeidstid".alle(
                        `ønsker ny beregning og fastsatt ny arbeidstid eller ikke`()
                    )
                }
            )
        )

    private fun Søknad.`ønsker ny beregning og fastsatt ny arbeidstid eller ikke`() =
        "ønsker ny beregning av dagpenger eller ikke".minstEnAv(
            boolsk(Arbeidsforhold.`gjenopptak ønsker ny beregning av dagpenger`) er false,
            boolsk(Arbeidsforhold.`gjenopptak ønsker ny beregning av dagpenger`) er true hvisOppfylt {
                "ønsker å få fastsatt ny vanlig arbeidstid eller ikke".minstEnAv(
                    boolsk(Arbeidsforhold.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`) er false,
                    boolsk(Arbeidsforhold.`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`) er true hvisOppfylt {
                        `type arbeidstid`()
                    }
                )
            }
        )

    private fun Søknad.`type arbeidstid`() = envalg(`type arbeidstid`).utfylt()

    private fun Søknad.søknadsdato() = dato(Arbeidsforhold.`dagpenger søknadsdato`).utfylt()

    override val spørsmålsrekkefølgeForSøker = listOf(
        `mottatt dagpenger siste 12 mnd`
    )
}
