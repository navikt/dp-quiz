package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object BarnetilleggSøker : DslFaktaseksjon {

    const val `barn liste` = 1001
    const val `barn fornavn mellomnavn` = 1002
    const val `barn etternavn` = 1003
    const val `barn foedselsdato` = 1004
    const val `barn statsborgerskap` = 1005
    const val `forsoerger du barnet` = 1006
    const val `barn aarsinntekt over 1g` = 1007
    const val `barn inntekt` = 1008

    override val fakta = listOf(
        heltall faktum "faktum.barn-liste" id `barn liste`
            genererer `barn fornavn mellomnavn`
            og `barn etternavn`
            og `barn foedselsdato`
            og `barn statsborgerskap`
            og `forsoerger du barnet`
            og `barn aarsinntekt over 1g`
            og `barn inntekt`,
        tekst faktum "faktum.barn-fornavn-mellomnavn" id `barn fornavn mellomnavn`,
        tekst faktum "faktum.barn-etternavn" id `barn etternavn`,
        dato faktum "faktum.barn-foedselsdato" id `barn foedselsdato`,
        land faktum "faktum.barn-statsborgerskap" id `barn statsborgerskap`,
        boolsk faktum "faktum.forsoerger-du-barnet" id `forsoerger du barnet`,
        boolsk faktum "faktum.barn-aarsinntekt-over-1g" id `barn aarsinntekt over 1g`,
        // @todo: Burde være faktum type 'inntekt'?
        heltall faktum "faktum.barn-inntekt" id `barn inntekt`

    )

    override fun seksjon(søknad: Søknad): List<Seksjon> {
        val barnetilleggSøker = søknad.seksjon(
            "barnetillegg",
            Rolle.søker,
            *this.databaseIder(),
            BarnetilleggRegister.`forsoerger du barnet register`,
            BarnetilleggRegister.`barn aarsinntekt over 1g register`,
            BarnetilleggRegister.`barn inntekt register`
        )
        return listOf(barnetilleggSøker)
    }

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "barnetillegg".deltre {
            "barn eller ikke".minstEnAv(
                generator(`barn liste`) er 0,
                generator(`barn liste`) minst 1 hvisOppfylt {
                    generator(`barn liste`) med "et eller flere barn".deltre {
                        `barnets navn, fødselsdsto og bostedsland`().hvisOppfylt {
                            "forsørger eller ikke".minstEnAv(
                                boolsk(`forsoerger du barnet`) er false,
                                boolsk(`forsoerger du barnet`) er true hvisOppfylt {
                                    `har barnet årsinntekt over 1G`()
                                }
                            )
                        }
                    }
                }
            )
        }
    }

    private fun Søknad.`barnets navn, fødselsdsto og bostedsland`() = "navn, dato og bostedsland".alle(
        tekst(`barn fornavn mellomnavn`).utfylt(),
        tekst(`barn etternavn`).utfylt(),
        dato(`barn foedselsdato`).utfylt(),
        land(`barn statsborgerskap`).utfylt(),
    )

    private fun Søknad.`har barnet årsinntekt over 1G`() = "inntekt over 1G eller ikke".minstEnAv(
        boolsk(`barn aarsinntekt over 1g`) er false,
        boolsk(`barn aarsinntekt over 1g`) er true hvisOppfylt {
            `barnets inntekt`()
        }
    )

    private fun Søknad.`barnets inntekt`() =
        heltall(`barn inntekt`).utfylt()
}
