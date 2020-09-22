package no.nav.dagpenger.model.fakta

class FaktumNavn(internal val id: Int, val navn: String) {
    override fun toString() = "$navn med id $id"
    override fun equals(other: Any?) = other is FaktumNavn && this.id == other.id
    override fun hashCode() = id.hashCode()
}
