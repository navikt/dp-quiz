package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Faktum.Companion.deepCopy
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Søknadprosess

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
                            alleSubsumsjon.addAll((1..faktum.svar()).map { makro.deepCopy(it, faktum.søknad) })
                    }
                }
                else -> {
                    (ugyldig as AlleSubsumsjon).clear()
                }
            }
        }
    }

    override fun deepCopy(søknadprosess: Søknadprosess) = GeneratorSubsumsjon(
        regel.deepCopy(søknadprosess),
        setOf(faktum).deepCopy(søknadprosess).first() as GeneratorFaktum,
        makro.deepCopy(søknadprosess)
    )

    override fun bygg(søknad: Søknad) = GeneratorSubsumsjon(
        regel.bygg(søknad),
        søknad.id(faktum.faktumId) as GeneratorFaktum,
        makro.bygg(søknad) as MakroSubsumsjon
    )

    override fun deepCopy() = GeneratorSubsumsjon(
        regel,
        faktum,
        makro.deepCopy() as MakroSubsumsjon
    )

    override fun deepCopy(indeks: Int, søknad: Søknad) = GeneratorSubsumsjon(
        regel.deepCopy(indeks, søknad),
        setOf(faktum).deepCopy(indeks, søknad).first() as GeneratorFaktum,
        makro.deepCopy(indeks, søknad) as MakroSubsumsjon
    )
}
