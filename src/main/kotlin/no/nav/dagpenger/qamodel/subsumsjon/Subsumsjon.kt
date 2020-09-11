package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.port.Inntekt
import no.nav.dagpenger.qamodel.regel.DatoEtterRegel
import no.nav.dagpenger.qamodel.regel.DatoFørRegel
import no.nav.dagpenger.qamodel.regel.DatoIkkeFørRegel
import no.nav.dagpenger.qamodel.regel.InntektMinstRegel
import no.nav.dagpenger.qamodel.visitor.SubsumsjonVisitor
import java.time.LocalDate

abstract class Subsumsjon(internal val navn: String) : Iterable<Subsumsjon> {
    protected var gyldigSubsumsjon: Subsumsjon = TomSubsumsjon
    protected var ugyldigSubsumsjon: Subsumsjon = TomSubsumsjon

    internal val gyldig get() = gyldigSubsumsjon
    internal val ugyldig get() = ugyldigSubsumsjon

    internal abstract fun konkluder(): Boolean

    internal open fun accept(visitor: SubsumsjonVisitor) {}

    internal abstract fun fakta(): Set<Faktum<*>>

    internal fun gyldig(child: Subsumsjon) {
        this.gyldigSubsumsjon = child
    }

    internal fun ugyldig(child: Subsumsjon) {
        this.ugyldigSubsumsjon = child
    }

    internal abstract operator fun get(indeks: Int): Subsumsjon

    internal abstract fun nesteFakta(): Set<Faktum<*>>

    protected fun acceptGyldig(visitor: SubsumsjonVisitor) {
        visitor.preVisitGyldig(this, gyldigSubsumsjon)
        gyldigSubsumsjon.accept(visitor)
        visitor.postVisitGyldig(this, gyldigSubsumsjon)
    }

    protected fun acceptUgyldig(visitor: SubsumsjonVisitor) {
        visitor.preVisitUgyldig(this, ugyldigSubsumsjon)
        ugyldigSubsumsjon.accept(visitor)
        visitor.postVisitUgyldig(this, ugyldigSubsumsjon)
    }

    abstract fun subsumsjoner(vararg fakta: Faktum<*>): List<EnkelSubsumsjon>
    internal abstract fun _sti(subsumsjon: Subsumsjon): List<Subsumsjon>
    fun sti(subsumsjon: Subsumsjon): List<Subsumsjon> =
        if (subsumsjon !is TomSubsumsjon) _sti(subsumsjon) else throw IndexOutOfBoundsException()
}

fun String.alle(vararg subsumsjoner: Subsumsjon): Subsumsjon {
    return AlleSubsumsjon(this, subsumsjoner.toList())
}

fun String.minstEnAv(vararg subsumsjoner: Subsumsjon): Subsumsjon {
    return MinstEnAvSubsumsjon(this, subsumsjoner.toList())
}

infix fun Faktum<LocalDate>.etter(other: Faktum<LocalDate>): Subsumsjon {
    return EnkelSubsumsjon(DatoEtterRegel(this, other), this, other)
}

infix fun Faktum<LocalDate>.før(other: Faktum<LocalDate>): Subsumsjon {
    return EnkelSubsumsjon(DatoFørRegel(this, other), this, other)
}

infix fun Faktum<LocalDate>.ikkeFør(other: Faktum<LocalDate>): Subsumsjon {
    return EnkelSubsumsjon(DatoIkkeFørRegel(this, other), this, other)
}

infix fun Faktum<Inntekt>.minst(other: Faktum<Inntekt>): Subsumsjon {
    return EnkelSubsumsjon(InntektMinstRegel(this, other), this, other)
}

infix fun Subsumsjon.så(child: Subsumsjon): Subsumsjon {
    return this.also { this.gyldig(child) }
}

infix fun Subsumsjon.eller(child: Subsumsjon): Subsumsjon {
    return this.also { this.ugyldig(child) }
}
