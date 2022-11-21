package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class SannsynliggjøringsSubsumsjon private constructor(
    navn: String,
    private val child: Subsumsjon,
    private val sannsynliggjøringsFakta: Collection<Faktum<*>>
) : SammensattSubsumsjon(navn, mutableListOf(child), TomSubsumsjon, TomSubsumsjon) {

    init {
        child.alleFakta().forEach { faktum -> faktum.sannsynliggjøresAv(sannsynliggjøringsFakta.toMutableSet()) }
    }

    internal constructor(child: Subsumsjon, sannsynliggjøringsFakta: Set<Faktum<*>>) :
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

    override fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon {
        return SannsynliggjøringsSubsumsjon(
            "$navn [$indeks]",
            child.deepCopy(indeks, søknad),
            sannsynliggjøringsFakta.map { it.deepCopy(indeks, søknad) }.toSet()
        )
    }

    override fun bygg(søknad: Søknad) = SannsynliggjøringsSubsumsjon(
        navn,
        child.bygg(søknad),
        sannsynliggjøringsFakta.map { faktum -> søknad.dokument(faktum.id) }.toSet()
    )

    override fun deepCopy(søknadprosess: Søknadprosess) = SannsynliggjøringsSubsumsjon(
        navn,
        child.deepCopy(søknadprosess),
        sannsynliggjøringsFakta.map { faktum -> søknadprosess.dokument(faktum.id) }.toSet()
    )
}
