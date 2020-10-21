package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class MakroSubsumsjon private constructor(
    navn: String,
    private val child: Subsumsjon,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, mutableListOf(child), gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(navn: String, child: Subsumsjon) : this(navn, child, TomSubsumsjon, TomSubsumsjon)

    override fun deepCopy(søknad: Søknad) = MakroSubsumsjon(
        navn,
        child.deepCopy(søknad),
        gyldigSubsumsjon.deepCopy(søknad),
        ugyldigSubsumsjon.deepCopy(søknad)
    ).also {
        it.søknad = søknad
    }

    override fun bygg(fakta: Fakta) = MakroSubsumsjon(
        navn,
        child.bygg(fakta),
        gyldigSubsumsjon.bygg(fakta),
        ugyldigSubsumsjon.bygg(fakta)
    )

    override fun deepCopy(): Subsumsjon {
        return MakroSubsumsjon(
            navn,
            child.deepCopy(),
            gyldigSubsumsjon.deepCopy(),
            ugyldigSubsumsjon.deepCopy()
        )
    }

    override fun deepCopy(indeks: Int): Subsumsjon {
        return MakroSubsumsjon(
            "$navn [$indeks]",
            child.deepCopy(indeks),
            gyldigSubsumsjon.deepCopy(indeks),
            ugyldigSubsumsjon.deepCopy(indeks)
        ).also {
            it.søknad = søknad
        }
    }

    override fun accept(visitor: SubsumsjonVisitor) {
        resultat().also {
            visitor.preVisit(this, it)
            super.accept(visitor)
            visitor.postVisit(this, it)
        }
    }

    override fun lokaltResultat() = child.resultat()
}
