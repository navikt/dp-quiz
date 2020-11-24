package no.nav.dagpenger.model.faktum

class Identer() {
    private val identer = mutableSetOf<Ident>()

    fun folkeregisterIdent(id: String) = this.also {
        identer.add(Ident(Ident.IdentType.FOLKEREGISTERIDENT, id, false))
    }

    fun aktørId(id: String) = this.also { identer.add(Ident(Ident.IdentType.AKTØRID, id, false)) }

    fun folkeregisterIdent() = identer.first { it.type == Ident.IdentType.FOLKEREGISTERIDENT }.id

    override fun equals(other: Any?): Boolean {
        return (this === other) || other is Identer && equals(other)
    }

    private fun equals(other: Identer): Boolean {
        return this.identer == other.identer
    }

    override fun hashCode(): Int {
        return identer.hashCode()
    }

    private data class Ident(val type: IdentType, val id: String, val historisk: Boolean) {
        enum class IdentType {
            FOLKEREGISTERIDENT,
            AKTØRID
        }
    }
}
