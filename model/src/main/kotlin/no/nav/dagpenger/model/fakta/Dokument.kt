package no.nav.dagpenger.model.fakta

import java.time.LocalDateTime

class Dokument(private val lastOppTidsstempel: LocalDateTime) : Comparable<Dokument> {
    override fun compareTo(other: Dokument): Int {
        return this.lastOppTidsstempel.compareTo(other.lastOppTidsstempel)
    }

    internal fun toUrl() = "http:"

    internal fun <R> reflection(block: (LocalDateTime, String) -> R) = block(
            lastOppTidsstempel,
            toUrl()
    )
}
