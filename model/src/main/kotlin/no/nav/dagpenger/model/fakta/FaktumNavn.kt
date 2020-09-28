package no.nav.dagpenger.model.fakta

class FaktumNavn internal constructor(internal val id: String, val navn: String) {
    constructor(id: Int, navn: String) : this(id.toString(), navn)

    override fun toString() = "$navn med id $id"
    override fun equals(other: Any?) = other is FaktumNavn && this.id == other.id
    override fun hashCode() = id.hashCode()
}
