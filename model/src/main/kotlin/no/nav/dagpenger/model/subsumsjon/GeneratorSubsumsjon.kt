package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Faktum.Companion.deepCopy
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Søknadprosess

class GeneratorSubsumsjon internal constructor(
    regel: Regel,
    private val faktum: GeneratorFaktum,
    private val makro: MakroSubsumsjon,
    private val resultatSubsumsjon: SammensattSubsumsjon
) : EnkelSubsumsjon(regel, listOf(faktum), TomSubsumsjon, resultatSubsumsjon) {

    override fun lokaltResultat(): Boolean? {
        return super.lokaltResultat().also { resultat ->
            when (resultat) {
                false -> {
                    (ugyldig as SammensattSubsumsjon).also { sammensattSubsumsjon ->
                        if (sammensattSubsumsjon.size != faktum.svar()) sammensattSubsumsjon.clear()
                        if (sammensattSubsumsjon.isEmpty())
                            sammensattSubsumsjon.addAll((1..faktum.svar()).map { makro.deepCopy(it, faktum.søknad) })
                    }
                }
                else -> {
                    (ugyldig as SammensattSubsumsjon).clear()
                }
            }
        }
    }

    override fun deepCopy(søknadprosess: Søknadprosess) = GeneratorSubsumsjon(
        regel,
        listOf(faktum).deepCopy(søknadprosess).first() as GeneratorFaktum,
        makro.deepCopy(søknadprosess),
        resultatSubsumsjon.deepCopy(søknadprosess) as SammensattSubsumsjon

    )

    override fun bygg(søknad: Søknad) = GeneratorSubsumsjon(
        regel,
        søknad.id(faktum.faktumId) as GeneratorFaktum,
        makro.bygg(søknad),
        resultatSubsumsjon.bygg(søknad) as SammensattSubsumsjon
    )

    override fun deepCopy() = GeneratorSubsumsjon(
        regel,
        faktum,
        makro.deepCopy() as MakroSubsumsjon,
        resultatSubsumsjon.deepCopy() as SammensattSubsumsjon
    )

    override fun deepCopy(indeks: Int, søknad: Søknad) = GeneratorSubsumsjon(
        regel,
        listOf(faktum).deepCopy(indeks, søknad).first() as GeneratorFaktum,
        makro.deepCopy(indeks, søknad) as MakroSubsumsjon,
        resultatSubsumsjon.deepCopy(indeks, søknad) as SammensattSubsumsjon

    )
}
