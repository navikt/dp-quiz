package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.sannsynliggjøresAv
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Hvorfor : DslFaktaseksjon {
    const val `hvorfor vil du sende oss ting` = 1001
    const val `hva sender du oss` = 1002
    const val `dokumentasjon` = 1003
    override val fakta = listOf(
        envalg faktum "faktum.hvorfor"
            med "svar.klage"
            med "svar.ettersending"
            med "svar.endring"
            med "svar.vet-ikke" id `hvorfor vil du sende oss ting`,
        tekst faktum "faktum.hva" id `hva sender du oss`,
        dokument faktum "faktum.dokument" id `dokumentasjon`,
    )

    override fun seksjon(søknad: Søknad) = listOf(søknad.seksjon("spørsmål", Rolle.søker, *this.databaseIder()))

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "spørsmål".deltre {
            envalg(`hvorfor vil du sende oss ting`).utfylt().hvisOppfylt {
                "dokument".alle(
                    tekst(`hva sender du oss`).sannsynliggjøresAv(dokument(`dokumentasjon`))
                )
            }
        }
    }
}
