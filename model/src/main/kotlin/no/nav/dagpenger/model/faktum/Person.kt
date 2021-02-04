package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.visitor.PersonVisitor
import java.util.UUID

// todo navngi til Søker? Hva er jobben til denne klassen>
class Person(private val uuid: UUID, private val identer: Identer) {
    constructor(identer: Identer) : this(UUID.randomUUID(), identer)

    companion object {
        internal val prototype = Person("", "")
    }

    private constructor(
        fnr: String,
        aktørId: String
    ) : this(Identer.Builder().folkeregisterIdent(fnr).aktørId(aktørId).build())

    override fun equals(other: Any?) =
        other is Person && this.identer == other.identer

    override fun hashCode() = identer.hashCode()

    fun accept(visitor: PersonVisitor) {
        visitor.preVisit(this, uuid)
        this.identer.forEach { it.accept(visitor) }
        visitor.postVisit(this, uuid)
    }
}
