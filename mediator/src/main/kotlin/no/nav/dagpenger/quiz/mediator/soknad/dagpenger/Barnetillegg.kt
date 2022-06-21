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

object Barnetillegg : DslFaktaseksjon {

    const val `barn liste` = 1001
    const val `barn fornavn mellomnavn` = 1002
    const val `barn etternavn` = 1003
    const val `barn foedselsdato` = 1004
    const val `barn statsborgerskap` = 1005
    const val `forsoerger du barnet` = 1006
    const val `barn aarsinntekt over 1g` = 1007
    const val `barn inntekt` = 1008

    const val `barn liste register` = 1009
    const val `barn fornavn mellomnavn register` = 1010
    const val `barn etternavn register` = 1011
    const val `barn foedselsdato register` = 1012
    const val `barn statsborgerskap register` = 1013
    const val `forsoerger du barnet register` = 1014
    const val `barn aarsinntekt over 1g register` = 1015
    const val `barn inntekt register` = 1016

    override val fakta = listOf(
        heltall faktum "faktum.register.barn-liste" id `barn liste register`
            genererer `barn fornavn mellomnavn register`
            og `barn etternavn register`
            og `barn foedselsdato register`
            og `barn statsborgerskap register`
            og `forsoerger du barnet register`
            og `barn aarsinntekt over 1g register`
            og `barn inntekt register`,
        tekst faktum "faktum.barn-fornavn-mellomnavn" id `barn fornavn mellomnavn register`,
        tekst faktum "faktum.barn-etternavn" id `barn etternavn register`,
        dato faktum "faktum.barn-foedselsdato" id `barn foedselsdato register`,
        land faktum "faktum.barn-statsborgerskap" id `barn statsborgerskap register`,
        boolsk faktum "faktum.forsoerger-du-barnet" id `forsoerger du barnet register` avhengerAv `barn liste register`,
        boolsk faktum "faktum.barn-aarsinntekt-over-1g" id `barn aarsinntekt over 1g register` avhengerAv `barn liste register`,
        heltall faktum "faktum.barn-inntekt" id `barn inntekt register` avhengerAv `barn liste register`,

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
        val barnetilleggRegister = søknad.seksjon(
            "barnetillegg-register", Rolle.nav,
            `barn liste register`,
            `barn fornavn mellomnavn register`,
            `barn etternavn register`,
            `barn statsborgerskap register`,
            `barn foedselsdato register`
        )
        val barnetillegg = søknad.seksjon(
            "barnetillegg", Rolle.søker,
            `barn liste`,
            `barn fornavn mellomnavn`,
            `barn etternavn`,
            `barn foedselsdato`,
            `barn statsborgerskap`,
            `forsoerger du barnet`,
            `barn aarsinntekt over 1g`,
            `barn inntekt`,
            `forsoerger du barnet register`,
            `barn aarsinntekt over 1g register`,
            `barn inntekt register`
        )

        return listOf(barnetilleggRegister, barnetillegg)
    }

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "barnetillegg".deltre {
            "barnetillegg fra PDL register".minstEnAv(
                generator(`barn liste register`) minst 0,
                generator(`barn liste register`) minst 1 hvisOppfylt {
                    generator(`barn liste register`) med "et eller flere barn".deltre {
                        "forsørger eller ikke".minstEnAv(
                            boolsk(`forsoerger du barnet register`) er false,
                            boolsk(`forsoerger du barnet register`) er true hvisOppfylt {
                                "inntekt over 1G eller ikke".minstEnAv(
                                    boolsk(`barn aarsinntekt over 1g register`) er false,
                                    boolsk(`barn aarsinntekt over 1g register`) er true hvisOppfylt {
                                        heltall(`barn inntekt register`).utfylt()
                                    }
                                )
                            }
                        )
                    }
                }
            ).hvisOppfylt {
                "barnetillegg fra søker".minstEnAv(
                    generator(`barn liste`) er 0, // TODO: Erstatte med "skal du legge til flere barn?"
                    generator(`barn liste`) minst 1 hvisOppfylt {
                        generator(`barn liste`) med "et eller flere barn".deltre {
                            `barnets navn, fødselsdato og bostedsland`().hvisOppfylt {
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
    }

    private fun Søknad.`barnets navn, fødselsdato og bostedsland`() = "navn, dato og bostedsland".alle(
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
