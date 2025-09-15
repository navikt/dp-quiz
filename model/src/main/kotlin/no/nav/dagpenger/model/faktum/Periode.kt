package no.nav.dagpenger.model.faktum

import java.time.LocalDate

class Periode(
    val fom: LocalDate,
    val tom: LocalDate?,
) : Comparable<Periode> {
    constructor(fom: LocalDate) : this(fom, null)

    init {
        if (tom != null) {
            require(fom <= tom) { "Fradato må være tidligere enn tildato, fant fom=$fom og tom=$tom" }
        }
    }

    fun erPågående() = tom == null

    fun <R> reflection(block: (LocalDate, LocalDate?) -> R) =
        block(
            fom,
            tom,
        )

    override fun compareTo(other: Periode): Int =
        when {
            fom != other.fom -> fom.compareTo(other.fom)
            tom != other.tom -> tom?.compareTo(other.tom) ?: -1
            else -> 0
        }

    override fun equals(other: Any?): Boolean = other is Periode && fom == other.fom && tom == other.tom

    override fun hashCode(): Int = fom.hashCode() * 37 + tom.hashCode()

    override fun toString(): String = "Periode(fom=$fom, tom=$tom)"
}
