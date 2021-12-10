package no.nav.dagpenger.model.faktum

import java.time.LocalDate
import java.time.LocalDateTime

// @todo: url i konstruktør burde være typet og ikke pre ufyllt?
class Dokument(private val lastOppTidsstempel: LocalDateTime, private val url: String = "http:") : Comparable<Dokument> {
    override fun compareTo(other: Dokument): Int {
        return this.lastOppTidsstempel.compareTo(other.lastOppTidsstempel)
    }

    internal constructor(opplastingsdato: LocalDate) : this(opplastingsdato.atStartOfDay())

    private fun toUrl() = url

    fun <R> reflection(block: (LocalDateTime, String) -> R) = block(
        lastOppTidsstempel,
        toUrl()
    )
    override fun equals(other: Any?) = other is Dokument && this.equals(other)

    private fun equals(other: Dokument) = lastOppTidsstempel == other.lastOppTidsstempel && url == other.url

    override fun toString(): String {
        return "Dokument(lastOppTidsstempel=$lastOppTidsstempel, url='$url')"
    }
}
