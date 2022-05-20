package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Utdanning : DslFaktaseksjon {

    const val `tar du utdanning` = 2001
    const val `avsluttet utdanning siste 6 mnd` = 2002
    const val `planlegger utdanning med dagpenger` = 2003

    override val fakta = listOf(
        boolsk faktum "faktum.tar-du-utdanning" id `tar du utdanning`,
        boolsk faktum "faktum.avsluttet-utdanning-siste-6-mnd" id `avsluttet utdanning siste 6 mnd` avhengerAv `tar du utdanning`,
        boolsk faktum "faktum.planlegger-utdanning-med-dagpenger" id `planlegger utdanning med dagpenger` avhengerAv `tar du utdanning`
    )

    override fun seksjon(søknad: Søknad) = listOf(søknad.seksjon("utdanning", Rolle.søker, *this.databaseIder()))

    fun regeltre(søknad: Søknad) = with(søknad) {
        "Utdanning".alle(
            "tar utdanning eller ikke".minstEnAv(
                boolsk(`tar du utdanning`) er true,
                boolsk(`tar du utdanning`) er false hvisOppfylt {
                    "nylig avsluttet utdanning eller planer om utdanning".alle(
                        boolsk(`avsluttet utdanning siste 6 mnd`).utfylt(),
                        boolsk(`planlegger utdanning med dagpenger`).utfylt()
                    )
                }
            )
        )
    }
}
