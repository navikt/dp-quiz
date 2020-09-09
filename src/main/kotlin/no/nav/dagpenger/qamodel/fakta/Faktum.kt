package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.visitor.FaktumVisitor
import no.nav.dagpenger.qamodel.visitor.PrettyPrint

class Faktum<R: Any>(internal val navn: String) {
    private var tilstand: Tilstand = Ukjent
    private lateinit var gjeldendeSvar: R

    fun besvar(r: R) = this.apply{
        gjeldendeSvar = r
        tilstand = Kjent
    }

    fun svar() = tilstand.svar(this)

    override fun toString() = PrettyPrint(this).result()

    internal fun accept(visitor: FaktumVisitor) {
    }

    enum class FaktumTilstand {
        Ukjent,
        Kjent
    }

    private interface Tilstand {
        val kode: FaktumTilstand

        fun <R: Any> svar(faktum: Faktum<R>): R = throw IllegalStateException("Faktumet er ikke kjent enda")
    }

    private object Ukjent : Tilstand {
        override val kode = FaktumTilstand.Ukjent
    }

    private object Kjent : Tilstand {
        override val kode = FaktumTilstand.Kjent
        override fun <R: Any> svar(faktum: Faktum<R>) = faktum.gjeldendeSvar
    }
}
