package no.nav.dagpenger.model.fakta

class FaktumNavn private constructor(private val rootId: Int, val navn: String, private val indeks: Int) {
    constructor(id: Int, navn: String) : this(id, navn, 0)

    val id: String get() = if (indeks == 0) rootId.toString() else "$rootId.$indeks"

    override fun toString() = "$navn med id $rootId"
    override fun equals(other: Any?) = other is FaktumNavn && this.rootId == other.rootId && this.indeks == other.indeks
    override fun hashCode() = rootId.hashCode() * 37 + indeks.hashCode()
    fun indeks(indeks: Int) = FaktumNavn(this.rootId, "$navn [$indeks]", indeks)
}
