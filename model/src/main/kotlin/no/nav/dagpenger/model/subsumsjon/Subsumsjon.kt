package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.faktum.ValgFaktum
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

    abstract fun deepCopy(faktagrupper: Faktagrupper): Subsumsjon

    internal abstract fun bygg(søknad: Søknad): Subsumsjon

    abstract fun deepCopy(): Subsumsjon

    internal abstract fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon

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

    internal fun relevanteFakta() = RelevanteFakta(this).resultater

    private class RelevanteFakta(subsumsjon: Subsumsjon) : SubsumsjonVisitor {
        val resultater = mutableSetOf<Faktum<*>>()
        private var ignore = false
        private var iValg = false

        init {
            subsumsjon.mulige().accept(this)
        }

        override fun preVisit(
            subsumsjon: GodkjenningsSubsumsjon,
            action: GodkjenningsSubsumsjon.Action,
            lokaltResultat: Boolean?
        ) {
            ignore = when (action) {
                GodkjenningsSubsumsjon.Action.JaAction -> lokaltResultat == false
                GodkjenningsSubsumsjon.Action.NeiAction -> lokaltResultat == true
                GodkjenningsSubsumsjon.Action.UansettAction -> false
            }
        }

        override fun postVisit(
            subsumsjon: GodkjenningsSubsumsjon,
            action: GodkjenningsSubsumsjon.Action,
            lokaltResultat: Boolean?
        ) {
            ignore = false
        }

        override fun <R : Comparable<R>> visit(
            faktum: GrunnleggendeFaktum<R>,
            tilstand: Faktum.FaktumTilstand,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>
        ) {
            if (!ignore && !iValg) {
                resultater.add(faktum)
            }
        }

        override fun <R : Comparable<R>> visit(
            faktum: GrunnleggendeFaktum<R>,
            tilstand: Faktum.FaktumTilstand,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            svar: R
        ) {
            if (!ignore) {
                resultater.add(faktum)
            }
        }

        override fun <R : Comparable<R>> preVisit(
            faktum: UtledetFaktum<R>,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            children: Set<Faktum<*>>,
            clazz: Class<R>,
            regel: FaktaRegel<R>
        ) {
            if (!ignore) {
                resultater.add(faktum)
            }
        }

        override fun <R : Comparable<R>> preVisit(
            faktum: UtledetFaktum<R>,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            children: Set<Faktum<*>>,
            clazz: Class<R>,
            regel: FaktaRegel<R>,
            svar: R
        ) {
            if (!ignore) {
                resultater.add(faktum)
            }
        }

        override fun preVisit(
            faktum: ValgFaktum,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            underordnedeJa: Set<Faktum<Boolean>>,
            underordnedeNei: Set<Faktum<Boolean>>,
            clazz: Class<Boolean>
        ) {
            if (!ignore) {
                resultater.add(faktum)
                iValg = true
            }
        }

        override fun preVisit(
            faktum: ValgFaktum,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            underordnedeJa: Set<Faktum<Boolean>>,
            underordnedeNei: Set<Faktum<Boolean>>,
            clazz: Class<Boolean>,
            svar: Boolean
        ) {
            if (!ignore) {
                resultater.add(faktum)
                iValg = true
            }
        }

        override fun postVisit(
            faktum: ValgFaktum,
            id: String,
            underordnedeJa: Set<Faktum<Boolean>>,
            underordnedeNei: Set<Faktum<Boolean>>,
            clazz: Class<Boolean>
        ) {
            iValg = false
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
