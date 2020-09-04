package no.nav.dagpenger.qamodel.fakta

import java.lang.IllegalStateException

class Faktum<R> (navn: String, private val strategi: SpørsmålStrategi<R>) {
    private var tilstand: Tilstand = Inaktivt
    private lateinit var gjeldendeSvar: Svar

    fun svar() = tilstand.svar(this)

    fun besvar(r: R) = tilstand.besvar(r, this)
    fun spør() = tilstand.spør(this)

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
}
