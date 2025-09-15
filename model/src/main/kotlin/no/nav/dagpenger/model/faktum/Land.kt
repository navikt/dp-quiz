package no.nav.dagpenger.model.faktum

class Land(
    alpha3Code: String,
) : Comparable<Land> {
    val alpha3Code: String

    init {
        require(alpha3Code.length == 3) {
            "ISO 3166-1-alpha3 må være 3 bokstaver lang. Fikk: $alpha3Code"
        }

        this.alpha3Code = alpha3Code.uppercase()
    }

    override fun compareTo(other: Land): Int = this.alpha3Code.compareTo(other.alpha3Code)

    override fun equals(other: Any?): Boolean = other is Land && this.alpha3Code == other.alpha3Code

    override fun hashCode(): Int = this.alpha3Code.hashCode()

    override fun toString(): String = "Land(alpha3Code='$alpha3Code')"
}
