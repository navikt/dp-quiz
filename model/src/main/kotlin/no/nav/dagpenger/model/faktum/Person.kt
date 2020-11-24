package no.nav.dagpenger.model.faktum

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
}
