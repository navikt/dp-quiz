package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.port.Inntekt
import no.nav.dagpenger.qamodel.regel.DatoEtterRegel
import no.nav.dagpenger.qamodel.regel.InntektMinstRegel
import no.nav.dagpenger.qamodel.visitor.SubsumsjonVisitor
import java.time.LocalDate

interface Subsumsjon {
    var gyldigSubsumsjon: Subsumsjon
    val navn: String

    fun konkluder(): Boolean
    fun accept(visitor: SubsumsjonVisitor) {}
    fun fakta(): Set<Faktum<*>>
    fun gyldig(child: Subsumsjon) {
        this.gyldigSubsumsjon = child
    }

    fun nesteFakta(): Set<Faktum<*>>

    fun acceptGyldig(visitor: SubsumsjonVisitor) {
        visitor.preVisitGyldig(this, gyldigSubsumsjon)
        gyldigSubsumsjon.accept(visitor)
        visitor.postVisitGyldig(this, gyldigSubsumsjon)
    }
}

fun String.alle(vararg subsumsjoner: Subsumsjon): Subsumsjon {
    return AlleSubsumsjon(this, subsumsjoner.toList())
}

fun String.minstEnAv(vararg subsumsjoner: Subsumsjon): Subsumsjon {
    return MinstEnAvSubsumsjon(this, subsumsjoner.toList())
}

infix fun Faktum<LocalDate>.etter(other: Faktum<LocalDate>): Subsumsjon {
    return EnkelSubsumsjon(DatoEtterRegel, this, other)
}

infix fun Faktum<LocalDate>.før(other: Faktum<LocalDate>): Subsumsjon {
    return EnkelSubsumsjon(DatoEtterRegel, this, other)
}

infix fun Faktum<LocalDate>.ikkeFør(other: Faktum<LocalDate>): Subsumsjon {
    return EnkelSubsumsjon(DatoEtterRegel, this, other)
}

infix fun Faktum<Inntekt>.minst(other: Faktum<Inntekt>): Subsumsjon {
    return EnkelSubsumsjon(InntektMinstRegel, this, other)
}

infix fun Subsumsjon.så(child: Subsumsjon): Subsumsjon {
    return this.also { this.gyldig(child) }
}
