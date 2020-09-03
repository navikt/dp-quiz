package no.nav.dagpenger.qamodel

import java.lang.IllegalStateException

class Fakta<R> (navn: String, private val strategi: SpørsmålStrategi<R>) {
    private var tilstand: Tilstand = Inaktivt
    private lateinit var gjeldendeSvar: Svar

    fun svar() = tilstand.svar(this)

    fun besvar(r: R) = tilstand.besvar(r, this)
    fun spør() = tilstand.spør(this)

    private fun _besvar(r: R) = strategi.besvar(r, this).also {
        gjeldendeSvar = it
        tilstand = Besvart
    }

    private interface Tilstand {
        fun <R> svar(fakta: Fakta<R>): Svar

        fun <R> besvar(r: R, fakta: Fakta<R>) = fakta._besvar(r)

        fun <R> spør(fakta: Fakta<R>): Fakta<R> = throw IllegalStateException("Spørsmålet er allerede spurt")
    }

    private object Inaktivt : Tilstand {
        override fun <R> svar(fakta: Fakta<R>) = Ubesvart(fakta)

        override fun <R> besvar(r: R, fakta: Fakta<R>) = throw IllegalStateException("Spørsmålet er ikke aktivt")

        override fun <R> spør(fakta: Fakta<R>) = fakta.apply {
            tilstand = Ubesvart
        }
    }

    private object Ubesvart : Tilstand {
        override fun <R> svar(fakta: Fakta<R>) = Ubesvart(fakta)
    }

    private object Besvart : Tilstand {
        override fun <R> svar(fakta: Fakta<R>) = fakta.gjeldendeSvar
    }
}

interface SpørsmålStrategi<R> {
    fun besvar(r: R, fakta: Fakta<R>): Svar
}

internal typealias SvarStrategi = () -> Unit

