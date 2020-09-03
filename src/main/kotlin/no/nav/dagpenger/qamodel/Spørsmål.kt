package no.nav.dagpenger.qamodel

import java.lang.IllegalStateException

class Spørsmål<R> (private val fakta: Fakta, private val strategi: SpørsmålStrategi<R>) {
    private var tilstand: Tilstand = Inaktivt
    private lateinit var gjeldendeSvar: Svar

    fun svar() = tilstand.svar(this)

    fun besvar(r: R) = tilstand.besvar(r, this)
    fun spør() = tilstand.spør(this)

    private fun _besvar(r: R) = strategi.besvar(r).also {
        gjeldendeSvar = it
        tilstand = Besvart
    }

    private interface Tilstand {
        fun <R> svar(spørsmål: Spørsmål<R>): Svar

        fun <R> besvar(r: R, spørsmål: Spørsmål<R>) = spørsmål._besvar(r)

        fun <R> spør(spørsmål: Spørsmål<R>): Spørsmål<R> = throw IllegalStateException("Spørsmålet er allerede spurt")
    }

    private object Inaktivt : Tilstand {
        override fun <R> svar(spørsmål: Spørsmål<R>) = Ubesvart(spørsmål.fakta)

        override fun <R> besvar(r: R, spørsmål: Spørsmål<R>) = throw IllegalStateException("Spørsmålet er ikke aktivt")

        override fun <R> spør(spørsmål: Spørsmål<R>) = spørsmål.apply {
            tilstand = Ubesvart
        }
    }

    private object Ubesvart : Tilstand {
        override fun <R> svar(spørsmål: Spørsmål<R>) = Ubesvart(spørsmål.fakta)
    }

    private object Besvart : Tilstand {
        override fun <R> svar(spørsmål: Spørsmål<R>) = spørsmål.gjeldendeSvar
    }
}

interface SpørsmålStrategi<R> {
    fun besvar(r: R): Svar
}
