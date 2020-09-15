package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

abstract class Subsumsjon(internal val navn: String) : Iterable<Subsumsjon> {
    protected var gyldigSubsumsjon: Subsumsjon = TomSubsumsjon
    protected var ugyldigSubsumsjon: Subsumsjon = TomSubsumsjon

    open fun resultat(): Boolean? = when (_resultat()) {
        true -> if (gyldig is TomSubsumsjon) true else gyldig.resultat()
        false -> if (ugyldig is TomSubsumsjon) false else ugyldig.resultat()
        null -> null
    }

    internal abstract fun _resultat(): Boolean?

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

    internal abstract fun konkluder(): Boolean

    internal abstract fun fakta(): Set<GrunnleggendeFaktum<*>>

    internal abstract operator fun get(indeks: Int): Subsumsjon

    internal abstract fun nesteFakta(): Set<GrunnleggendeFaktum<*>>

    internal val gyldig get() = gyldigSubsumsjon

    internal fun gyldig(child: Subsumsjon) {
        this.gyldigSubsumsjon = child
    }

    internal val ugyldig get() = ugyldigSubsumsjon

    internal fun ugyldig(child: Subsumsjon) {
        this.ugyldigSubsumsjon = child
    }
}

fun String.alle(vararg subsumsjoner: Subsumsjon, handling: Handling = Handling { _, _ -> }  ): Subsumsjon {
    return AlleSubsumsjon(this, subsumsjoner.toList(), handling)
}

fun interface Handling {
    fun kjør(it: Subsumsjon, resultat: Boolean?)
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
