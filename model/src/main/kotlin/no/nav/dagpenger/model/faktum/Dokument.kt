package no.nav.dagpenger.model.faktum

import de.slub.urn.RFC
import de.slub.urn.URN
import de.slub.urn.URNSyntaxError
import java.time.LocalDate
import java.time.LocalDateTime

class Dokument : Comparable<Dokument> {
    private val lastOppTidsstempel: LocalDateTime
    private val urn: URN

    constructor(lastOppTidsstempel: LocalDateTime, urn: URN) {
        require(urn.supports(RFC.RFC_8141)) {
            "Must support ${RFC.RFC_8141}. See ${RFC.RFC_8141.url()}"
        }
        this.lastOppTidsstempel = lastOppTidsstempel
        this.urn = urn
    }

    constructor(lastOppTidsstempel: LocalDateTime, urn: String) {
        this.lastOppTidsstempel = lastOppTidsstempel
        this.urn =
            try {
                URN.rfc8141().parse(urn)
            } catch (e: URNSyntaxError) {
                throw IllegalArgumentException(e.message)
            }
    }

    constructor(lastOppTidsstempel: LocalDate, urn: String) : this(lastOppTidsstempel.atStartOfDay(), urn)

    @JvmName("reflectionUrn")
    fun <R> reflection(block: (LocalDateTime, URN) -> R) =
        block(
            lastOppTidsstempel,
            urn,
        )

    fun <R> reflection(block: (LocalDateTime, String) -> R) =
        block(
            lastOppTidsstempel,
            urn.toString(),
        )

    override fun compareTo(other: Dokument): Int = this.lastOppTidsstempel.compareTo(other.lastOppTidsstempel)

    override fun equals(other: Any?) =
        other is Dokument &&
            this.lastOppTidsstempel == other.lastOppTidsstempel &&
            this.urn == other.urn

    override fun hashCode(): Int {
        var result = lastOppTidsstempel.hashCode()
        result = 31 * result + urn.hashCode()
        return result
    }
}
