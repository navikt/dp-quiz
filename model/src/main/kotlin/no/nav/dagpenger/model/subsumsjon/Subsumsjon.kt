package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

abstract class Subsumsjon protected constructor(
    internal val navn: String,
    gyldigSubsumsjon: Subsumsjon?,
    ugyldigSubsumsjon: Subsumsjon?
) : Iterable<Subsumsjon> {
    protected lateinit var gyldigSubsumsjon: Subsumsjon
    protected lateinit var ugyldigSubsumsjon: Subsumsjon

    init {
        if (gyldigSubsumsjon != null) this.gyldigSubsumsjon = gyldigSubsumsjon
        if (ugyldigSubsumsjon != null) this.ugyldigSubsumsjon = ugyldigSubsumsjon
    }

    internal constructor(navn: String) : this(navn, TomSubsumsjon, TomSubsumsjon)

    open fun resultat(): Boolean? = when (lokaltResultat()) {
        true -> if (gyldig is TomSubsumsjon) true else gyldig.resultat()
        false -> if (ugyldig is TomSubsumsjon) false else ugyldig.resultat()
        null -> null
    }

    abstract fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>): Subsumsjon

    internal abstract fun lokaltResultat(): Boolean?

    abstract fun nesteFakta(): Set<GrunnleggendeFaktum<*>>

    abstract fun enkelSubsumsjoner(vararg fakta: Faktum<*>): List<EnkelSubsumsjon>

    fun sti(subsumsjon: Subsumsjon): List<Subsumsjon> =
        if (subsumsjon !is TomSubsumsjon) _sti(subsumsjon) else throw IndexOutOfBoundsException()

    internal abstract fun _sti(subsumsjon: Subsumsjon): List<Subsumsjon>

    internal open fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisitGyldig(this, gyldigSubsumsjon)
        gyldigSubsumsjon.accept(visitor)
        visitor.postVisitGyldig(this, gyldigSubsumsjon)

        visitor.preVisitUgyldig(this, ugyldigSubsumsjon)
        ugyldigSubsumsjon.accept(visitor)
        visitor.postVisitUgyldig(this, ugyldigSubsumsjon)
    }

    internal abstract fun fakta(): Set<Faktum<*>>

    internal abstract operator fun get(indeks: Int): Subsumsjon

    internal val gyldig get() = gyldigSubsumsjon

    internal fun gyldig(child: Subsumsjon) {
        this.gyldigSubsumsjon = child
    }

    internal val ugyldig get() = ugyldigSubsumsjon

    internal fun ugyldig(child: Subsumsjon) {
        this.ugyldigSubsumsjon = child
    }
}

fun String.alle(vararg subsumsjoner: Subsumsjon): Subsumsjon {
    return AlleSubsumsjon(this, subsumsjoner.toList())
}

fun String.minstEnAv(vararg subsumsjoner: Subsumsjon): Subsumsjon {
    return MinstEnAvSubsumsjon(this, subsumsjoner.toList())
}

infix fun Subsumsjon.så(child: Subsumsjon): Subsumsjon {
    return this.also { this.gyldig(child) }
}

infix fun Subsumsjon.eller(child: Subsumsjon): Subsumsjon {
    return this.also { this.ugyldig(child) }
}

infix fun String.makro(child: Subsumsjon) = MakroSubsumsjon(this, child)
