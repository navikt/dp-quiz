package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.inneholder
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.sannsynliggjøresAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Hvorfor : DslFaktaseksjon {
    const val `hvorfor vil du sende oss ting` = 1001
    const val `hva sender du oss` = 1002
    const val tittel = 1003
    const val dokumentasjon = 1005
    const val `godkjenning av dokumentasjon` = 1006

    override val fakta = listOf(
        envalg faktum "faktum.hvorfor"
            med "svar.klage"
            med "svar.ettersending"
            med "svar.endring"
            med "svar.vet-ikke" id `hvorfor vil du sende oss ting`,
        tekst faktum "faktum.hva" id `hva sender du oss`,
        tekst faktum "faktum.tittel" id tittel,
        dokument faktum "dokumentasjon" id dokumentasjon,
        boolsk faktum "dokumentasjon.godkjent" id `godkjenning av dokumentasjon` avhengerAv dokumentasjon
    )

    override fun seksjon(søknad: Søknad) = listOf(
        søknad.seksjon(
            "spørsmål",
            Rolle.søker,
            *spørsmålsrekkefølgeForSøker()
        )
    )

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "spørsmål".deltre {
            "alle spørsmålene må være besvart".alle(
                (envalg(`hvorfor vil du sende oss ting`) inneholder Envalg("faktum.hvorfor.svar.vet-ikke")) hvisOppfylt {
                    tekst(`hva sender du oss`).utfylt()
                },
                tekst(tittel).utfylt()
                    .sannsynliggjøresAv(dokument(dokumentasjon)).godkjentAv(
                        boolsk(
                            `godkjenning av dokumentasjon`
                        )
                    )
            )
        }
    }

    override val spørsmålsrekkefølgeForSøker: List<Int> = listOf(
        `hvorfor vil du sende oss ting`,
        `hva sender du oss`,
        tittel,
        dokumentasjon
    )
}
