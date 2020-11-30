package no.nav.dagpenger.model.faktum

import java.time.LocalDate

class Periode(private val fom: LocalDate, private val tom: LocalDate) : ClosedRange<LocalDate>, Comparable<Periode> {

    init {
        require(fom < tom) { "Fra og med dato kan ikke være etter til og med dato" }
    }

    internal infix fun inneholder(kandidat: LocalDate) = kandidat in fom..tom

    override fun equals(other: Any?): Boolean = other is Periode && other.fom == this.fom && other.tom == this.tom
    override operator fun compareTo(other: Periode): Int =
        other.start.compareTo(this.start) + other.endInclusive.compareTo(this.endInclusive)

    override val endInclusive: LocalDate
        get() = tom
    override val start: LocalDate
        get() = fom

    override fun toString() = "$fom  til og med $tom"
}

internal infix fun LocalDate.i(periode: Periode) = periode.inneholder(this)
infix fun LocalDate.til(tom: LocalDate) = Periode(this, tom)
