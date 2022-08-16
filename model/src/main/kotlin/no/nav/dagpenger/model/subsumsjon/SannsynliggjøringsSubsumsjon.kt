package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

@Suppress("UNCHECKED_CAST")
class SannsynliggjøringsSubsumsjon private constructor(
    navn: String,
    private val child: Subsumsjon,
    private val sannsynliggjøringsFaktum: Faktum<*>
) : SammensattSubsumsjon(navn, mutableListOf(child), TomSubsumsjon, TomSubsumsjon) {

    init {
        child.alleFakta().forEach { it.sannsynliggjøresAv(mutableSetOf(sannsynliggjøringsFaktum)) }
    }

    internal constructor(
        child: Subsumsjon,
        sannsynliggjøringsFakta: Faktum<*>
    ) :
        this(
            "${child.navn} sannsynligjøring",
            child,
            sannsynliggjøringsFakta
        )

    override fun lokaltResultat(): Boolean? = child.resultat()

    override fun accept(visitor: SubsumsjonVisitor) {
        lokaltResultat().also { subsumsjon ->
            visitor.preVisit(this, sannsynliggjøringsFaktum as GrunnleggendeFaktum<*>, subsumsjon)
            super.accept(visitor)
            sannsynliggjøringsFaktum.accept(visitor)
            visitor.postVisit(this, sannsynliggjøringsFaktum, subsumsjon)
        }
    }

    override fun deepCopy(): Subsumsjon {
        return SannsynliggjøringsSubsumsjon(
            navn,
            child.deepCopy(),
            sannsynliggjøringsFaktum
        )
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon {
        return SannsynliggjøringsSubsumsjon(
            "$navn [$indeks]",
            child.deepCopy(indeks, søknad),
            sannsynliggjøringsFaktum.deepCopy(indeks, søknad)
        )
    }

    override fun bygg(søknad: Søknad) = SannsynliggjøringsSubsumsjon(
        navn,
        child.bygg(søknad),
        søknad.dokument(sannsynliggjøringsFaktum.id)
    )

    override fun deepCopy(søknadprosess: Søknadprosess) = SannsynliggjøringsSubsumsjon(
        navn,
        child.deepCopy(søknadprosess),
        søknadprosess.dokument(sannsynliggjøringsFaktum.id)
    )
}
