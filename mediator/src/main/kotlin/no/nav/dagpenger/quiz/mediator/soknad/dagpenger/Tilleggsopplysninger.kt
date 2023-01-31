package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Fakta.Companion.seksjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Tilleggsopplysninger : DslFaktaseksjon {
    const val tilleggsopplysninger = 4001
    const val `har tilleggsopplysninger` = 4002

    override val fakta = listOf(
        tekst faktum "faktum.tilleggsopplysninger" id tilleggsopplysninger avhengerAv `har tilleggsopplysninger`,

        boolsk faktum "faktum.tilleggsopplysninger.har-tilleggsopplysninger" id `har tilleggsopplysninger`
    )

    override fun seksjon(fakta: Fakta) =
        listOf(fakta.seksjon("tilleggsopplysninger", Rolle.søker, *spørsmålsrekkefølgeForSøker()))

    override fun regeltre(fakta: Fakta) = with(fakta) {
        "tilleggsopplysninger".deltre {
            "har tilleggsopplysninger eller ikke".minstEnAv(
                boolsk(`har tilleggsopplysninger`) er false,
                boolsk(`har tilleggsopplysninger`) er true hvisOppfylt {
                    tekst(tilleggsopplysninger).utfylt()
                }
            )
        }
    }

    override val spørsmålsrekkefølgeForSøker = listOf(
        `har tilleggsopplysninger`,
        tilleggsopplysninger
    )
}
