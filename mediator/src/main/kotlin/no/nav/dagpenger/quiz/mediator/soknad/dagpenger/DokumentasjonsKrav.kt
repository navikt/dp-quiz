package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.factory.FaktumFactory
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object DokumentasjonsKrav : DslFaktaseksjon {

    const val `tjenestebevis for avtjent verneplikt` = 11001
    const val `tjenestebevis tilgjengelig` = 11002
    const val `tjenestebevis ikke tilgjengelig årsak` = 11003

    override val fakta: List<FaktumFactory<*>>
        get() = listOf(
            boolsk faktum "faktum.dokument-verneplikt-tjenestebevis-tilgjengelig" id `tjenestebevis tilgjengelig`,
            dokument faktum "faktum.dokument-verneplikt-tjenestebevis" id `tjenestebevis for avtjent verneplikt` avhengerAv Verneplikt.`avtjent militaer sivilforsvar tjeneste siste 12 mnd`,
            tekst faktum "faktum.dokument-verneplikt-tjenestebevis-årsak" id `tjenestebevis ikke tilgjengelig årsak` avhengerAv `tjenestebevis tilgjengelig`
        )

    override fun seksjon(søknad: Søknad): List<Seksjon> {
        return listOf(søknad.seksjon("dokumentasjonskrav", Rolle.søker, *this.databaseIder()))
    }

    fun regeltre(søknad: Søknad): Subsumsjon = with(søknad) {

        "dokumentasjonskrav".minstEnAv(
            boolsk(Verneplikt.`avtjent militaer sivilforsvar tjeneste siste 12 mnd`) er false,
            boolsk(Verneplikt.`avtjent militaer sivilforsvar tjeneste siste 12 mnd`) er true hvisOppfylt {
                boolsk(`tjenestebevis tilgjengelig`) er true hvisOppfylt {
                    dokument(`tjenestebevis for avtjent verneplikt`).utfylt()
                } hvisIkkeOppfylt {
                    tekst(`tjenestebevis ikke tilgjengelig årsak`).utfylt()
                }
            }
        )
    }
}
