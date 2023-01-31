package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Fakta.Companion.seksjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.regel.inneholder
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.sannsynliggjøresAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object GenerellInnsending : DslFaktaseksjon {
    const val `hvorfor sender du inn dokumentasjon` = 1001
    const val `skriv kort hvorfor du sender inn dokumentasjon` = 1002
    const val `tittel på dokument` = 1003
    const val dokumentasjon = 1005
    const val `godkjenning av dokumentasjon` = 1006

    override val fakta = listOf(
        envalg faktum "faktum.generell-innsending.hvorfor"
            med "svar.klage"
            med "svar.ettersending"
            med "svar.endring"
            med "svar.annet" id `hvorfor sender du inn dokumentasjon`,

        tekst faktum "faktum.generell-innsending.skriv-hvorfor" id `skriv kort hvorfor du sender inn dokumentasjon`
            avhengerAv `hvorfor sender du inn dokumentasjon`,

        tekst faktum "faktum.generell-innsending.tittel-paa-dokument" id `tittel på dokument` avhengerAv `skriv kort hvorfor du sender inn dokumentasjon` og `hvorfor sender du inn dokumentasjon`,

        dokument faktum "faktum.generell-innsending.dokumentasjon" id dokumentasjon avhengerAv `tittel på dokument`,
        boolsk faktum "faktum.generell-innsending.godkjenning-dokumentasjon" id `godkjenning av dokumentasjon` avhengerAv dokumentasjon
    )

    override fun seksjon(fakta: Fakta) = listOf(
        fakta.seksjon(
            "generell-innsending",
            Rolle.søker,
            *spørsmålsrekkefølgeForSøker()
        )
    )

    override fun regeltre(fakta: Fakta): DeltreSubsumsjon = with(fakta) {
        "spørsmål".deltre {
            "alle spørsmålene må være besvart".alle(
                (envalg(`hvorfor sender du inn dokumentasjon`) inneholder Envalg("faktum.generell-innsending.hvorfor.svar.annet")) hvisOppfylt {
                    tekst(`skriv kort hvorfor du sender inn dokumentasjon`).utfylt()
                },
                tekst(`tittel på dokument`).utfylt()
                    .sannsynliggjøresAv(dokument(dokumentasjon)).godkjentAv(
                        boolsk(
                            `godkjenning av dokumentasjon`
                        )
                    )
            )
        }
    }

    override val spørsmålsrekkefølgeForSøker: List<Int> = listOf(
        `hvorfor sender du inn dokumentasjon`,
        `skriv kort hvorfor du sender inn dokumentasjon`,
        `tittel på dokument`,
        dokumentasjon
    )
}
