package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.søknad.Søknad
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

    abstract fun deepCopy(søknad: Søknad): Subsumsjon

    internal abstract fun bygg(fakta: Fakta): Subsumsjon

    abstract fun deepCopy(): Subsumsjon

    internal abstract fun deepCopy(indeks: Int, fakta: Fakta): Subsumsjon

    internal abstract fun lokaltResultat(): Boolean?

    internal abstract fun nesteFakta(): Set<GrunnleggendeFaktum<*>>

    internal open fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisitGyldig(this, gyldigSubsumsjon)
        gyldigSubsumsjon.accept(visitor)
        visitor.postVisitGyldig(this, gyldigSubsumsjon)

        visitor.preVisitUgyldig(this, ugyldigSubsumsjon)
        ugyldigSubsumsjon.accept(visitor)
        visitor.postVisitUgyldig(this, ugyldigSubsumsjon)
    }

    internal abstract operator fun get(indeks: Int): Subsumsjon

    internal val gyldig get() = gyldigSubsumsjon

    internal fun gyldig(child: Subsumsjon) {
        this.gyldigSubsumsjon = child
    }

    internal val ugyldig get() = ugyldigSubsumsjon

    internal fun ugyldig(child: Subsumsjon) {
        this.ugyldigSubsumsjon = child
    }
    internal fun mulige(): Subsumsjon = this.deepCopy()._mulige()

    internal open fun _mulige(): Subsumsjon = this.also { copy ->
        when (lokaltResultat()) {
            true -> {
                copy.ugyldig(TomSubsumsjon)
                copy.gyldigSubsumsjon._mulige()
            }
            false -> {
                copy.gyldig(TomSubsumsjon)
                copy.ugyldigSubsumsjon._mulige()
            }
            null -> {
                copy.gyldigSubsumsjon._mulige()
                copy.ugyldigSubsumsjon._mulige()
            }
        }
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

infix fun Subsumsjon.uansett(child: Subsumsjon): Subsumsjon {
    return this.also {
        this.gyldig(child)
        this.ugyldig(child)
    }
}

infix fun String.makro(child: Subsumsjon) = MakroSubsumsjon(this, child)
