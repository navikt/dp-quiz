package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.visitor.PersonVisitor

class Person(private val identer: Identer) {

    internal val fnr = identer.folkeregisterIdent()

    companion object {
        internal val prototype = Person("", "")
    }
    private constructor(
        fnr: String,
        aktørId: String
    ) : this(Identer().folkeregisterIdent(fnr).aktørId(aktørId))

    override fun equals(other: Any?) =
        other is Person && this.identer == other.identer

    override fun hashCode() = identer.hashCode()

    fun accept(visitor: PersonVisitor) {
        visitor.preVisit(this)
        this.identer.forEach { it.accept(visitor) }
        visitor.postVisit(this)
    }
}
