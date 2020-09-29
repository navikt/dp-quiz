package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.regel.Regel

class GeneratorSubsumsjon internal constructor(
    regel: Regel,
    private val faktum: GeneratorFaktum,
    private val makro: MakroSubsumsjon
) : EnkelSubsumsjon(regel, setOf(faktum), TomSubsumsjon, AlleSubsumsjon(faktum.navn.navn, mutableListOf())) {

    override fun lokaltResultat(): Boolean? {
        return super.lokaltResultat().also {
            when (it) {
                false -> { (ugyldig as AlleSubsumsjon).addAll((1..faktum.svar()).map { makro.deepCopy(it) }) }
                else -> { (ugyldig as AlleSubsumsjon).clear() }
            }
        }
    }
}
