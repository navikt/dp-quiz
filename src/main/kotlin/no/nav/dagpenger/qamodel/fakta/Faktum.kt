package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.handling.Handling
import no.nav.dagpenger.qamodel.visitor.FaktumVisitor
import no.nav.dagpenger.qamodel.visitor.PrettyPrint

class Faktum<R>(internal val navn: String, private val strategi: SpørsmålStrategi<R>) {
    private var tilstand: Tilstand = Inaktivt
    private lateinit var gjeldendeHandling: Handling<R>

    fun besvar(r: R) = tilstand.besvar(r, this)
    fun spør() = tilstand.spør(this)

    override fun toString() = PrettyPrint(this).result()

    internal fun accept(visitor: FaktumVisitor) {
        strategi.accept(visitor, this, tilstand.kode)
    }

    private fun _besvar(r: R) = strategi.besvar(r, this).apply {
        gjeldendeHandling = this
        utfør(r)
        nesteSpørsmål()

        tilstand = Kjent
    }

    enum class FaktumTilstand {
        Inaktivt,
        Ukjent,
        Kjent
    }

    private interface Tilstand {
        val kode: FaktumTilstand

        fun <R> besvar(r: R, faktum: Faktum<R>) = faktum._besvar(r)
        fun <R> spør(faktum: Faktum<R>): Faktum<R> = throw IllegalStateException("Spørsmålet er allerede spurt")
    }

    private object Inaktivt : Tilstand {
        override val kode = FaktumTilstand.Inaktivt

        override fun <R> besvar(r: R, faktum: Faktum<R>) = throw IllegalStateException("Spørsmålet er ikke aktivt")
        override fun <R> spør(faktum: Faktum<R>) = faktum.apply {
            tilstand = Ukjent
        }
    }

    private object Ukjent : Tilstand {
        override val kode = FaktumTilstand.Ukjent
    }

    private object Kjent : Tilstand {
        override val kode = FaktumTilstand.Kjent
    }
}

interface SpørsmålStrategi<R> {
    fun besvar(r: R, faktum: Faktum<R>): Handling<R>
    fun accept(visitor: FaktumVisitor, faktum: Faktum<R>, tilstand: Faktum.FaktumTilstand)
}
