package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.seksjon.Henvendelser

interface Faktatype {
    val id: String
}

class Faktaversjon(val faktatype: Faktatype, val versjon: Int) {
    internal companion object {
        val prototypeversjon = Faktaversjon(
            object : Faktatype {
                override val id: String = "prototype"
            },
            0,
        )
    }

    fun siste() = Henvendelser.siste(faktatype)

    fun kanMigrereTil(tilVersjon: Faktaversjon): Boolean {
        require(tilVersjon.faktatype.id == faktatype.id) { "Kan ikke migrere til en annen prosesstype." }
        require(tilVersjon.versjon >= versjon) { "Kan ikke migrere bakover. Gjeldende versjon er $versjon, forsøkte å migrere til ${tilVersjon.versjon}" }

        return this != tilVersjon
    }

    init {
        require(faktatype.id.isNotBlank()) { "Prosessnavn kan ikke være blank" }
    }

    override fun equals(other: Any?): Boolean =
        other is Faktaversjon && other.faktatype.id == this.faktatype.id && other.versjon == this.versjon

    override fun hashCode(): Int = faktatype.id.hashCode() * 37 + versjon.hashCode()

    override fun toString(): String {
        return "Faktaversjon(faktatype=$faktatype, versjon=$versjon)"
    }
}
