package no.nav.dagpenger.model.faktum

class Land(alpha3Code: String) : Comparable<Land> {
    companion object {
        internal val gyldigeLand = LandOppslag.land()
        internal val pdlKodeForUkjentLand = "XXX"
    }

    val alpha3Code: String

    init {
        require(alpha3Code.length == 3) {
            "ISO 3166-1-alpha3 må være 3 bokstaver lang. Fikk: $alpha3Code"
        }

        if (alpha3Code.erAntattKjentLand()) {
            require(LandOppslag.fraAlpha3Code(alpha3Code) != null) {
                "Ugyldig land kode: $alpha3Code"
            }
        }

        this.alpha3Code = alpha3Code.uppercase()
    }

    private fun String.erAntattKjentLand() = !pdlKodeForUkjentLand.equals(this, ignoreCase = true)

    override fun compareTo(other: Land): Int {
        return this.alpha3Code.compareTo(other.alpha3Code)
    }

    override fun equals(other: Any?): Boolean {
        return other is Land && this.alpha3Code == other.alpha3Code
    }

    override fun hashCode(): Int {
        return this.alpha3Code.hashCode()
    }

    override fun toString(): String {
        return "Land(alpha3Code='$alpha3Code')"
    }
}
