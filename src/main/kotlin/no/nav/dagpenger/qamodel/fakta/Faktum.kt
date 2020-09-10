package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.visitor.SubsumsjonVisitor

class Faktum<R : Any>(internal val navn: String) {
    private var tilstand: Tilstand = Ukjent
    private lateinit var gjeldendeSvar: R

    fun besvar(r: R) = this.apply {
        gjeldendeSvar = r
        tilstand = Kjent
    }

    fun svar() = tilstand.svar(this)

    internal fun accept(visitor: SubsumsjonVisitor) {
        tilstand.accept(this, visitor)
    }

    fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<Faktum<*>>) {
        if (tilstand.kode == kode) fakta.add(this)
    }

    enum class FaktumTilstand {
        Ukjent,
        Kjent
    }

    private interface Tilstand {
        val kode: FaktumTilstand

        fun <R : Any> accept(faktum: Faktum<R>, visitor: SubsumsjonVisitor)
        fun <R : Any> svar(faktum: Faktum<R>): R = throw IllegalStateException("Faktumet er ikke kjent enda")
    }

    private object Ukjent : Tilstand {
        override val kode = FaktumTilstand.Ukjent
        override fun <R : Any> accept(faktum: Faktum<R>, visitor: SubsumsjonVisitor) {
            visitor.visit(faktum, kode)
        }
    }

    private object Kjent : Tilstand {
        override val kode = FaktumTilstand.Kjent
        override fun <R : Any> accept(faktum: Faktum<R>, visitor: SubsumsjonVisitor) {
            visitor.visit(faktum, Ukjent.kode, faktum.gjeldendeSvar)
        }

        override fun <R : Any> svar(faktum: Faktum<R>) = faktum.gjeldendeSvar
    }
}
