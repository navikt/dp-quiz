package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.visitor.FaktumVisitor

class FaktumId private constructor(private val rootId: Int, private val indeks: Int) {

    constructor(id: Int) : this(id, 0)

    constructor(id: String) : this(id.rootId(), id.indeks())

    init {
        require(rootId > 0) { "Id må være en positiv integer større enn null" }
        require(indeks >= 0) { "Indeks må være en positiv integer større enn null" }
    }

    val id: String get() = if (indeks == 0) rootId.toString() else "$rootId.$indeks"

    override fun toString() = "Id $id"

    internal fun accept(visitor: FaktumVisitor) {
        visitor.visit(this, rootId, indeks)
    }

    override fun equals(other: Any?) = other is FaktumId && this.rootId == other.rootId && this.indeks == other.indeks

    override fun hashCode() = rootId.hashCode() * 37 + indeks.hashCode()

    fun indeks(indeks: Int) = FaktumId(rootId, indeks).also {
        require(this.indeks == 0) { "Kan ikke indeksere et allerede indeksert FaktumNavn, id: $id " }
        require(indeks != 0) { "Indeks må være en positiv integer større enn null" }
    }
}

private fun String.rootId() = "\\d+".toRegex().find(this)?.value?.toInt()
    ?: throw IllegalArgumentException("ugyldig id: $this")
private fun String.indeks(): Int = "\\d+".toRegex().findAll(this).elementAtOrNull(1)?.value?.toInt() ?: 0
