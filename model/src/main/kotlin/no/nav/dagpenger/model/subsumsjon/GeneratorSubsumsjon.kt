package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum.Companion.deepCopy
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Utredningsprosess
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
                            sammensattSubsumsjon.addAll((1..faktum.svar()).map { deltre.deepCopy(it, faktum.fakta) })
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

    override fun deepCopy(utredningsprosess: Utredningsprosess) = GeneratorSubsumsjon(
        regel,
        listOf(faktum).deepCopy(utredningsprosess).first() as GeneratorFaktum,
        deltre.deepCopy(utredningsprosess),
        resultatSubsumsjon.deepCopy(utredningsprosess) as SammensattSubsumsjon

    )

    override fun bygg(fakta: Fakta) = GeneratorSubsumsjon(
        regel,
        fakta.id(faktum.faktumId) as GeneratorFaktum,
        deltre.bygg(fakta),
        resultatSubsumsjon.bygg(fakta) as SammensattSubsumsjon
    )

    override fun deepCopy() = GeneratorSubsumsjon(
        regel,
        faktum,
        deltre.deepCopy() as DeltreSubsumsjon,
        resultatSubsumsjon.deepCopy() as SammensattSubsumsjon
    )

    override fun deepCopy(indeks: Int, fakta: Fakta) = GeneratorSubsumsjon(
        regel,
        listOf(faktum).deepCopy(indeks, fakta).first() as GeneratorFaktum,
        deltre.deepCopy(indeks, fakta) as DeltreSubsumsjon,
        resultatSubsumsjon.deepCopy(indeks, fakta) as SammensattSubsumsjon

    )
}
