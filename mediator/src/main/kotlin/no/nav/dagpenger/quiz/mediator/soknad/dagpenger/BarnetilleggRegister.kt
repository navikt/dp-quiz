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
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object BarnetilleggRegister : DslFaktaseksjon {

    const val `barn liste register` = 1009
    const val `barn fornavn mellomnavn register` = 1010
    const val `barn etternavn register` = 1011
    const val `barn foedselsdato register` = 1012
    const val `barn bostedsland register` = 1013
    const val `forsoerger du barnet register` = 1014
    const val `barn aarsinntekt over 1g register` = 1015
    const val `barn inntekt register` = 1016

    override val fakta = listOf(

        heltall faktum "faktum.register.barn-liste" id `barn liste register`
            genererer `barn fornavn mellomnavn register`
            og `barn etternavn register`
            og `barn foedselsdato register`
            og `barn bostedsland register`
            og `forsoerger du barnet register`
            og `barn aarsinntekt over 1g register`
            og `barn inntekt register`,
        tekst faktum "faktum.register.barn-fornavn-mellomnavn" id `barn fornavn mellomnavn register`,
        tekst faktum "faktum.register.barn-etternavn" id `barn etternavn register`,
        dato faktum "faktum.register.barn-foedselsdato" id `barn foedselsdato register`,
        land faktum "faktum.register.barn-bostedsland" id `barn bostedsland register`,
        boolsk faktum "faktum.register.forsoerger-du-barnet" id `forsoerger du barnet register` kanEndresAv Rolle.søker,
        boolsk faktum "faktum.register.barn-aarsinntekt-over-1g" id `barn aarsinntekt over 1g register` kanEndresAv Rolle.søker,
        heltall faktum "faktum.register.barn-inntekt" id `barn inntekt register` kanEndresAv Rolle.søker,

    )

    override fun seksjon(søknad: Søknad): List<Seksjon> {
        val barnetilleggRegister = søknad.seksjon(
            "barnetillegg-register", Rolle.nav,
            `barn liste register`,
            `barn fornavn mellomnavn register`,
            `barn etternavn register`,
            `barn bostedsland register`
        )
        return listOf(barnetilleggRegister)
    }

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "barnetillegg-register".deltre {
            "minstEnAv".minstEnAv(
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
            )
        }
    }
}
