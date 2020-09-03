package no.nav.dagpenger.qamodel

class Spørsmål<R> (private val fakta: Fakta, private val spørsmålStrategi: SpørsmålStrategi<R>) {
    private var tilstand: Tilstand = Ubesvart
    private lateinit var gjeldendeSvar: Svar

    fun svar() = tilstand.svar(this)

    fun besvar(r: R) = spørsmålStrategi.besvar(r).also {
        gjeldendeSvar = it
        tilstand = Besvart
    }

    private interface Tilstand {
        fun <R> svar(spørsmål: Spørsmål<R>): Svar
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
