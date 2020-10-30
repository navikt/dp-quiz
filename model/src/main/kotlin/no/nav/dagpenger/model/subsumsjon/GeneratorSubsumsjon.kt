package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum.Companion.deepCopy
import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.søknad.Faktagrupper

class GeneratorSubsumsjon internal constructor(
    regel: Regel,
    private val faktum: GeneratorFaktum,
    private val makro: MakroSubsumsjon
) : EnkelSubsumsjon(regel, setOf(faktum), TomSubsumsjon, AlleSubsumsjon(faktum.navn, mutableListOf())) {

    override fun lokaltResultat(): Boolean? {
        return super.lokaltResultat().also { resultat ->
            when (resultat) {
                false -> {
                    (ugyldig as AlleSubsumsjon).also { alleSubsumsjon ->
                        if (alleSubsumsjon.isEmpty())
                            alleSubsumsjon.addAll((1..faktum.svar()).map { makro.deepCopy(it, faktum.fakta) })
                    }
                }
                else -> {
                    (ugyldig as AlleSubsumsjon).clear()
                }
            }
        }
    }

    override fun deepCopy(faktagrupper: Faktagrupper) = GeneratorSubsumsjon(
        regel.deepCopy(faktagrupper),
        setOf(faktum).deepCopy(faktagrupper).first() as GeneratorFaktum,
        makro.deepCopy(faktagrupper)
    )

    override fun bygg(fakta: Fakta) = GeneratorSubsumsjon(
        regel.bygg(fakta),
        fakta.id(faktum.faktumId) as GeneratorFaktum,
        makro.bygg(fakta) as MakroSubsumsjon
    )

    override fun deepCopy() = GeneratorSubsumsjon(
        regel,
        faktum,
        makro.deepCopy() as MakroSubsumsjon
    )

    override fun deepCopy(indeks: Int, fakta: Fakta) = GeneratorSubsumsjon(
        regel.deepCopy(indeks, fakta),
        setOf(faktum).deepCopy(indeks, fakta).first() as GeneratorFaktum,
        makro.deepCopy(indeks, fakta) as MakroSubsumsjon
    )
}
