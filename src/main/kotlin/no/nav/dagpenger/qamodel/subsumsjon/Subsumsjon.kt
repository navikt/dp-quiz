package no.nav.dagpenger.qamodel.subsumsjon

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.regel.DatoEtterRegel
import no.nav.dagpenger.qamodel.visitor.SubsumsjonVisitor
import java.time.LocalDate

interface Subsumsjon {
    fun konkluder(): Boolean
    fun accept(visitor: SubsumsjonVisitor) {}
    fun fakta(): Set<Faktum<*>>
}

fun String.alle(vararg subsumsjoner: Subsumsjon): Subsumsjon {
    return AllSubsumsjon(this, subsumsjoner.toList())
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
