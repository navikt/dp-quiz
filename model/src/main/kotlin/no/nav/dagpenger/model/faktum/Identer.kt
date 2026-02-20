package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.visitor.IdentVisitor

class Identer(
    private val identer: Set<Ident>,
) : Iterable<Identer.Ident> by identer {
    override fun equals(other: Any?): Boolean = (this === other) || (other is Identer && equals(other))

    private fun equals(other: Identer): Boolean = this.identer == other.identer

    override fun hashCode(): Int = identer.hashCode()

    data class Ident(
        val type: Type,
        val id: String,
        val historisk: Boolean,
    ) {
        enum class Type {
            FOLKEREGISTERIDENT,
            AKTØRID,
            NPID,
        }

        fun accept(visitor: IdentVisitor) {
            visitor.visit(type, id, historisk)
        }
    }

    class Builder {
        val identer = mutableSetOf<Ident>()

        fun folkeregisterIdent(
            id: String,
            historisk: Boolean = false,
        ) = this.also {
            identer.add(Ident(Ident.Type.FOLKEREGISTERIDENT, id, historisk))
        }

        fun aktørId(
            id: String,
            historisk: Boolean = false,
        ) = this.also { identer.add(Ident(Ident.Type.AKTØRID, id, historisk)) }

        fun npId(
            id: String,
            historisk: Boolean = false,
        ) = this.also { identer.add(Ident(Ident.Type.NPID, id, historisk)) }

        fun build() = Identer(identer)
    }
}
