package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class SannsynliggjøringsSubsumsjon private constructor(
    navn: String,
    private val child: Subsumsjon,
    private val sannsynliggjøringsFakta: Collection<Faktum<*>>
) : SammensattSubsumsjon(navn, mutableListOf(child), TomSubsumsjon, TomSubsumsjon) {
    internal constructor(child: Subsumsjon, sannsynliggjøringsFakta: Collection<Faktum<*>>) :
        this(
            "${child.navn} sannsynligjøring",
            child,
            sannsynliggjøringsFakta
        )

    override fun lokaltResultat(): Boolean? = child.resultat()

    override fun accept(visitor: SubsumsjonVisitor) {
        lokaltResultat().also { subsumsjon ->
            sannsynliggjøringsFakta.forEach { sannsynliggjøringsFaktum ->
                visitor.preVisit(this, sannsynliggjøringsFaktum as GrunnleggendeFaktum<*>, subsumsjon)
            }
            super.accept(visitor)
            sannsynliggjøringsFakta.forEach { sannsynliggjøringsFaktum -> sannsynliggjøringsFaktum.accept(visitor) }

            sannsynliggjøringsFakta.forEach { sannsynliggjøringsFaktum ->
                visitor.postVisit(this, sannsynliggjøringsFaktum as GrunnleggendeFaktum<*>, subsumsjon)
            }
        }
    }

    override fun deepCopy(): Subsumsjon {
        return SannsynliggjøringsSubsumsjon(
            navn,
            child.deepCopy(),
            sannsynliggjøringsFakta
        )
    }

    override fun deepCopy(indeks: Int, fakta: Fakta): Subsumsjon {
        return SannsynliggjøringsSubsumsjon(
            "$navn [$indeks]",
            child.deepCopy(indeks, fakta),
            sannsynliggjøringsFakta.map { it.deepCopy(indeks, fakta) }.toSet()
        )
    }

    override fun bygg(fakta: Fakta) = SannsynliggjøringsSubsumsjon(
        navn,
        child.bygg(fakta),
        sannsynliggjøringsFakta.map { faktum -> fakta.dokument(faktum.id) }.toSet()
    )

    override fun deepCopy(faktagrupper: Faktagrupper) = SannsynliggjøringsSubsumsjon(
        navn,
        child.deepCopy(faktagrupper),
        sannsynliggjøringsFakta.map { faktum -> faktagrupper.dokument(faktum.id) }.toSet()
    )
}
