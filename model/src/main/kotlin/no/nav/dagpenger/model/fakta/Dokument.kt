package no.nav.dagpenger.model.fakta

import java.time.LocalDate
import java.time.LocalDateTime

class Dokument(private val lastOppTidsstempel: LocalDateTime) : Comparable<Dokument> {
    override fun compareTo(other: Dokument): Int {
        return this.lastOppTidsstempel.compareTo(other.lastOppTidsstempel)
    }

    internal constructor(opplastingsdato: LocalDate) : this(opplastingsdato.atStartOfDay())

    internal fun toUrl() = "http:"

    fun <R> reflection(block: (LocalDateTime, String) -> R) = block(
        lastOppTidsstempel,
        toUrl()
    )
}
