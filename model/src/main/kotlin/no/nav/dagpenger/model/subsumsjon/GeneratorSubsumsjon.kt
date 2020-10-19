package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum.Companion.deepCopy
import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.søknad.Søknad

class GeneratorSubsumsjon internal constructor(
    regel: Regel,
    private val faktum: GeneratorFaktum,
    private val makro: MakroSubsumsjon
) : EnkelSubsumsjon(regel, setOf(faktum), TomSubsumsjon, AlleSubsumsjon(faktum.navn, mutableListOf())) {

    override fun lokaltResultat(): Boolean? {
        return super.lokaltResultat().also {
            when (it) {
                false -> { (ugyldig as AlleSubsumsjon).addAll((1..faktum.svar()).map { makro.deepCopy(it) }) }
                else -> { (ugyldig as AlleSubsumsjon).clear() }
            }
        }
    }

    override fun deepCopy(søknad: Søknad) = GeneratorSubsumsjon(
        regel.deepCopy(søknad),
        setOf(faktum).deepCopy(søknad).first() as GeneratorFaktum,
        makro.deepCopy(søknad) as MakroSubsumsjon
    ).also {
        it.søknad = søknad
    }

    override fun deepCopy() = GeneratorSubsumsjon(
        regel,
        faktum,
        makro.deepCopy() as MakroSubsumsjon
    )

    override fun deepCopy(indeks: Int) = GeneratorSubsumsjon(
        regel.deepCopy(indeks, søknad),
        setOf(faktum).deepCopy(indeks, søknad).first() as GeneratorFaktum,
        makro.deepCopy(indeks) as MakroSubsumsjon
    )
}
