package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.handling.Handling
import java.lang.IllegalStateException

class Faktum<R> (navn: String, private val strategi: SpørsmålStrategi<R>) {
    private var tilstand: Tilstand = Inaktivt
    private lateinit var gjeldendeSvar: Svar

    fun svar() = tilstand.svar(this)

    fun besvar(r: R) = tilstand.besvar(r, this)
    fun spør() = tilstand.spør(this)
    internal fun accept(visitor: FaktumVisitor){
        visitor.preVisit(this)
        strategi.accept(visitor)
        visitor.postVisit(this)
    }

    private fun _besvar(r: R) = strategi.besvar(r, this).also {
        gjeldendeSvar = it
        tilstand = Kjent
    }

    private interface Tilstand {
        fun <R> svar(faktum: Faktum<R>): Svar

        fun <R> besvar(r: R, faktum: Faktum<R>) = faktum._besvar(r)

        fun <R> spør(faktum: Faktum<R>): Faktum<R> = throw IllegalStateException("Spørsmålet er allerede spurt")
    }

    private object Inaktivt : Tilstand {
        override fun <R> svar(faktum: Faktum<R>) = Ubesvart(faktum)

        override fun <R> besvar(r: R, faktum: Faktum<R>) = throw IllegalStateException("Spørsmålet er ikke aktivt")

        override fun <R> spør(faktum: Faktum<R>) = faktum.apply {
            tilstand = Ukjent
        }
    }

    private object Ukjent : Tilstand {
        override fun <R> svar(faktum: Faktum<R>) = Ubesvart(faktum)
    }

    private object Kjent : Tilstand {
        override fun <R> svar(faktum: Faktum<R>) = faktum.gjeldendeSvar
    }
}

interface SpørsmålStrategi<R> {
    fun besvar(r: R, faktum: Faktum<R>): Svar
    fun accept(visitor: FaktumVisitor)
}

interface FaktumVisitor{
    fun preVisit(faktum : Faktum<*>)
    fun postVisit(faktum : Faktum<*>)
    fun preVisit(strategi: SpørsmålStrategi<*>)
    fun postVisit(strategi: SpørsmålStrategi<*>)
    fun preVisit(handling : Handling)
    fun postVisit(handling : Handling)
}
