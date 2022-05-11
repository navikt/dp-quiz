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
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Barnetillegg : DslFaktaseksjon {

    const val `barn liste` = 1001
    const val `barn fornavn mellomnavn` = 1002
    const val `barn etternavn` = 1003
    const val `barn foedselsdato` = 1004
    const val `barn bostedsland` = 1005
    const val `forsoerger du barnet` = 1006
    const val `barn aarsinntekt over 1g` = 1007
    const val `barn inntekt` = 1008

    override val fakta = listOf(
        boolsk faktum "faktum.barn-aarsinntekt-over-1g" id `barn aarsinntekt over 1g`,
        heltall faktum "faktum.barn-inntekt" id `barn inntekt`,
        heltall faktum "faktum.barn-liste" id `barn liste`
            genererer `barn fornavn mellomnavn`
            og `barn etternavn`
            og `barn foedselsdato`
            og `barn bostedsland`
            og `forsoerger du barnet`
            og `barn aarsinntekt over 1g`
            og `barn inntekt`,
        tekst faktum "faktum.barn-fornavn-mellomnavn" id `barn fornavn mellomnavn`,
        tekst faktum "faktum.barn-etternavn" id `barn etternavn`,
        dato faktum "faktum.barn-foedselsdato" id `barn foedselsdato`,
        land faktum "faktum.barn-bostedsland" id `barn bostedsland`,
        boolsk faktum "faktum.forsoerger-du-barnet" id `forsoerger du barnet`

    )

    override fun seksjon(søknad: Søknad): List<Seksjon> {
        val barnetilleggRegister = søknad.seksjon("barnetillegg-register", Rolle.nav, *this.databaseIder())
        val barnetilleggSøker = søknad.seksjon("barnetillegg", Rolle.søker, *this.databaseIder())

        return listOf(barnetilleggRegister, barnetilleggSøker)
    }

    fun regeltre(søknad: Søknad): Subsumsjon = with(søknad) {
        "§ x-x Barnetillegg".alle(
            generator(`barn liste`) har "et eller flere barn".deltre {
                `info om barnet`().hvisOppfylt {
                    `forsørger barnet`()
                }.hvisOppfylt {
                    `har barnet årsinntekt på over 1 år`().hvisOppfylt {
                        `barnets inntekt`()
                    }
                }
            }
        )
    }

    private fun Søknad.`info om barnet`() = "info".alle(
        tekst(`barn fornavn mellomnavn`).utfylt(),
        tekst(`barn etternavn`).utfylt(),
        dato(`barn foedselsdato`).utfylt(),
        land(`barn bostedsland`).utfylt(),
    )

    private fun Søknad.`forsørger barnet`() =
        boolsk(`forsoerger du barnet`) er true

    private fun Søknad.`har barnet årsinntekt på over 1 år`() =
        boolsk(`barn aarsinntekt over 1g`) er true

    private fun Søknad.`barnets inntekt`() =
        heltall(`barn inntekt`).utfylt()
}
