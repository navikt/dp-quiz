package no.nav.dagpenger.quiz.mediator.soknad.dagpenger.v248

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Fakta.Companion.seksjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.sannsynliggjøresAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Utdanning : DslFaktaseksjon {
    const val `tar du utdanning` = 2001
    const val `avsluttet utdanning siste 6 mnd` = 2002
    const val `planlegger utdanning med dagpenger` = 2003

    const val `dokumentasjon på sluttdato` = 2004
    const val `godkjenning dokumentasjon på sluttdato` = 2005

    override val fakta = listOf(
        boolsk faktum "faktum.tar-du-utdanning" id `tar du utdanning`,

        boolsk faktum "faktum.avsluttet-utdanning-siste-6-mnd" id `avsluttet utdanning siste 6 mnd`
            avhengerAv `tar du utdanning`,

        boolsk faktum "faktum.planlegger-utdanning-med-dagpenger" id `planlegger utdanning med dagpenger`
            avhengerAv `tar du utdanning`,

        dokument faktum "faktum.dokument-utdanning-sluttdato" id `dokumentasjon på sluttdato`
            avhengerAv `avsluttet utdanning siste 6 mnd`,
        boolsk faktum "faktum.dokument-utdanning-sluttdato-godkjenning" id `godkjenning dokumentasjon på sluttdato`
            avhengerAv `dokumentasjon på sluttdato`
    )

    override fun seksjon(fakta: Fakta) =
        listOf(fakta.seksjon("utdanning", Rolle.søker, *spørsmålsrekkefølgeForSøker()))

    override fun regeltre(fakta: Fakta) = with(fakta) {
        "utdanning".deltre {
            "Utdanning".alle(
                "tar utdanning eller ikke".minstEnAv(
                    boolsk(`tar du utdanning`) er true,
                    boolsk(`tar du utdanning`) er false hvisOppfylt {
                        "nylig avsluttet utdanning eller planer om utdanning".alle(
                            "avsluttet utdanning må dokumenteres".minstEnAv(
                                boolsk(`avsluttet utdanning siste 6 mnd`) er false,
                                (boolsk(`avsluttet utdanning siste 6 mnd`) er true).sannsynliggjøresAv(
                                    dokument(`dokumentasjon på sluttdato`)
                                ).godkjentAv(
                                    boolsk(
                                        `godkjenning dokumentasjon på sluttdato`
                                    )
                                )
                            ),
                            boolsk(`planlegger utdanning med dagpenger`).utfylt()
                        )
                    }
                )
            )
        }
    }

    override val spørsmålsrekkefølgeForSøker = listOf(
        `tar du utdanning`,
        `avsluttet utdanning siste 6 mnd`,
        `planlegger utdanning med dagpenger`,
        `dokumentasjon på sluttdato`,
        `godkjenning dokumentasjon på sluttdato`
    )
}
