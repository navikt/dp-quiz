package no.nav.dagpenger.model.faktum

import java.time.LocalDate

class Periode(
    val fom: LocalDate,
    val tom: LocalDate?
) : Comparable<Periode> {

    constructor(fom: LocalDate) : this(fom, null)

    init {
        if (tom != null) {
            require(fom <= tom) { "Fradato må være tidligere enn tildato, fant fom=$fom og tom=$tom" }
        }
    }

    override fun compareTo(other: Periode): Int {
        TODO("Not yet implemented")
    }

    fun erPågående() = tom == null
}
