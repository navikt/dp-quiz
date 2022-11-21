package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
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
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.sannsynliggjøresAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Barnetillegg : DslFaktaseksjon {
    const val `barn liste` = 1001
    const val `barn fornavn mellomnavn` = 1002
    const val `barn etternavn` = 1003
    const val `barn fødselsdato` = 1004
    const val `barn statsborgerskap` = 1005
    const val `forsørger du barnet` = 1006
    const val `egne barn` = 1007
    const val `barn liste register` = 1008
    const val `barn fornavn mellomnavn register` = 1009
    const val `barn etternavn register` = 1010
    const val `barn fødselsdato register` = 1011
    const val `barn statsborgerskap register` = 1012
    const val `forsørger du barnet register` = 1013

    const val `dokumentasjon fødselsattest bostedsbevis for barn under 18år` = 1014
    const val `godkjenning av fødselsattest bostedsbevis for barn under 18år` = 1015

    override val fakta = listOf(
        heltall faktum "faktum.register.barn-liste" id `barn liste register`
            genererer `barn fornavn mellomnavn register`
            og `barn etternavn register`
            og `barn fødselsdato register`
            og `barn statsborgerskap register`
            og `forsørger du barnet register`,

        tekst faktum "faktum.barn-fornavn-mellomnavn" id `barn fornavn mellomnavn register`,

        tekst faktum "faktum.barn-etternavn" id `barn etternavn register`,

        dato faktum "faktum.barn-foedselsdato" id `barn fødselsdato register`,

        land faktum "faktum.barn-statsborgerskap" id `barn statsborgerskap register`,

        boolsk faktum "faktum.forsoerger-du-barnet" id `forsørger du barnet register` avhengerAv `barn liste register`,

        boolsk faktum "faktum.legge-til-egne-barn" id `egne barn`,

        heltall faktum "faktum.barn-liste" id `barn liste`
            avhengerAv `egne barn`
            genererer `barn fornavn mellomnavn`
            navngittAv `barn fornavn mellomnavn`
            og `barn etternavn`
            og `barn fødselsdato`
            og `barn statsborgerskap`
            og `forsørger du barnet`
            og `dokumentasjon fødselsattest bostedsbevis for barn under 18år`,

        tekst faktum "faktum.barn-fornavn-mellomnavn" id `barn fornavn mellomnavn`,

        tekst faktum "faktum.barn-etternavn" id `barn etternavn`,

        dato faktum "faktum.barn-foedselsdato" id `barn fødselsdato`,

        land faktum "faktum.barn-statsborgerskap" id `barn statsborgerskap`,

        boolsk faktum "faktum.forsoerger-du-barnet" id `forsørger du barnet`,

        dokument faktum "faktum.dokument-foedselsattest-bostedsbevis-for-barn-under-18aar"
            id `dokumentasjon fødselsattest bostedsbevis for barn under 18år` avhengerAv `forsørger du barnet`,

        boolsk faktum "faktum.godkjenning-dokumentasjon-foedselsattest-bostedsbevis-for-barn-under-18aar"
            id `godkjenning av fødselsattest bostedsbevis for barn under 18år`
            avhengerAv `dokumentasjon fødselsattest bostedsbevis for barn under 18år`
    )

    override fun seksjon(søknad: Søknad): List<Seksjon> {
        val barnetilleggRegister = søknad.seksjon("barnetillegg-register", Rolle.nav, *navSpørsmålsrekkefølge)
        val barnetillegg = søknad.seksjon("barnetillegg", Rolle.søker, *spørsmålsrekkefølgeForSøker())

        return listOf(barnetilleggRegister, barnetillegg)
    }

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "barnetillegg".deltre {
            "barnetillegg fra PDL register".minstEnAv(
                generator(`barn liste register`) minst 0,
                generator(`barn liste register`) minst 1 hvisOppfylt {
                    generator(`barn liste register`) med "et eller flere barn".deltre {
                        boolsk(`forsørger du barnet register`).utfylt()
                    }
                }
            ).hvisOppfylt {
                boolsk(`egne barn`) er false hvisIkkeOppfylt {
                    "barnetillegg fra søker".minstEnAv(
                        generator(`barn liste`) er 0,
                        generator(`barn liste`) minst 1 hvisOppfylt {
                            generator(`barn liste`) med "et eller flere barn".deltre {
                                `barnets navn, fødselsdato og bostedsland`().hvisOppfylt {
                                    "forsørger barnet eller ikke".minstEnAv(
                                        (boolsk(`forsørger du barnet`) er true)
                                            .sannsynliggjøresAv(
                                                dokument(`dokumentasjon fødselsattest bostedsbevis for barn under 18år`)
                                            )
                                            .godkjentAv(
                                                boolsk(`godkjenning av fødselsattest bostedsbevis for barn under 18år`)
                                            ),
                                        boolsk(`forsørger du barnet`) er false
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun Søknad.`barnets navn, fødselsdato og bostedsland`() = "navn, dato og bostedsland".alle(
        tekst(`barn fornavn mellomnavn`).utfylt(),
        tekst(`barn etternavn`).utfylt(),
        dato(`barn fødselsdato`).utfylt(),
        land(`barn statsborgerskap`).utfylt()
    )

    override val spørsmålsrekkefølgeForSøker = listOf(
        `forsørger du barnet register`,
        `egne barn`,
        `barn liste`,
        `barn fornavn mellomnavn`,
        `barn etternavn`,
        `barn fødselsdato`,
        `barn statsborgerskap`,
        `forsørger du barnet`,
        `dokumentasjon fødselsattest bostedsbevis for barn under 18år`
    )
    private val navSpørsmålsrekkefølge = listOf(
        `barn liste register`,
        `barn fornavn mellomnavn register`,
        `barn etternavn register`,
        `barn statsborgerskap register`,
        `barn fødselsdato register`
    ).toIntArray()
}
