package no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.grensedato67år
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Aldersvurdering : DslFaktaseksjon {

    const val virkningsdato = 1
    const val fødselsdato = 2
    const val grensedato = 3

    override val fakta = listOf(
        dato faktum "virkningsdato" id virkningsdato,
        dato faktum "fødselsdato" id fødselsdato,
        grensedato67år dato "grensedato" av fødselsdato id grensedato
    )

    override val spørsmålsrekkefølgeForSøker: List<Int>
        get() = TODO("Not yet implemented")

    override fun seksjon(søknad: Søknad): List<Seksjon> =
        listOf(søknad.seksjon("alder", Rolle.nav, virkningsdato, fødselsdato, grensedato))

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon {
        return "søkeren må være under aldersgrense ved virkningstidspunkt".deltre {
            with(AldersvurderingOppsett.prototypeSøknad) {
                "under aldersgrense" deltre {
                    dato(virkningsdato) før dato(grensedato)
                }
            }
        }
    }
}
