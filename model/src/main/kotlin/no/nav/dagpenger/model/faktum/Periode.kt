package no.nav.dagpenger.model.faktum

import java.time.LocalDate

class Periode(private val fom: LocalDate, private val tom: LocalDate) {

    init {
        require(fom < tom) { "Fra og med dato kan ikke være etter til og med dat" }
    }

    internal infix fun inneholder(kandidat: LocalDate) = kandidat in fom..tom

    override fun equals(other: Any?): Boolean = other is Periode && other.fom == this.fom && other.tom == this.tom
}

internal infix fun LocalDate.i(periode: Periode) = periode.inneholder(this)
