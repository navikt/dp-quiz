package no.nav.dagpenger.model.faktum

interface HenvendelsesType {
    val id: String
}

class Prosessversjon(val henvendelsesType: HenvendelsesType, val prosessnavn: String)

class FaktaVersjon(val henvendelsesType: HenvendelsesType, val versjon: Int) {
    internal companion object {
        val prototypeversjon = FaktaVersjon(
            object : HenvendelsesType {
                override val id: String = "prototype"
            },
            0
        )
    }

    fun siste() = this

    fun kanMigrereTil(tilVersjon: FaktaVersjon): Boolean {
        require(tilVersjon.henvendelsesType.id == henvendelsesType.id) { "Kan ikke migrere til en annen prosesstype." }
        require(tilVersjon.versjon >= versjon) { "Kan ikke migrere bakover. Gjeldende versjon er $versjon, forsøkte å migrere til ${tilVersjon.versjon}" }

        return this != tilVersjon
    }

    init {
        require(henvendelsesType.id.isNotBlank()) { "Prosessnavn kan ikke være blank" }
    }

    override fun equals(other: Any?): Boolean =
        other is FaktaVersjon && other.henvendelsesType.id == this.henvendelsesType.id && other.versjon == this.versjon

    override fun hashCode(): Int = henvendelsesType.id.hashCode() * 37 + versjon.hashCode()

    override fun toString(): String {
        return "Prosessversjon(prosessnavn=$henvendelsesType, versjon=$versjon)"
    }
}
