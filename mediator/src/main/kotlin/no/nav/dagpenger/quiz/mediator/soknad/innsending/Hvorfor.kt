package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.sannsynliggjøresAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Hvorfor : DslFaktaseksjon {
    const val `hvorfor vil du sende oss ting` = 1001
    const val `hva sender du oss` = 1002
    const val `dokumentasjon` = 1005
    const val `godkjenning av dokumentasjon` = 1006

    override val fakta = listOf(
        envalg faktum "faktum.hvorfor"
            med "svar.klage"
            med "svar.ettersending"
            med "svar.endring"
            med "svar.vet-ikke" id `hvorfor vil du sende oss ting`,
        tekst faktum "faktum.hva" id `hva sender du oss`,
        dokument faktum "dokumentasjon" id `dokumentasjon`,
        boolsk faktum "dokumentasjon.godkjent" id `godkjenning av dokumentasjon` avhengerAv `dokumentasjon`
    )

    override fun seksjon(søknad: Søknad) = listOf(
        søknad.seksjon(
            "spørsmål",
            Rolle.søker,
            *spørsmålsrekkefølge()
        ),
        søknad.seksjon(
            "godkjenning",
            Rolle.saksbehandler,
            `godkjenning av dokumentasjon`
        )
    )

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "spørsmål".deltre {
            envalg(`hvorfor vil du sende oss ting`).utfylt().hvisOppfylt {
                tekst(`hva sender du oss`).utfylt().sannsynliggjøresAv(
                    dokument(`dokumentasjon`)
                ).godkjentAv(boolsk(`godkjenning av dokumentasjon`))
            }
        }
    }

    override val spørsmålsrekkefølge: List<Int> = listOf(
        `hvorfor vil du sende oss ting`,
        `hva sender du oss`
    )
}
