package no.nav.dagpenger.model.faktum

import java.time.LocalDate
import java.time.LocalDateTime

class Dokument(private val lastOppTidsstempel: LocalDateTime, private val url: String = "http:") : Comparable<Dokument> {
    override fun compareTo(other: Dokument): Int {
        return this.lastOppTidsstempel.compareTo(other.lastOppTidsstempel)
    }

    internal constructor(opplastingsdato: LocalDate) : this(opplastingsdato.atStartOfDay())

    internal fun toUrl() = url

    fun <R> reflection(block: (LocalDateTime, String) -> R) = block(
        lastOppTidsstempel,
        toUrl()
    )
}
