package no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett

object Aldersvurdering : DslFaktaseksjon {

    const val virkningsdato =
    const val over67årFradato = 2

    override val fakta = listOf(
        dato faktum "virkningsdato" id virkningsdato,
        dato faktum "over 67 år fra dato" id over67årFradato,
    )

    override val spørsmålsrekkefølgeForSøker: List<Int>
        get() = TODO("Not yet implemented")

    override fun seksjon(søknad: Søknad): List<Seksjon> {
        TODO("Not yet implemented")
    }

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon {
        TODO("Not yet implemented")
    }

    private val `søkeren må være under 67 år ved virkningstidspunkt` =
        with(AvslagPåMinsteinntektOppsett.prototypeSøknad) {
            "under 67år" deltre {
                dato(virkningsdato) før dato(over67årFradato)
            }
        }
}