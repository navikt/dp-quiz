package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.fakta.deepCopy
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

    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>) = GeneratorSubsumsjon(
        regel.deepCopy(faktaMap),
        setOf(faktum).deepCopy(faktaMap).first() as GeneratorFaktum,
        makro.deepCopy(faktaMap) as MakroSubsumsjon
    )

    override fun deepCopy() = GeneratorSubsumsjon(
        regel,
        faktum,
        makro.deepCopy() as MakroSubsumsjon
    )

    override fun deepCopy(indeks: Int) = GeneratorSubsumsjon(
        regel.deepCopy(indeks),
        setOf(faktum).deepCopy(indeks).first() as GeneratorFaktum,
        makro.deepCopy(indeks) as MakroSubsumsjon
    )
}
