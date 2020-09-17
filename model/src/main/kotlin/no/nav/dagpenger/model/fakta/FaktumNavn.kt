package no.nav.dagpenger.model.fakta

class FaktumNavn(private val id: Int, val navn: String) {
    override fun toString() = navn
    override fun equals(other: Any?) = other is FaktumNavn && this.id == other.id
    override fun hashCode() = id.hashCode()
}
