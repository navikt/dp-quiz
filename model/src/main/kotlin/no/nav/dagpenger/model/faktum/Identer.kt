package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.visitor.IdentVisitor

class Identer(private val identer: MutableSet<Ident> = mutableSetOf()) : Iterable<Identer.Ident> by identer {

    fun folkeregisterIdent(id: String) = this.also {
        identer.add(Ident(Ident.Type.FOLKEREGISTERIDENT, id, false))
    }

    fun aktørId(id: String) = this.also { identer.add(Ident(Ident.Type.AKTØRID, id, false)) }

    fun folkeregisterIdent() = identer.first { it.type == Ident.Type.FOLKEREGISTERIDENT }.id

    override fun equals(other: Any?): Boolean {
        return (this === other) || other is Identer && equals(other)
    }

    private fun equals(other: Identer): Boolean {
        return this.identer == other.identer
    }

    override fun hashCode(): Int {
        return identer.hashCode()
    }

    data class Ident(val type: Type, val id: String, val historisk: Boolean) {
        enum class Type {
            FOLKEREGISTERIDENT,
            AKTØRID
        }

        fun accept(visitor: IdentVisitor) {
            visitor.visit(type, id, historisk)
        }
    }
}
