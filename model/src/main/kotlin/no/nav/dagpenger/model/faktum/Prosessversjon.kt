package no.nav.dagpenger.model.faktum

interface Prosessnavn {
    val id: String
}

class Prosessversjon(val prosessnavn: Prosessnavn, val versjon: Int) {

    internal companion object {
        val prototypeversjon = Prosessversjon(
            object : Prosessnavn {
                override val id: String = "prototype"
            },
            0
        )
    }

    init {
        require(prosessnavn.id.isNotBlank()) { "Prosessnavn kan ikke være blank" }
    }

    override fun equals(other: Any?): Boolean = other is Prosessversjon && other.prosessnavn.id == this.prosessnavn.id && other.versjon == this.versjon

    override fun hashCode(): Int = prosessnavn.id.hashCode() * 37 + versjon.hashCode()
}
