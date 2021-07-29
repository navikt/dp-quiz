package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Faktum.Companion.deepCopy
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class GeneratorSubsumsjon internal constructor(
    regel: Regel,
    private val faktum: GeneratorFaktum,
    private val deltre: DeltreSubsumsjon,
    private val resultatSubsumsjon: SammensattSubsumsjon
) : EnkelSubsumsjon(regel, listOf(faktum), resultatSubsumsjon, TomSubsumsjon) {

    override fun lokaltResultat(): Boolean? {
        return super.lokaltResultat().also { resultat ->
            when (resultat) {
                true -> {
                    (oppfylt as SammensattSubsumsjon).also { sammensattSubsumsjon ->
                        if (sammensattSubsumsjon.size != faktum.svar()) sammensattSubsumsjon.clear()
                        if (sammensattSubsumsjon.isEmpty())
                            sammensattSubsumsjon.addAll((1..faktum.svar()).map { deltre.deepCopy(it, faktum.søknad) })
                    }
                }
                else -> {
                    (oppfylt as SammensattSubsumsjon).clear()
                }
            }
        }
    }

    override fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisit(this, deltre)
        super.accept(visitor)
        visitor.postVisit(this, deltre)
    }

    override fun deepCopy(søknadprosess: Søknadprosess) = GeneratorSubsumsjon(
        regel,
        listOf(faktum).deepCopy(søknadprosess).first() as GeneratorFaktum,
        deltre.deepCopy(søknadprosess),
        resultatSubsumsjon.deepCopy(søknadprosess) as SammensattSubsumsjon

    )

    override fun bygg(søknad: Søknad) = GeneratorSubsumsjon(
        regel,
        søknad.id(faktum.faktumId) as GeneratorFaktum,
        deltre.bygg(søknad),
        resultatSubsumsjon.bygg(søknad) as SammensattSubsumsjon
    )

    override fun deepCopy() = GeneratorSubsumsjon(
        regel,
        faktum,
        deltre.deepCopy() as DeltreSubsumsjon,
        resultatSubsumsjon.deepCopy() as SammensattSubsumsjon
    )

    override fun deepCopy(indeks: Int, søknad: Søknad) = GeneratorSubsumsjon(
        regel,
        listOf(faktum).deepCopy(indeks, søknad).first() as GeneratorFaktum,
        deltre.deepCopy(indeks, søknad) as DeltreSubsumsjon,
        resultatSubsumsjon.deepCopy(indeks, søknad) as SammensattSubsumsjon

    )
}
