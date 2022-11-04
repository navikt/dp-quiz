package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
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

object Gjenopptak : DslFaktaseksjon {
    const val `dagpenger søknadsdato` = 8001
    const val `type arbeidstid` = 8002
    const val `mottatt dagpenger siste 12 mnd` = 10001
    const val `gjenopptak jobbet siden sist du fikk dagpenger` = 8049
    const val `gjenopptak årsak til stans av dagpenger` = 8050
    const val `gjenopptak søknadsdato` = 8051
    const val `gjenopptak endringer i arbeidsforhold siden sist` = 8052
    const val `gjenopptak ønsker ny beregning av dagpenger` = 8053
    const val `gjenopptak ønsker å få fastsatt ny vanlig arbeidstid` = 8054
    override val fakta = listOf(
        envalg faktum "faktum.type-arbeidstid"
            med "svar.fast"
            med "svar.varierende"
            med "svar.kombinasjon"
            med "svar.ingen-passer" id `type arbeidstid`,
        dato faktum "faktum.dagpenger-soknadsdato" id `dagpenger søknadsdato`,
        envalg faktum "faktum.mottatt-dagpenger-siste-12-mnd" med "svar.ja" med "svar.nei" med "svar.vet-ikke" id `mottatt dagpenger siste 12 mnd`,
        boolsk faktum "faktum.arbeidsforhold.gjenopptak.jobbet-siden-sist" id `gjenopptak jobbet siden sist du fikk dagpenger` avhengerAv `mottatt dagpenger siste 12 mnd`,
        tekst faktum "faktum.arbeidsforhold.gjenopptak.aarsak-til-stans" id `gjenopptak årsak til stans av dagpenger` avhengerAv `mottatt dagpenger siste 12 mnd`,
        dato faktum "faktum.arbeidsforhold.gjenopptak.soknadsdato-gjenopptak" id `gjenopptak søknadsdato` avhengerAv `mottatt dagpenger siste 12 mnd`,
        boolsk faktum "faktum.arbeidsforhold.gjenopptak.endringer-i-arbeidsforhold" id `gjenopptak endringer i arbeidsforhold siden sist` avhengerAv `mottatt dagpenger siste 12 mnd`,
        boolsk faktum "faktum.arbeidsforhold.gjenopptak.onsker-ny-beregning" id `gjenopptak ønsker ny beregning av dagpenger` avhengerAv `mottatt dagpenger siste 12 mnd`,
        boolsk faktum "faktum.arbeidsforhold.gjenopptak.onsker-faa-fastsatt-ny-vanlig-arbeidstid" id `gjenopptak ønsker å få fastsatt ny vanlig arbeidstid` avhengerAv `mottatt dagpenger siste 12 mnd`
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
            tekst(`gjenopptak årsak til stans av dagpenger`).utfylt(),
            dato(`gjenopptak søknadsdato`).utfylt(),
            (boolsk(`gjenopptak jobbet siden sist du fikk dagpenger`).er(true)).hvisOppfylt {
                "hatt endringer i arbeidsforhold siden sist eller ikke".minstEnAv(
                    boolsk(`gjenopptak endringer i arbeidsforhold siden sist`) er false,
                    boolsk(`gjenopptak endringer i arbeidsforhold siden sist`) er true hvisOppfylt {
                        "arbeidsforhold og spørsmål om beregning og fastsatt ny arbeidstid".alle(
                            `ønsker ny beregning og fastsatt ny arbeidstid eller ikke`()
                        )
                    }
                )
            }
        )

    private fun Søknad.`ønsker ny beregning og fastsatt ny arbeidstid eller ikke`() =
        "ønsker ny beregning av dagpenger eller ikke".minstEnAv(
            boolsk(`gjenopptak ønsker ny beregning av dagpenger`) er false,
            boolsk(`gjenopptak ønsker ny beregning av dagpenger`) er true hvisOppfylt {
                "ønsker å få fastsatt ny vanlig arbeidstid eller ikke".minstEnAv(
                    boolsk(`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`) er false,
                    boolsk(`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`) er true hvisOppfylt {
                        `type arbeidstid`()
                    }
                )
            }
        )

    private fun Søknad.`type arbeidstid`() = envalg(`type arbeidstid`).utfylt()

    private fun Søknad.søknadsdato() = dato(`dagpenger søknadsdato`).utfylt()

    override val spørsmålsrekkefølgeForSøker = listOf(
        `mottatt dagpenger siste 12 mnd`,
        `gjenopptak årsak til stans av dagpenger`,
        `gjenopptak søknadsdato`,
        `gjenopptak jobbet siden sist du fikk dagpenger`,
        `gjenopptak endringer i arbeidsforhold siden sist`,
        `gjenopptak ønsker ny beregning av dagpenger`,
        `gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`,
        `dagpenger søknadsdato`,
        `type arbeidstid`
    )
}
