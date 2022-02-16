package no.nav.dagpenger.model.faktum

import java.time.LocalDate
import java.time.LocalDateTime

class Dokument(private val lastOppTidsstempel: LocalDateTime, private val urn: String) : Comparable<Dokument> {
    override fun compareTo(other: Dokument): Int {
        return this.lastOppTidsstempel.compareTo(other.lastOppTidsstempel)
    }

    internal constructor(opplastingsdato: LocalDate, urn: String) : this(opplastingsdato.atStartOfDay(), urn)

    fun <R> reflection(block: (LocalDateTime, String) -> R) = block(
        lastOppTidsstempel,
        urn
    )

    override fun equals(other: Any?) = other is Dokument && this.equals(other)

    private fun equals(other: Dokument) = lastOppTidsstempel == other.lastOppTidsstempel && urn == other.urn
}
