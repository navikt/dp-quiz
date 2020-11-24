package no.nav.dagpenger.model.faktum

class Person private constructor(
    internal val fnr: String,
    private val identer: Identer
) {

    companion object {
        internal val prototype = Person("", "")
    }
    constructor(
        fnr: String,
        aktørId: String
    ) : this(fnr, Identer().folkeregisterIdent(fnr).aktørId(aktørId))

    override fun equals(other: Any?) =
        other is Person && this.identer == other.identer

    override fun hashCode() = identer.hashCode()
}
