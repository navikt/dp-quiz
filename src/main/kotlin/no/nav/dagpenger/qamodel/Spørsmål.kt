package no.nav.dagpenger.qamodel

class Spørsmål(private val fakta: Fakta, private val spørsmålStrategi: SpørsmålStrategi) {
    private var tilstand: Tilstand = Ubesvart
    private lateinit var gjeldendeSvar: Svar

    fun svar() = tilstand.svar(this)

    fun besvar(any: Any) = spørsmålStrategi.besvar(any).also {
        gjeldendeSvar = it
        tilstand = Besvart
    }

    private interface Tilstand {
        fun svar(spørsmål: Spørsmål): Svar
    }

    private object Ubesvart : Tilstand {
        override fun svar(spørsmål: Spørsmål) = Ubesvart(spørsmål.fakta)
    }

    private object Besvart : Tilstand {
        override fun svar(spørsmål: Spørsmål) = spørsmål.gjeldendeSvar
    }
}

interface SpørsmålStrategi {
    fun besvar(any: Any): Svar
}
